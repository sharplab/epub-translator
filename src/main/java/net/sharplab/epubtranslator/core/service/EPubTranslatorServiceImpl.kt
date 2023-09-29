package net.sharplab.epubtranslator.core.service

import net.sharplab.epubtranslator.core.driver.translator.Translator
import net.sharplab.epubtranslator.core.model.EPubChapter
import net.sharplab.epubtranslator.core.model.EPubContentFile
import net.sharplab.epubtranslator.core.model.EPubFile
import net.sharplab.epubtranslator.core.util.XmlUtils.parseXmlStringToDocument
import net.sharplab.epubtranslator.core.util.XmlUtils.parseXmlStringToDocumentFragment
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.ls.DOMImplementationLS
import java.nio.charset.StandardCharsets
import java.util.function.Consumer
import java.util.function.Predicate
import javax.enterprise.context.Dependent
import java.io.StringWriter
import org.w3c.dom.Element


@Dependent
class EPubTranslatorServiceImpl(private val translator: Translator, private val translationMemoryService: TranslationMemoryService) : EPubTranslatorService {

    private val logger = LoggerFactory.getLogger(EPubTranslatorServiceImpl::class.java)

    override fun translate(ePubFile: EPubFile, srcLang: String, dstLang: String): EPubFile {
        val contentFiles = ePubFile.contentFiles
        val translatedContentFiles = contentFiles.map { contentFile: EPubContentFile ->
            if (contentFile is EPubChapter) {
                val contents = contentFile.dataAsString
                val translatedContents = translateEPubXhtmlString(contents, srcLang, dstLang)
                val ePubChapter = EPubChapter(contentFile.name, translatedContents.toByteArray(StandardCharsets.UTF_8))
                logger.info("{} is translated.", ePubChapter.name)
                return@map ePubChapter
            } else {
                return@map contentFile
            }
        }
        return EPubFile(translatedContentFiles)
    }

    fun translateEPubXhtmlString(xhtmlString: String, srcLang: String, dstLang: String): String {
        val document = parseXmlStringToDocument(xhtmlString)
        val translatedDocument = translateEPubXhtmlDocument(document, srcLang, dstLang)
        val domImplementation = translatedDocument.implementation as DOMImplementationLS

        val lsOutput = domImplementation.createLSOutput()
        lsOutput.encoding = "UTF-8"
        val stringWriter = StringWriter()
        lsOutput.characterStream = stringWriter

        val lsSerializer = domImplementation.createLSSerializer()
        lsSerializer.domConfig.setParameter("xml-declaration", true)
        lsSerializer.domConfig.setParameter("element-content-whitespace", true)
        lsSerializer.domConfig.setParameter("canonical-form", false)
        lsSerializer.write(translatedDocument, lsOutput)
        return stringWriter.toString();
    }

    private fun translateEPubXhtmlDocument(document: Document, srcLang: String, dstLang: String): Document {
        preProcessNode(document)
        var translationRequests = generateTranslationRequests(document)
        translationRequests = translateWithTranslationMemory(translationRequests, srcLang, dstLang)
        val translationRequestChunks = formTranslationRequestChunks(translationRequests)
        translateWithDeepL(translationRequestChunks, srcLang, dstLang)
        return document
    }

    private fun translateWithTranslationMemory(
        translationRequests: List<TranslationRequest>,
        srcLang: String,
        dstLang: String
    ): List<TranslationRequest> {
        val list = ArrayList<TranslationRequest>()
        translationRequests.forEach {
            val translatedString = translationMemoryService.load(it.sourceXmlString, srcLang, dstLang)
            if(translatedString == null){
                list.add(it)
            }
            else{
                replaceWithTranslatedString(it, translatedString)
            }
        }
        return list
    }

    /**
     * 翻訳リクエストのリストを作成する
     *
     * @param document 翻訳元のXML文書
     * @return 翻訳リクエストのリスト
     */
    private fun generateTranslationRequests(document: Document): List<TranslationRequest> {
        val translationRequests: MutableList<TranslationRequest> = ArrayList()
        val wip = TranslationRequest(document, ArrayList())
        translationRequests.add(wip)
        walkToGenerateTranslationRequests(document, HashSet(), translationRequests)
        return translationRequests.filter { it.sourceXmlString.trim(' ').isNotEmpty() }
    }

    /**
     * XML文書を走査して翻訳リクエストのリストを作成する
     *
     * @param node              走査対象のノード
     * @param processedSet      処理済ノードのセット
     * @param translationRequests 翻訳リクエストのリスト
     */
    private fun walkToGenerateTranslationRequests(node: Node, processedSet: MutableSet<Node>, translationRequests: MutableList<TranslationRequest>) {
        val document = node.ownerDocument
        //処理済のNodeはスキップ
        if (processedSet.contains(node)) {
            return
        }
        if (isExcludedNode(node)) {
            return
        }
        var wip = translationRequests.last()
        //翻訳対象ノードの場合
        if (isTranslationTargetNode(node)) { //テキスト中なので、リストに追加
            wip.target.add(node)
            //同一階層の次以降のノードの探索
            var nextNode = node.nextSibling
            while (nextNode != null) {
                //翻訳対象Nodeならリストに追加、処理済セットに追加
                if (isTranslationTargetNode(nextNode)) {
                    wip.target.add(nextNode)
                    processedSet.add(nextNode)
                } else if (wip.target.isEmpty()) {
                    wip = TranslationRequest(document, ArrayList())
                    translationRequests.add(wip)
                    break
                }
                nextNode = nextNode.nextSibling
            }
            //親ノードが、翻訳対象ノード以外の場合、テキストの末尾なので次のTranslateRequestを準備、但しWIPが0件でない場合に限る
            if (!isTranslationTargetNode(node.parentNode) && wip.target.size > 0) {
                wip = TranslationRequest(document, ArrayList())
                translationRequests.add(wip)
            }
        } else { //子ノードを走査し、再帰処理
            val childNodes = node.childNodes
            for (i in 0 until childNodes.length) {
                val child = childNodes.item(i)
                walkToGenerateTranslationRequests(child, processedSet, translationRequests)
            }
        }
    }

