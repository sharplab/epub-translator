package net.sharplab.epubtranslator.core.driver.translator

import net.sharplab.epubtranslator.core.service.EPubTranslatorServiceImpl
import org.eclipse.microprofile.rest.client.RestClientBuilder
import org.jboss.resteasy.specimpl.MultivaluedMapImpl
import org.slf4j.LoggerFactory
import java.net.URI
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MultivaluedMap

class DeepLTranslator(private val apiEndpoint: String, private val apiKey: String) : Translator {

    private val logger = LoggerFactory.getLogger(DeepLTranslator::class.java)

    // recoverable errors, we can do some retry
    private val retryableErrors  = listOf(DeepLStatusCode.TOO_MANY_REQUESTS.code)

    // default cool down interval, no need to put it into configure for now
    private var coolDownInterval = 30000L

    override fun translate(texts: List<String>, srcLang: String, dstLang: String): List<String> {
        if (texts.isEmpty()) {
            return emptyList()
        }
        val map: MultivaluedMap<String, String> = MultivaluedMapImpl()
        map.add("auth_key", apiKey)
        map.add("source_lang", srcLang)
        map.add("target_lang", dstLang)
        map.add("non_splitting_tags", java.lang.String.join(",", EPubTranslatorServiceImpl.INLINE_ELEMENT_NAMES))
        map.add("ignore_tags", java.lang.String.join(",", IGNORE_ELEMENT_NAMES))
        map.add("tag_handling", "xml")
        for (text in texts) {
            map.add("text", text)
        }
        var retryTimes = 3
        while (--retryTimes > 0) {
            try {
                val deepLClient = RestClientBuilder.newBuilder().baseUri(URI.create(apiEndpoint)).build(DeepLClient::class.java)
                val response = deepLClient.translate(map)

                return response.translations.map(DeepLTranslateAPIResponse.Translation::text)
            } catch (e: WebApplicationException) {
                val statusCode = e.response.status
                // should try again on recoverable errors (like 429)
                if(retryableErrors.contains(statusCode)) {
                    // if it's code 429 means request to quick
                    if(DeepLStatusCode.TOO_MANY_REQUESTS.code == statusCode) {
                        logger.info("request too quick, wait for {} ms", coolDownInterval)
                        Thread.sleep(coolDownInterval)
                    }
                    // if we would be dealing with more error here, we may want to refactor this using factory design-pattern
                    logger.info("retry on error {}", e.response.readEntity(String::class.java))
                } else {
                    // else let it crash
                    val message = String.format("%d error is thrown: %s", statusCode, e.response.readEntity(String::class.java))
                    throw DeepLTranslatorException(message, e)
                }
            }
        }
        return listOf()
    }

    companion object {
        private val IGNORE_ELEMENT_NAMES = java.util.List.of("abbr", "b", "cite", "code", "data", "dfn", "kbd", "rp", "rt", "rtc", "ruby", "samp", "time", "var")
    }

}