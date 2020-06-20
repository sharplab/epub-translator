package net.sharplab.epubtranslator.core.driver.translator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DeepLTranslationResponse {

    private final List<Translation> translations;

    @JsonCreator
    public DeepLTranslationResponse(@JsonProperty("translations") List<Translation> translations) {
        this.translations = translations;
    }

    @JsonGetter("translations")
    public List<Translation> getTranslations() {
        return translations;
    }

    static class Translation {

        private final String text;
        private final String detectedSourceLanguage;

        @JsonCreator
        public Translation(@JsonProperty("text") String text, @JsonProperty("detected_source_language") String detectedSourceLanguage) {
            this.text = text;
            this.detectedSourceLanguage = detectedSourceLanguage;
        }

        @JsonGetter("text")
        public String getText() {
            return text;
        }

        @JsonGetter("detected_source_language")
        public String getDetectedSourceLanguage() {
            return detectedSourceLanguage;
        }
    }
}