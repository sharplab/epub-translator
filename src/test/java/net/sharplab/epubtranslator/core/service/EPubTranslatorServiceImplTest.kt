package net.sharplab.epubtranslator.core.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.quarkus.test.junit.QuarkusTest
import net.sharplab.epubtranslator.app.config.EpubGenerationConfig
import net.sharplab.epubtranslator.core.driver.epub.EPubReader
import net.sharplab.epubtranslator.core.driver.translator.Translator
import net.sharplab.epubtranslator.core.util.XmlParser
import net.sharplab.epubtranslator.core.util.XmlParserImpl
import net.sharplab.epubtranslator.core.util.XmlUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.w3c.dom.Document
import java.io.File
import javax.inject.Inject

@QuarkusTest
internal class EPubTranslatorServiceImplTest {

    private val testDir = "src/test/resources"
    private val testSrcFile = File(testDir, "/bad-book-on-certain-replacement.epub")
    // This string is a relatively short version that demonstrates the problem.
    // Note that tiny changes stops this string from failing, such as removing preprocessNode() or adding/removing Separator/leading word.
    private val badReplacementString =
        """Hi <em xmlns="http://www.w3.org/1999/xhtml" class="calibre2">one</em>
            | SEPARATOR1 <em xmlns="http://www.w3.org/1999/xhtml" class="calibre2">two</em>
            | SEPARATOR2 <em xmlns="http://www.w3.org/1999/xhtml" class="calibre2">three</em>
            |""".trimMargin()
    // This is the full string which reliably caused problems. If the above stops failing in future version of library, it makes sense to test it on this more complex string.
    //    private val badReplacementString = """"Hvorfor-" sagde Rianne Felthorne. <em xmlns="http://www.w3.org/1999/xhtml" class="calibre2">Han ved, at jeg ikke vil huske det.</em> "Hvorfor <em xmlns="http://www.w3.org/1999/xhtml" class="calibre2">gjorde</em> du det? Jeg mener - du sagde, at jeg skulle fortµlle dig, hvor mobberne ville vµre, og hvem de ville vµre, men ikke om Granger ville vµre der. Og jeg ved godt, at sσdan som Tidsvenderen fungerer, kan man ikke fσ at vide, om det allerede er sket, hvis man vil <em xmlns="http://www.w3.org/1999/xhtml" class="calibre2">have</em> Granger til at vµre der. Sσ jeg fandt ud af, at det var <em xmlns="http://www.w3.org/1999/xhtml" class="calibre2">os</em>, der fortalte hende, hvor hun skulle tage hen. Det var vi, ikke sandt?""""

    @Inject
    private lateinit var ePubReader: EPubReader

    private val translationMemoryService = mockk<TranslationMemoryService> {
        every { load(any(), any(), any()) } returns badReplacementString
    }

    private val translator = mockk<Translator>() // Unused in our test, as all strings have DB-translations, hence no need to mock methods

    private var epubGenerationConfigNoPrefix: EpubGenerationConfig = EpubGenerationConfig(false, "") // Test works, program fails
    private var epubGenerationConfigWithPrefix: EpubGenerationConfig = EpubGenerationConfig(true, "> ") // Test works, program fails


    // This will fail with NullPointerException if XmlUtils.parseXmlStringToDocument does not checkDoc(), see tests parseEpubFailing_*
    @Test
    fun parseEpub_ShouldSucceed_WhenUsingProdXmlParserImpl_And_noPrefix_OnProblematicTranslatedString() {
        val sut = EPubTranslatorServiceImpl(translator, translationMemoryService, epubGenerationConfigNoPrefix, XmlParserImpl())
        val ePubFile = ePubReader.read(testSrcFile)
        sut.translate(ePubFile, "en", "da", limitCredits = 0, abortOnError = false)
    }

    @Test
    fun parseEpub_ShouldSucceed_WhenUsingProdXmlParserImpl_And_prefix__OnProblematicTranslatedString() {
        val sut = EPubTranslatorServiceImpl(translator, translationMemoryService, epubGenerationConfigWithPrefix, XmlParserImpl())
        val ePubFile = ePubReader.read(testSrcFile)
        sut.translate(ePubFile, "en", "da", limitCredits = 0, abortOnError = false)
    }

    @Test
    fun parseEpubFailing_withPrefix() {
        executeTest_ParseEpubFailing_withConfig(epubGenerationConfigWithPrefix)
    }

    @Test
    fun parseEpubFailing_noPrefix() {
        executeTest_ParseEpubFailing_withConfig(epubGenerationConfigNoPrefix)
    }

    private fun executeTest_ParseEpubFailing_withConfig(epubGenerationConfig: EpubGenerationConfig) {

        // This is actually longer and more complex than just making an anonymous stub, but at least it demonstrates the usage, and might be easier to fit toward other usages later.
        val xmlParserWithoutCheckDoc = mockk<XmlParser> {
            val xmlSlot = slot<String>()
            val documentSlot = slot<Document>()
            every { parse(capture(xmlSlot)) } answers { XmlUtils.parseXmlStringToDocumentWithoutCheckDoc(xmlSlot.captured) }
            every { parseStringToDocumentFragment(capture(documentSlot), capture(xmlSlot)) } answers { XmlUtils.parseXmlStringToDocumentFragment(documentSlot.captured, xmlSlot.captured) }
        }

        val sut = EPubTranslatorServiceImpl(translator, translationMemoryService, epubGenerationConfig, xmlParserWithoutCheckDoc)

        val ePubFile = ePubReader.read(testSrcFile)

        val exception = assertThrows<org.w3c.dom.ls.LSException> {
            sut.translate(ePubFile, "en", "da", limitCredits = 0, abortOnError = false)
        }
        println(exception)
        Assertions.assertEquals("java.lang.NullPointerException", exception.message) // Strangely enough, the cause is null
    }
}