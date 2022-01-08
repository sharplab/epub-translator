package net.sharplab.epubtranslator.core.driver.translator

import net.sharplab.deepl4j.DeepLApi
import net.sharplab.deepl4j.DeepLApiFactory
import net.sharplab.deepl4j.client.ApiException
import net.sharplab.deepl4j.model.Translations
import net.sharplab.epubtranslator.core.service.EPubTranslatorServiceImpl
import javax.ws.rs.WebApplicationException

class DeepLTranslator(apiEndpoint: String, apiKey: String) : Translator {

    private val deepLApi : DeepLApi;

    init {
        deepLApi = DeepLApiFactory().create(apiKey)
        deepLApi.apiClient.servers.first().URL = apiEndpoint
    }

    override fun translate(texts: List<String>, srcLang: String, dstLang: String): List<String> {
        if (texts.isEmpty()) {
            return emptyList()
        }
        val nonSplittingTags = java.lang.String.join(",", EPubTranslatorServiceImpl.INLINE_ELEMENT_NAMES)
        val ignoreTags = java.lang.String.join(",", IGNORE_ELEMENT_NAMES)
        val translations: Translations
        try {
            translations = when (texts.size) {
                1 -> deepLApi.translateText(texts.first(), srcLang, dstLang, null, null, null, null, "xml", nonSplittingTags, null, null, ignoreTags)
                else -> deepLApi.translateTexts(texts, srcLang, dstLang, null, null, null, null, "xml", nonSplittingTags, null, null, ignoreTags)
            }
        } catch (e: ApiException) {
            val message = String.format("%d error is thrown: %s", e.code, e.responseBody)
            throw DeepLTranslatorException(message, e)
        }
        return translations.translations.map{ it.text }
    }

    companion object {
        private val IGNORE_ELEMENT_NAMES = listOf("abbr", "b", "cite", "code", "data", "dfn", "kbd", "rp", "rt", "rtc", "ruby", "samp", "time", "var")
    }

}