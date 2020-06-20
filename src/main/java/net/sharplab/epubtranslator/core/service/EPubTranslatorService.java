package net.sharplab.epubtranslator.core.service;

import net.sharplab.epubtranslator.core.model.EPubFile;

import javax.enterprise.context.Dependent;

public interface EPubTranslatorService {

    EPubFile translate(EPubFile ePubFile, String srcLang, String dstLang);

}
