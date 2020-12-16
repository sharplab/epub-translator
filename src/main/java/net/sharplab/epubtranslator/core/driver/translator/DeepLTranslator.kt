package net.sharplab.epubtranslator.core.driver.translator

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import net.sharplab.epubtranslator.core.service.EPubTranslatorServiceImpl
import org.apache.http.HttpStatus
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.charset.StandardCharsets
import java.util.*

class DeepLTranslator(private val apiKey: String) : Translator {

    private val logger = LoggerFactory.getLogger(DeepLTranslator::class.java)

    override fun translate(texts: List<String>, srcLang: String, dstLang: String): List<String> {
        HttpClients.createDefault().use { httpClient ->
            val httpPost = HttpPost(ENDPOINT)
            val parameters: MutableList<NameValuePair> = ArrayList()
            parameters.add(BasicNameValuePair("auth_key", apiKey))
            parameters.add(BasicNameValuePair("source_lang", srcLang))
            parameters.add(BasicNameValuePair("target_lang", dstLang))
            parameters.add(BasicNameValuePair("non_splitting_tags", java.lang.String.join(",", EPubTranslatorServiceImpl.INLINE_ELEMENT_NAMES)))
            parameters.add(BasicNameValuePair("ignore_tags", java.lang.String.join(",", IGNORE_ELEMENT_NAMES)))
            parameters.add(BasicNameValuePair("tag_handling", "xml"))
            for (text in texts) {
                parameters.add(BasicNameValuePair("text", text))
            }
            val entity = UrlEncodedFormEntity(parameters, "UTF-8")
            httpPost.entity = entity
            try {
                httpClient.execute(httpPost).use { httpResponse ->
                    return if (httpResponse.statusLine.statusCode == HttpStatus.SC_OK) {
                        val objectMapper = ObjectMapper()
                        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        val body = EntityUtils.toString(httpResponse.entity, StandardCharsets.UTF_8)
                        val response = objectMapper.readValue(body, DeepLTranslationResponse::class.java)
                        Objects.requireNonNull(response).translations.map{ it.text }
                    } else {
                        val body = EntityUtils.toString(httpResponse.entity, StandardCharsets.UTF_8)
                        logger.error("Error returned from DeepL API: {} {}", httpResponse.statusLine.toString(), body)
                        throw DeepLTranslatorException() //TODO
                    }
                }
            } catch (e: IOException) {
                throw UncheckedIOException(e)
            }
        }
    }

    @Suppress("ConvertSecondaryConstructorToPrimary", "unused")
    internal class DeepLTranslationResponse {

        val translations: List<Translation>
            @JsonGetter("translations")
            get

        @JsonCreator
        constructor(@JsonProperty("translations") translations: List<Translation>) {
            this.translations = translations
        }

        internal class Translation {
            @JsonCreator
            constructor(@JsonProperty("text") text: String) {
                this.text = text
            }

            val text: String
                @JsonGetter("text")
                get
        }

    }

    companion object {
        private const val ENDPOINT = "https://api.deepl.com/v2/translate"
        private val IGNORE_ELEMENT_NAMES = listOf("abbr", "b", "cite", "code", "data", "dfn", "kbd", "rp", "rt", "rtc", "ruby", "samp", "time", "var")
    }

}