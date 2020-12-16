package net.sharplab.epubtranslator.core.service

import net.sharplab.epubtranslator.core.driver.translator.Translator
import net.sharplab.epubtranslator.core.model.EPubChapter
import net.sharplab.epubtranslator.core.model.EPubFile
import net.sharplab.epubtranslator.core.util.XmlUtils.parseXmlStringToDocument
import net.sharplab.epubtranslator.core.util.XmlUtils.parseXmlStringToDocumentFragment
import net.sharplab.epubtranslator.core.util.XmlUtils.serialize
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import javax.enterprise.context.Dependent

@Dependent
class EPubTranslatorServiceImpl(private val translator: Translator) : EPubTranslatorService {

    private val logger = LoggerFactory.getLogger(EPubTranslatorServiceImpl::class.java)

    override fun translate(ePubFile: EPubFile, srcLang: String, dstLang: String): EPubFile {
        val contentFiles = ePubFile.contentFiles
        val translatedContentFiles = contentFiles.map { contentFile ->
            when (contentFile) {
                is EPubChapter -> {
                    val contents = contentFile.dataAsString
                    val translatedContents = translateXmlString(contents, srcLang, dstLang)
                    val ePubChapter = EPubChapter(contentFile.name, translatedContents.toByteArray(StandardCharsets.UTF_8))
                    logger.info("{} is translated.", ePubChapter.name)
                    return@map ePubChapter
                }
                else -> {
                    return@map contentFile
                }
            }
        }
        return EPubFile(translatedContentFiles)
    }

    fun translateXmlString(xmlString: String, srcLang: String, dstLang: String): String {
        val document = parseXmlStringToDocument(xmlString)
        val translatedDocument = translateDocument(document, srcLang, dstLang)
        return serialize(translatedDocument)
    }

    private fun translateDocument(document: Document, srcLang: String, dstLang: String): Document {
        preProcessNode(document)
        val translateRequests = generateTranslateRequests(document)
        val translateRequestChunks = formTranslateRequestChunks(translateRequests)
        replaceWithTranslatedText(translateRequestChunks, srcLang, dstLang)
        return document
    }

    /**
     * 翻訳リクエストのリストを作成する
     * @param document 翻訳元のXML文書
     * @return 翻訳リクエストのリスト
     */
    private fun generateTranslateRequests(document: Document): List<TranslateRequest> {
        val translateRequests: MutableList<TranslateRequest> = ArrayList()
        val wip = TranslateRequest(document, ArrayList())
        translateRequests.add(wip)
        walkToGenerateTranslateRequests(document, HashSet(), translateRequests)
        return translateRequests.filter { it.sourceXmlString.trim(' ').isNotEmpty() }
    }

    /**
     * XML文書を走査して翻訳リクエストのリストを作成する
     * @param node 走査対象のノード
     * @param processedSet 処理済ノードのセット
     * @param translateRequests 翻訳リクエストのリスト
     */
    private fun walkToGenerateTranslateRequests(node: Node, processedSet: MutableSet<Node>, translateRequests: MutableList<TranslateRequest>) {
        val document = node.ownerDocument
        //処理済のNodeはスキップ
        if (processedSet.contains(node)) {
            return
        }
        if (isExcludedNode(node)) {
            return
        }
        var wip = translateRequests[translateRequests.size - 1]
        //翻訳対象ノードの場合
        if (isTranslationTargetNode(node)) {
            //テキスト中なので、リストに追加
            wip.target.add(node)
            //同一階層の次以降のノードの探索
            var nextNode = node.nextSibling
            while (nextNode != null) {

                //翻訳対象Nodeならリストに追加、処理済セットに追加
                if (isTranslationTargetNode(nextNode)) {
                    wip.target.add(nextNode)
                    processedSet.add(nextNode)
                } else if (wip.target.size > 0) {
                    wip = TranslateRequest(document, ArrayList())
                    translateRequests.add(wip)
                    break
                }
                nextNode = nextNode.nextSibling
            }
            //親ノードが、翻訳対象ノード以外の場合、テキストの末尾なので次のTranslateRequestを準備、但しWIPが0件でない場合に限る
            if (!isTranslationTargetNode(node.parentNode) && wip.target.size > 0) {
                wip = TranslateRequest(document, ArrayList())
                translateRequests.add(wip)
            }
        } else {
            //子ノードを走査し、再帰処理
            val childNodes = node.childNodes
            for (i in 0 until childNodes.length) {
                val child = childNodes.item(i)
                walkToGenerateTranslateRequests(child, processedSet, translateRequests)
            }
        }
    }

    /**
     * 翻訳除外ノードか判定する
     * @param node 判定対象ノード
     * @return 翻訳除外ノードの場合、true
     */
    private fun isExcludedNode(node: Node): Boolean {
        return EXCLUDED_ELEMENT_NAMES.contains(node.nodeName) || isTranslationTargetNode(node) && lookupNode(node, Predicate { isExcludedNode(it) })
    }

