package net.sharplab.epubtranslator.app.config

import net.sharplab.epubtranslator.app.EPubTranslatorSetting
import net.sharplab.epubtranslator.core.driver.epub.EPubReader
import net.sharplab.epubtranslator.core.driver.epub.EPubWriter
import net.sharplab.epubtranslator.core.driver.translator.DeepLTranslator
import net.sharplab.epubtranslator.core.driver.translator.Translator
import net.sharplab.epubtranslator.core.provider.epub.DefaultEPubContentFileProvider
import net.sharplab.epubtranslator.core.provider.epub.EPubChapterProvider
import javax.enterprise.context.Dependent
import javax.enterprise.inject.Produces

@Dependent
class AppConfig(private val ePubTranslatorSetting: EPubTranslatorSetting) {
    @Produces
    fun translator(): Translator {
        val apiEndpoint = ePubTranslatorSetting.deepLApiEndpoint
        val apiKey = ePubTranslatorSetting.deepLApiKey
        if(apiEndpoint == null){
            throw RuntimeException("ePubTranslator.deepL.apiEndpoint must be provided in application.yml")
        }
        if(apiKey == null){
            throw RuntimeException("ePubTranslator.deepL.apiKey must be provided in application.yml")
        }
        return DeepLTranslator(apiEndpoint, apiKey)
    }

    @Produces
    fun epubGenerationConfig(): EpubGenerationConfig {
        return EpubGenerationConfig(
            applyPrefix = ePubTranslatorSetting.outputApplyTranslatedPrefix,
            outputTranslatedPrefix = ePubTranslatorSetting.outputTranslatedPrefix ?: "",
        )
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