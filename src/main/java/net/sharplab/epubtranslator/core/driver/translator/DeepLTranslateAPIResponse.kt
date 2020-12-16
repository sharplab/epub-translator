package net.sharplab.epubtranslator.core.driver.translator

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonProperty

@Suppress("ConvertSecondaryConstructorToPrimary")
class DeepLTranslateAPIResponse {

    @JsonCreator
    constructor(@JsonProperty("translations") translations: List<Translation>) {
        this.translations = translations
    }

    val translations: List<Translation>
        @JsonGetter("translations")
        get

    class Translation {

        val detectedSourceLanguage: String
            @JsonGetter("detected_source_language")
            get

        val text: String
            @JsonGetter("text")
            get

        @JsonCreator
        constructor(@JsonProperty("text") text: String, @JsonProperty("detected_source_language") detectedSourceLanguage: String) {
            this.detectedSourceLanguage = detectedSourceLanguage
            this.text = text
        }

    }

}