    /**
     * 翻訳リクエストのリストを翻訳リクエストのチャンクに構成する
     * @param translateRequests 翻訳リクエストのリスト
     * @return 翻訳リクエストのチャンクのリスト
     */
    private fun formTranslateRequestChunks(translateRequests: List<TranslateRequest>): List<TranslateRequestChunk> {
        var textLengthCounter = 0
        val translateRequestChunks: MutableList<TranslateRequestChunk> = ArrayList()
        var workingTranslateRequestList: MutableList<TranslateRequest> = ArrayList()
        translateRequests.forEach { translateRequest ->
            val sourceXmlString = translateRequest.sourceXmlString
            if (textLengthCounter + sourceXmlString.length > MAX_REQUESTABLE_TEXT_LENGTH) {
                translateRequestChunks.add(TranslateRequestChunk(workingTranslateRequestList.toList()))
                workingTranslateRequestList = ArrayList()
                textLengthCounter = 0
            }
            workingTranslateRequestList.add(translateRequest)
            textLengthCounter += sourceXmlString.length
        }
        translateRequestChunks.add(TranslateRequestChunk(workingTranslateRequestList))
        return translateRequestChunks
    }

    /**
     * XML文書中の翻訳リクエストのチャンクで指定された箇所を翻訳する
     * @param translateRequestChunks 翻訳リクエストチャンクのリスト
     */
    private fun replaceWithTranslatedText(translateRequestChunks: List<TranslateRequestChunk>, srcLang: String, dstLang: String) {
        translateRequestChunks.forEach(Consumer { translateRequestChunk: TranslateRequestChunk ->
            val translateRequests = translateRequestChunk.translateRequests
            val translateResponse = translator.translate(translateRequests.map { it.sourceXmlString }, srcLang, dstLang)
            for (i in translateRequests.indices) {
                val translateRequest = translateRequests[i]
                val document = translateRequest.document
                val firstNode = translateRequest.target[0]
                val documentFragment = parseXmlStringToDocumentFragment(document, translateResponse[i])
                val translatedTextContainerDiv = document.createElement("div")
                translatedTextContainerDiv.appendChild(documentFragment)
                firstNode.parentNode.insertBefore(translatedTextContainerDiv, firstNode)
                val originalTextContainerDiv = document.createElement("div")
                translatedTextContainerDiv.parentNode.insertBefore(originalTextContainerDiv, translatedTextContainerDiv)
                val targetNodes = translateRequest.target
                for (targetNode in targetNodes) {
                    originalTextContainerDiv.appendChild(targetNode)
                }
            }
        })
    }

    /**
     * 前処理
     */
    private fun preProcessNode(node: Node) {
        // 空ノードにコメントを挿入し、空ノードを存在しないように処理。空ノードは正常に処理出来ない為
        val children = node.childNodes
        for (i in 0 until children.length) {
            val child = children.item(i)
            if (isEmptyElement(child)) {
                child.appendChild(node.ownerDocument.createComment("place holder"))
            } else {
                preProcessNode(child)
            }
        }
    }

    /**
     * 翻訳対象ノードか判定する
     * @param node 判定対象ノード
     * @return 翻訳対象ノードの場合、true
     */
    private fun isTranslationTargetNode(node: Node): Boolean {
        return node.nodeType == Node.TEXT_NODE || node.nodeType == Node.ELEMENT_NODE && INLINE_ELEMENT_NAMES.contains(node.nodeName)
    }

    private fun isEmptyElement(node: Node): Boolean {
        return node.nodeType == Node.ELEMENT_NODE && !node.hasChildNodes()
    }

    private fun lookupNode(node: Node, condition: Predicate<Node>): Boolean {
        val childNodes = node.childNodes
        for (i in 0 until childNodes.length) {
            val child = childNodes.item(i)
            if (condition.test(child)) {
                return true
            } else {
                if (lookupNode(child, condition)) {
                    return true
                }
            }
        }
        return false
    }

    companion object {
        /**
         * 一つの翻訳リクエストチャンクに含めることが可能な最大文字数
         */
        @Suppress("SpellCheckingInspection")
        private const val MAX_REQUESTABLE_TEXT_LENGTH = 5000

        /**
         * インライン要素のタグリスト
         */
        @JvmField
        val INLINE_ELEMENT_NAMES = listOf("a", "abbr", "b", "bdi", "bdo", "br", "cite", "code", "data", "dfn", "em", "i", "kbd", "mark", "q", "rp", "rt", "rtc", "ruby", "s", "samp", "small", "span", "strong", "sub", "sup", "time", "u", "var", "wbr")

        /**
         * 翻訳除外要素のリスト
         */
        val EXCLUDED_ELEMENT_NAMES = listOf("head", "pre", "tt")
    }

}