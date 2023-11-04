package net.sharplab.epubtranslator.app

import org.eclipse.microprofile.config.inject.ConfigProperty
import jakarta.enterprise.context.Dependent

@Dependent
class EPubTranslatorSetting {
    @ConfigProperty(name = "ePubTranslator.deepL.apiEndpoint", defaultValue = "https://api.deepl.com")
    var deepLApiEndpoint: String? = null
    @ConfigProperty(name = "ePubTranslator.deepL.apiKey")
    var deepLApiKey: String? = null
    @ConfigProperty(name = "ePubTranslator.language.source", defaultValue = "en")
    var defaultSrcLang: String? = null
    @ConfigProperty(name = "ePubTranslator.language.destination", defaultValue = "ja")
    var defaultDstLang: String? = null

}
