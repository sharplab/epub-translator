package net.sharplab.epubtranslator.core.driver.translator;

import net.sharplab.epubtranslator.core.service.EPubTranslatorServiceImpl;
import org.apache.http.util.EntityUtils;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DeepLTranslator implements Translator {

    private static final List<String> IGNORE_ELEMENT_NAMES = List.of("abbr", "b", "cite", "code", "data", "dfn", "kbd", "rp", "rt", "rtc", "ruby", "samp", "time", "var");

    private final String apiKey;

    public DeepLTranslator(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public List<String> translate(List<String> texts, String srcLang, String dstLang) {
        if(texts.isEmpty()){
            return Collections.emptyList();
        }

        MultivaluedMap<String, String> map = new MultivaluedMapImpl<>();
        map.add("auth_key", apiKey);
        map.add("source_lang", srcLang);
        map.add("target_lang", dstLang);
        map.add("non_splitting_tags", String.join(",", EPubTranslatorServiceImpl.INLINE_ELEMENT_NAMES));
        map.add("ignore_tags", String.join(",", IGNORE_ELEMENT_NAMES));
        map.add("tag_handling", "xml");
        for (String text : texts) {
            map.add("text", text);
        }
        DeepLTranslateAPIResponse response;
        try(DeepLClient deepLClient = RestClientBuilder.newBuilder().baseUri(URI.create("https://api.deepl.com")).build(DeepLClient.class)){
            response = deepLClient.translate(map);
        }
        catch (WebApplicationException e){
            String message = String.format("%d error is thrown: %s", e.getResponse().getStatus(), e.getResponse().readEntity(String.class));
            throw new DeepLTranslatorException(message, e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return response.getTranslations().stream().map(DeepLTranslateAPIResponse.Translation::getText).collect(Collectors.toList());

    }
}
