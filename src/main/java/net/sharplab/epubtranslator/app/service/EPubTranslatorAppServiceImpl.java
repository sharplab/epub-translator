package net.sharplab.epubtranslator.app.service;

import net.sharplab.epubtranslator.core.driver.epub.EPubReader;
import net.sharplab.epubtranslator.core.driver.epub.EPubWriter;
import net.sharplab.epubtranslator.core.model.EPubFile;
import net.sharplab.epubtranslator.core.service.EPubTranslatorService;

import javax.enterprise.context.Dependent;

@Dependent
public class EPubTranslatorAppServiceImpl implements EPubTranslatorAppService {

    private final EPubTranslatorService ePubTranslatorService;
    private final EPubReader ePubReader;
    private final EPubWriter ePubWriter;

    public EPubTranslatorAppServiceImpl(EPubTranslatorService ePubTranslatorService, EPubReader ePubReader, EPubWriter ePubWriter) {
        this.ePubTranslatorService = ePubTranslatorService;
        this.ePubReader = ePubReader;
        this.ePubWriter = ePubWriter;
    }

    @Override
    public void translateEPubFile(EPubTranslateParameters ePubTranslateParameters){
        EPubFile ePubFile = ePubReader.read(ePubTranslateParameters.srcFile);
        EPubFile translated = ePubTranslatorService.translate(ePubFile, ePubTranslateParameters.getSrcLang(), ePubTranslateParameters.getDstLang());
        ePubWriter.write(translated, ePubTranslateParameters.dstFile);
    }
}
