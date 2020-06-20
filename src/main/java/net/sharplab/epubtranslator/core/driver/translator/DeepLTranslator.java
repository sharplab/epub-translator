package net.sharplab.epubtranslator.core.driver.translator;

import net.sharplab.epubtranslator.core.service.EPubTranslatorServiceImpl;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;
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

        DeepLTranslatorClient deepLTranslatorClient = RestClientBuilder.newBuilder().baseUri(URI.create("https://api.deepl.com")).build(DeepLTranslatorClient.class);
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
        return deepLTranslatorClient.translate(map).getTranslations().stream().map(DeepLTranslationResponse.Translation::getText).collect(Collectors.toList());

    }
}
