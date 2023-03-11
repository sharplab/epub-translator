package net.sharplab.epubtranslator.app

import org.eclipse.microprofile.config.inject.ConfigProperty
import javax.enterprise.context.Dependent

@Suppress("CdiInjectInspection") // We have to initialize fields in kotlin, but IntelliJ warns due to CDI rules
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
    @ConfigProperty(name = "ePubTranslator.output.applyPrefix", defaultValue = "false")
    var outputApplyTranslatedPrefix: Boolean = false
    @ConfigProperty(name = "ePubTranslator.output.translatedPrefix", defaultValue = "")
    var outputTranslatedPrefix: String? = null

}
