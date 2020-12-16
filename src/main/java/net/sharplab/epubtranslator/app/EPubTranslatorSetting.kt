package net.sharplab.epubtranslator.app

import org.eclipse.microprofile.config.inject.ConfigProperty
import javax.enterprise.context.Dependent

@Dependent
class EPubTranslatorSetting {
    @ConfigProperty(name = "ePubTranslator.deepL.apiKey")
    var deepLApiKey: String? = null

    @ConfigProperty(name = "ePubTranslator.language.source")
    var defaultSrcLang: String? = null

    @ConfigProperty(name = "ePubTranslator.language.destination")
    var defaultDstLang: String? = null

}
