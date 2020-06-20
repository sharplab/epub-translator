package net.sharplab.epubtranslator.core.driver.translator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sharplab.epubtranslator.core.service.EPubTranslatorServiceImpl;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class DeepLTranslator implements Translator {

    private final Logger logger = LoggerFactory.getLogger(DeepLTranslator.class);

    private static final String ENDPOINT = "https://api.deepl.com/v2/translate";

    private final static List<String> IGNORE_ELEMENT_NAMES = Collections.unmodifiableList(Arrays.asList(
            "abbr", "b", "cite", "code", "data", "dfn",
            "kbd", "rp", "rt", "rtc", "ruby", "samp", "time", "var"));

    private String apiKey;

    public DeepLTranslator(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public List<String> translate(List<String> texts, String srcLang, String dstLang) {

        try(CloseableHttpClient httpClient = HttpClients.createDefault(); ) {
            HttpPost httpPost = new HttpPost(ENDPOINT);
            List<NameValuePair> parameters = new ArrayList<>();
            parameters.add(new BasicNameValuePair("auth_key", apiKey));
            parameters.add(new BasicNameValuePair("source_lang", srcLang));
            parameters.add(new BasicNameValuePair("target_lang", dstLang));
            parameters.add(new BasicNameValuePair("non_splitting_tags", String.join(",", EPubTranslatorServiceImpl.INLINE_ELEMENT_NAMES)));
            parameters.add(new BasicNameValuePair("ignore_tags", String.join(",", IGNORE_ELEMENT_NAMES)));
            parameters.add(new BasicNameValuePair("tag_handling", "xml"));
            for (String text: texts) {
                parameters.add(new BasicNameValuePair("text", text));
            }
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters, "UTF-8");
            httpPost.setEntity(entity);
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost); ) {
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    String body = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
                    DeepLTranslationResponse response = objectMapper.readValue(body, DeepLTranslationResponse.class);
                    return Objects.requireNonNull(response).getTranslations().stream().map(DeepLTranslationResponse.Translation::getText).collect(Collectors.toList());
                }
                else {
                    String body = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
                    logger.error("Error returned from DeepL API: {} {}", httpResponse.getStatusLine().toString(), body);
                    throw new DeepLTranslatorException(); //TODO
                }
            }
            catch(IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        catch (IOException e){
            throw new UncheckedIOException(e);
        }
    }

    static class DeepLTranslationResponse {

        private List<Translation> translations;

        @JsonCreator
        public DeepLTranslationResponse(@JsonProperty("translations") List<Translation> translations) {
            this.translations = translations;
        }

        @JsonGetter("translations")
        public List<Translation> getTranslations() {
            return translations;
        }

        static class Translation {

            private String text;

            @JsonCreator
            public Translation(@JsonProperty("text") String text) {
                this.text = text;
            }

            @JsonGetter("text")
            public String getText() {
                return text;
            }
        }
    }
}
