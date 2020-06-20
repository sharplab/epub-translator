package net.sharplab.epubtranslator.app.config;

import net.sharplab.epubtranslator.app.EPubTranslatorSetting;
import net.sharplab.epubtranslator.core.driver.epub.EPubReader;
import net.sharplab.epubtranslator.core.driver.epub.EPubWriter;
import net.sharplab.epubtranslator.core.driver.translator.DeepLTranslator;
import net.sharplab.epubtranslator.core.driver.translator.Translator;
import net.sharplab.epubtranslator.core.provider.epub.DefaultEPubContentFileProvider;
import net.sharplab.epubtranslator.core.provider.epub.EPubChapterProvider;
import net.sharplab.epubtranslator.core.provider.epub.EPubContentFileProvider;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import java.util.Arrays;
import java.util.List;

@Dependent
public class AppConfig {

    private EPubTranslatorSetting ePubTranslatorSetting;

    public AppConfig(EPubTranslatorSetting ePubTranslatorSetting) {
        this.ePubTranslatorSetting = ePubTranslatorSetting;
    }

    @Produces
    Translator translator() {
        String apiKey = ePubTranslatorSetting.getDeepLApiKey();
        return new DeepLTranslator(apiKey);
    }

    @Produces
    EPubReader ePubReader() {
        List<EPubContentFileProvider> contentFileProviders = Arrays.asList(new EPubChapterProvider(), new DefaultEPubContentFileProvider());
        return new EPubReader(contentFileProviders);
    }

    @Produces
    EPubWriter ePubWriter() {
        return new EPubWriter();
    }
}
