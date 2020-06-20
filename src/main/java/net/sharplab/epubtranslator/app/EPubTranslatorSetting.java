package net.sharplab.epubtranslator.app;


import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;

@Dependent
public class EPubTranslatorSetting {

    @ConfigProperty(name = "ePubTranslator.deepL.apiKey")
    String deepLApiKey;

    @ConfigProperty(name = "ePubTranslator.language.source")
    String defaultSrcLang;
    @ConfigProperty(name = "ePubTranslator.language.destination")
    String defaultDstLang;

    public String getDeepLApiKey() {
        return deepLApiKey;
    }

    public String getDefaultSrcLang() {
        return defaultSrcLang;
    }

    public String getDefaultDstLang() {
        return defaultDstLang;
    }
}
