package net.sharplab.epubtranslator.core.service

import net.sharplab.epubtranslator.app.config.EpubGenerationConfig
import net.sharplab.epubtranslator.core.driver.translator.DeepLTranslatorException
import net.sharplab.epubtranslator.core.driver.translator.Translator
import net.sharplab.epubtranslator.core.model.EPubChapter
import net.sharplab.epubtranslator.core.model.EPubContentFile
import net.sharplab.epubtranslator.core.model.EPubFile
import net.sharplab.epubtranslator.core.util.XmlParser
import net.sharplab.epubtranslator.core.util.logSubString
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.ls.DOMImplementationLS
import java.nio.charset.StandardCharsets
import java.util.function.Predicate
import javax.enterprise.context.Dependent

@Dependent
class EPubTranslatorServiceImpl(
    private val translator: Translator,
    private val translationMemoryService: TranslationMemoryService,
    private val epubGenerationConfig: EpubGenerationConfig,
    private val xmlParser: XmlParser,
) : EPubTranslatorService {

    private val logger = LoggerFactory.getLogger(EPubTranslatorServiceImpl::class.java)

    private var lastTranslatedUsed = LogTranslationSource()
    private var translationFailure: TranslationFailure? = null

    /**
     * Computes number of characters (including xml-formatting chars) in each content-file of the epub.
     * @return map of filename -> character count
     */
    override fun countCharacters(ePubFile: EPubFile): Map<String, Int> {
        return ePubFile.contentFiles
            .filterIsInstance<EPubChapter>()
            .associate { contentFile: EPubChapter ->
                val contents = contentFile.dataAsString
                val contentFileName = contentFile.name

                val document = xmlParser.parse(contents)
                val translationRequests = generateTranslationRequests(document)
                val characters = translationRequests.sumOf { it.sourceXmlString.length }

                logger.info("File {} contains # characters: {} ", contentFileName, characters)
                Pair(contentFileName, characters)
            }
    }

    override fun translate(ePubFile: EPubFile, srcLang: String, dstLang: String): Pair<EPubFile, TranslationFailure?> {
        val contentFiles = ePubFile.contentFiles
        val translatedContentFiles = contentFiles.map { contentFile: EPubContentFile ->
            if (contentFile is EPubChapter) {
                lastTranslatedUsed.clear()
                val contents = contentFile.dataAsString
                val contentFileName = contentFile.name
                val translatedContents = translateEPubXhtmlString(contentFileName, contents, srcLang, dstLang)
                val ePubChapter = EPubChapter(contentFileName, translatedContents.toByteArray(StandardCharsets.UTF_8))
                logger.info("{}", lastTranslatedUsed.logTranslation(ePubChapter.name))
                return@map ePubChapter
            } else {
                return@map contentFile
            }
        }
        return Pair(EPubFile(translatedContentFiles), translationFailure)
    }

    private fun translateEPubXhtmlString(contentFileName: String, xhtmlString: String, srcLang: String, dstLang: String): String {
        val document = xmlParser.parse(xhtmlString)
        val translatedDocument: Document = translateEPubXhtmlDocument(contentFileName, document, srcLang, dstLang)
        val domImplementation = translatedDocument.implementation as DOMImplementationLS
        val lsSerializer = domImplementation.createLSSerializer()
        lsSerializer.domConfig.setParameter("xml-declaration", true)
        lsSerializer.domConfig.setParameter("element-content-whitespace", true)
        lsSerializer.domConfig.setParameter("canonical-form", false)
        return lsSerializer.writeToString(translatedDocument)
    }

    /**
     * Translates document, first by using database, then by translating remaining by DeepL.
     * If DeepL fails we record the exception and continue, so we can produce and output
     * document with what we got until failure.
     */
    private fun translateEPubXhtmlDocument(contentFileName: String, document: Document, srcLang: String, dstLang: String): Document {
        var translationRequests = generateTranslationRequests(document)
        lastTranslatedUsed.withSourceTexts(translationRequests)
        translationRequests = translateWithTranslationMemory(translationRequests, srcLang, dstLang)
        val translationRequestChunks = formTranslationRequestChunks(translationRequests)
        try {
            translateWithDeepL(translationRequestChunks, srcLang, dstLang)
        } catch (e: Exception) {
            lastTranslatedUsed.withDeepLFailure(e)
            // only record first exception
            if (translationFailure == null)
                translationFailure = TranslationFailure(
                    exception = e,
                    reason = e.cause?.message ?: e.message,
                    contentFileName = contentFileName,
                )
        }
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
            if (translatedString == null) {
                list.add(it)
            } else {
                if (doLog) logger.info("load: {}, translated: {}", it.sourceXmlString.logSubString(), translatedString.logSubString())
                lastTranslatedUsed.withDatabase(it)
                replaceWithTranslatedString(it, translatedString)
            }
        }
        if (translationRequests.isEmpty()) lastTranslatedUsed.emptyFile()
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
        return translationRequests.filter { it.sourceXmlString.isNotBlank() }
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
    @Throws(DeepLTranslatorException::class) // For documentation, this is what gets thrown on Quota Exceeded, in which case it wraps a QuotaExceededException
    private fun translateWithDeepL(translationRequestChunks: List<TranslationRequestChunk>, srcLang: String, dstLang: String) {
        translationRequestChunks
            .forEach { translationRequestChunk: TranslationRequestChunk ->
                val translationRequests = translationRequestChunk.translationRequests
                val translationResponse: List<String> =
                    translator.translate(translationRequests.map(TranslationRequest::sourceXmlString), srcLang, dstLang)
                lastTranslatedUsed.withDeepL(translationRequests)
                for (i in translationRequests.indices) {
                    val translationRequest = translationRequests[i]
                    val translatedString = translationResponse[i]
                    if (doLog) logger.info("Saving translation in DB: ${translatedString.logSubString()}")
                    translationMemoryService.save(translationRequest.sourceXmlString, translatedString, srcLang, dstLang)
                    replaceWithTranslatedString(translationRequest, translatedString)
                }
            }
    }

    private fun replaceWithTranslatedString(translationRequest: TranslationRequest, translatedString: String) {
        val translatedStringWithPostfix =
            if (epubGenerationConfig.applyPrefix) {
                epubGenerationConfig.outputTranslatedPrefix + translatedString
            } else translatedString

        if (doLogReplacements) logger.info("Replacing '${translationRequest.sourceXmlString}' -> '$translatedStringWithPostfix'")

        val document = translationRequest.document
        val firstNode = translationRequest.target.first()
        val documentFragment = xmlParser.parseStringToDocumentFragment(document, translatedStringWithPostfix)

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

    /**
     * 翻訳対象ノードか判定する
     *
     * @param node 判定対象ノード
     * @return 翻訳対象ノードの場合、true
     */
    private fun isTranslationTargetNode(node: Node): Boolean {
        return node.nodeType == Node.TEXT_NODE || node.nodeType == Node.ELEMENT_NODE && INLINE_ELEMENT_NAMES.contains(node.nodeName)
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
        // @formatter:off - disables IntelliJ autoformatting which might split this line
        /**
         * インライン要素のタグリスト
         */
        val INLINE_ELEMENT_NAMES = listOf("a", "abbr", "b", "bdi", "bdo", "br", "cite", "code", "data", "dfn", "em", "i", "kbd", "mark", "q", "rp", "rt", "rtc", "ruby", "s", "samp", "small", "span", "strong", "sub", "sup", "time", "u", "var", "wbr")
        // @formatter:on

        /**
         * 翻訳除外要素のリスト
         */
        private val EXCLUDED_ELEMENT_NAMES = listOf("head", "pre", "tt")

        private const val doLog = false
        private const val doLogReplacements = doLog

        class LogTranslationSource {
            private var charCountSource: Int = 0
            private var charCountTranslatedDeepL: Int = 0
            private var charCountTranslatedDatabase: Int = 0
            private var translatedUsingInMemory: Boolean = false
            private var translatedUsingDeepL: Boolean = false
            private var deeplException: Exception? = null
            private var isFileEmpty: Boolean = false

            fun clear() {
                charCountTranslatedDeepL = 0
                charCountTranslatedDatabase = 0
                charCountSource = 0
                translatedUsingInMemory = false
                translatedUsingDeepL = false
                deeplException = null
                isFileEmpty = false
            }

            fun withSourceTexts(translationRequests: List<TranslationRequest>) {
                charCountSource = translationRequests.sumOf { it.sourceXmlString.length }
            }

            fun withDeepLFailure(exception: java.lang.Exception) {
                deeplException = exception
            }

            fun withDeepL(translatedSourceTexts: List<TranslationRequest>) {
                translatedUsingDeepL = true
                charCountTranslatedDeepL += translatedSourceTexts.sumOf { it.sourceXmlString.length }
            }

            fun withDatabase(translatedSourceTexts: TranslationRequest) {
                charCountTranslatedDatabase += translatedSourceTexts.sourceXmlString.length
                translatedUsingInMemory = true
            }

            fun emptyFile() {
                isFileEmpty = true
            }

            fun logTranslation(filename: String): String {
                val totalTranslated = charCountTranslatedDeepL + charCountTranslatedDatabase
                val translated = deeplException?.let { "partially translated with $totalTranslated/$charCountSource chars" } ?: "translated with $charCountSource chars"
                val postFix = deeplException?.let { " but with DeepL failures" } ?: ""

                return if (translatedUsingDeepL && translatedUsingInMemory) "$filename is $translated using DeepL and existing database$postFix"
                else if (translatedUsingDeepL) "$filename is $translated using DeepL$postFix"
                else if (translatedUsingInMemory) "$filename is $translated using existing database$postFix"
                else if (isFileEmpty) "$filename: Nothing to translate"
                else "$filename was not translated with $charCountSource chars${deeplException?.let { " due to DeepL failures" } ?: ""}" // DeepL's error should be the only reason for this.
            }
        }
    }

}