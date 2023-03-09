package net.sharplab.epubtranslator.core.driver.translator

import com.deepl.api.DeepLException
import com.deepl.api.TextResult
import com.deepl.api.TextTranslationOptions
import com.deepl.api.TranslatorOptions
import net.sharplab.epubtranslator.core.service.EPubTranslatorServiceImpl
import org.slf4j.LoggerFactory

class DeepLTranslator(apiEndpoint: String, apiKey: String) : Translator {

    private val logger = LoggerFactory.getLogger(DeepLTranslator::class.java)
    private val deepLApi: com.deepl.api.Translator

    init {
        val translatorOptions = TranslatorOptions()
        translatorOptions.serverUrl = apiEndpoint
        deepLApi = com.deepl.api.Translator(apiKey, translatorOptions)
    }

    override fun translate(texts: List<String>, srcLang: String, dstLang: String): List<String> {
        if (texts.isEmpty()) {
            return emptyList()
        }
        val translations: List<TextResult>
        try {
            val textTranslatorOptions = TextTranslationOptions()
            textTranslatorOptions.nonSplittingTags = EPubTranslatorServiceImpl.INLINE_ELEMENT_NAMES
            textTranslatorOptions.ignoreTags = IGNORE_ELEMENT_NAMES
            textTranslatorOptions.tagHandling = "xml"
            translations = deepLApi.translateText(texts, srcLang, dstLang, textTranslatorOptions)
            if (doLog) logger.info("DeepL Translated $texts -> $translations")
        } catch (e: DeepLException) {
            if (doLog) logger.info("DeepL failed with $e on input $texts")
            throw DeepLTranslatorException("DeepL error is thrown", e)
        }
        return translations.map { it.text }
    }

    companion object {
        private const val doLog = false
        private val IGNORE_ELEMENT_NAMES = listOf("abbr", "b", "cite", "code", "data", "dfn", "kbd", "rp", "rt", "rtc", "ruby", "samp", "time", "var")
    }

}