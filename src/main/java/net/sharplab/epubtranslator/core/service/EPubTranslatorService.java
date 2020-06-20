package net.sharplab.epubtranslator.core.service;

import net.sharplab.epubtranslator.core.model.EPubFile;

public interface EPubTranslatorService {

    EPubFile translate(EPubFile ePubFile, String srcLang, String dstLang);

}
