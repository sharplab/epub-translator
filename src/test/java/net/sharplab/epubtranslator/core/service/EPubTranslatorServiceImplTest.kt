package net.sharplab.epubtranslator.core.service

import io.quarkus.test.junit.QuarkusTest
import net.sharplab.epubtranslator.app.config.EpubGenerationConfig
import net.sharplab.epubtranslator.core.driver.epub.EPubReader
import net.sharplab.epubtranslator.core.util.XmlParser
import net.sharplab.epubtranslator.core.util.XmlParserImpl
import net.sharplab.epubtranslator.core.util.XmlUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.w3c.dom.Document
import org.w3c.dom.DocumentFragment
import java.io.File
import javax.inject.Inject

@QuarkusTest
internal class EPubTranslatorServiceImplTest {

    private val testDir = "src/test/resources"
    private val testSrcFile = File(testDir, "/bad-book-on-certain-replacement.epub")
    private val badReplacementString = """<em xmlns="http://www.w3.org/1999/xhtml" class="calibre2">one</em><em xmlns="http://www.w3.org/1999/xhtml" class="calibre2">two</em>"""

    @Inject
    private lateinit var ePubReader: EPubReader

    // We could also use
    private val translationMemoryService =
        object : TranslationMemoryService {
            override fun load(source: String, sourceLang: String, targetLang: String): String {
                return badReplacementString
            }

            override fun save(source: String, target: String, sourceLang: String, targetLang: String) {
            }
        }

    private val translator = object : net.sharplab.epubtranslator.core.driver.translator.Translator {
        override fun translate(texts: List<String>, srcLang: String, dstLang: String): List<String> = texts
    }


    // This will fail with NullPointerException if XmlUtils.parseXmlStringToDocument does not checkDoc()
    @Test
    fun parseEpub() {
        val sut = EPubTranslatorServiceImpl(translator, translationMemoryService, EpubGenerationConfig(false, ""), XmlParserImpl())
        val ePubFile = ePubReader.read(testSrcFile)
        sut.translate(ePubFile, "en", "da")
    }

    @Test
    fun parseEpubFailing() {
        val xmlParserWithoutCheckDoc = object : XmlParser {
            override fun parse(xmlString: String): Document = XmlUtils.parseXmlStringToDocumentWithoutCheckDoc(xmlString)
            override fun parseStringToDocumentFragment(document: Document, xmlString: String): DocumentFragment = XmlUtils.parseXmlStringToDocumentFragment(document, xmlString)
        }

        val sut = EPubTranslatorServiceImpl(
            translator, translationMemoryService, EpubGenerationConfig(false, ""),
            xmlParserWithoutCheckDoc
        )

        val ePubFile = ePubReader.read(testSrcFile)

        val exception = assertThrows<org.w3c.dom.ls.LSException> {
            sut.translate(ePubFile, "en", "da")
        }
        println(exception)
        Assertions.assertEquals("java.lang.NullPointerException", exception.message) // Strangely enough, the cause is null
    }
}