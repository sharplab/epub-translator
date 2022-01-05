package net.sharplab.epubtranslator.app

import org.eclipse.microprofile.config.inject.ConfigProperty
import javax.enterprise.context.Dependent

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
    // gracefully quit discuss on issue #60 , defaulting to false due to backward compatibility concerns
    @ConfigProperty(name = "ePubTranslator.controlPanel.gracefulQuit")
    var gracefulQuit: Boolean? = false
    // skip error page discuss on issue #4, *higher priority then gracefully quit
    @ConfigProperty(name = "ePubTranslator.controlPanel.skipError")
    var skipError: Boolean? = false

}