    /**
     * 翻訳除外ノードか判定する
     *
     * @param node 判定対象ノード
     * @return 翻訳除外ノードの場合、true
     */
    private fun isExcludedNode(node: Node): Boolean {
        return EXCLUDED_ELEMENT_NAMES.contains(node.nodeName) || isTranslationTargetNode(node) && lookupNode(node) { isExcludedNode(it) }
    }

    /**
     * 翻訳リクエストのリストを翻訳リクエストのチャンクに構成する
     *
     * @param translationRequests 翻訳リクエストのリスト
     * @return 翻訳リクエストのチャンクのリスト
     */
    private fun formTranslationRequestChunks(translationRequests: List<TranslationRequest>): List<TranslationRequestChunk> {
        var textLengthCounter = 0
        val translationRequestChunks: MutableList<TranslationRequestChunk> = ArrayList()
        var workingTranslationRequestList: MutableList<TranslationRequest> = ArrayList()
        for (translateRequest in translationRequests) {
            val sourceXmlString = translateRequest.sourceXmlString
            if (textLengthCounter + sourceXmlString.length > MAX_REQUESTABLE_TEXT_LENGTH) {
                translationRequestChunks.add(TranslationRequestChunk(workingTranslationRequestList))
                workingTranslationRequestList = ArrayList()
                textLengthCounter = 0
            }
            workingTranslationRequestList.add(translateRequest)
            textLengthCounter += sourceXmlString.length
        }
        translationRequestChunks.add(TranslationRequestChunk(workingTranslationRequestList))
        return translationRequestChunks
    }

    /**
     * XML文書中の翻訳リクエストのチャンクで指定された箇所を翻訳する
     *
     * @param translationRequestChunks 翻訳リクエストチャンクのリスト
     */
    private fun translateWithDeepL(translationRequestChunks: List<TranslationRequestChunk>, srcLang: String, dstLang: String) {
        translationRequestChunks.forEach(Consumer { translationRequestChunk: TranslationRequestChunk ->
            val translationRequests = translationRequestChunk.translationRequests
            val translationResponse: List<String> =
                translator.translate(translationRequests.map(TranslationRequest::sourceXmlString), srcLang, dstLang)
            for (i in translationRequests.indices) {
                val translationRequest = translationRequests[i]
                val translatedString = translationResponse[i]
                translationMemoryService.save(translationRequest.sourceXmlString, translatedString, srcLang, dstLang)
                replaceWithTranslatedString(translationRequest, translatedString)
            }
        })
    }

    private fun modifyIds(element: Element) {
        if (element.hasAttribute("id")) {
            val originalId = element.getAttribute("id")
            val modifiedId = "${originalId}_translated"
            element.setAttribute("id", modifiedId)
        }
        val children = element.childNodes
        for (i in 0 until children.length) {
            val child = children.item(i)
            if (child is Element) {
                modifyIds(child)
            }
        }
    }

    private fun replaceWithTranslatedString(translationRequest: TranslationRequest, translatedString: String){
        val document = translationRequest.document
        val firstNode = translationRequest.target.first()
        val documentFragment = parseXmlStringToDocumentFragment(document, translatedString)
        val translatedTextContainerDiv = document.createElement("div")
        translatedTextContainerDiv.appendChild(documentFragment)
        firstNode.parentNode.insertBefore(translatedTextContainerDiv, firstNode)
        val originalTextContainerDiv = document.createElement("div")
        translatedTextContainerDiv.parentNode.insertBefore(originalTextContainerDiv, translatedTextContainerDiv)
        val targetNodes = translationRequest.target
        for (targetNode in targetNodes) {
            originalTextContainerDiv.appendChild(targetNode)
        }
    }

    private fun preProcessNode(node: Node) {
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
     *
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
        private const val MAX_REQUESTABLE_TEXT_LENGTH = 1000
        /**
         * インライン要素のタグリスト
         */
        val INLINE_ELEMENT_NAMES = listOf("a", "abbr", "b", "bdi", "bdo", "br", "cite", "code", "data", "dfn", "em", "i", "kbd", "mark", "q", "rp", "rt", "rtc", "ruby", "s", "samp", "small", "span", "strong", "sub", "sup", "time", "u", "var", "wbr")
        /**
         * 翻訳除外要素のリスト
         */
        private val EXCLUDED_ELEMENT_NAMES = listOf("head", "pre", "tt")
    }

}