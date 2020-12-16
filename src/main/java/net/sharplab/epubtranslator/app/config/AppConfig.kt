package net.sharplab.epubtranslator.app.config

import net.sharplab.epubtranslator.app.EPubTranslatorSetting
import net.sharplab.epubtranslator.core.driver.epub.EPubReader
import net.sharplab.epubtranslator.core.driver.epub.EPubWriter
import net.sharplab.epubtranslator.core.driver.translator.DeepLTranslator
import net.sharplab.epubtranslator.core.driver.translator.Translator
import net.sharplab.epubtranslator.core.provider.epub.DefaultEPubContentFileProvider
import net.sharplab.epubtranslator.core.provider.epub.EPubChapterProvider
import java.lang.IllegalArgumentException
import javax.enterprise.context.Dependent
import javax.enterprise.inject.Produces

@Dependent
class AppConfig(private val ePubTranslatorSetting: EPubTranslatorSetting) {
    @Produces
    fun translator(): Translator {
        val apiKey = ePubTranslatorSetting.deepLApiKey ?: throw IllegalArgumentException("deepLApiKey is not provided")
        return DeepLTranslator(apiKey)
    }

    @Produces
    fun ePubReader(): EPubReader {
        val contentFileProviders = listOf(EPubChapterProvider(), DefaultEPubContentFileProvider())
        return EPubReader(contentFileProviders)
    }

    @Produces
    fun ePubWriter(): EPubWriter {
        return EPubWriter()
    }

}
