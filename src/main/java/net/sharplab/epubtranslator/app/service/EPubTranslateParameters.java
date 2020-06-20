package net.sharplab.epubtranslator.app.service;

import java.io.File;

public class EPubTranslateParameters {

    File srcFile;
    File dstFile;
    String srcLang;
    String dstLang;

    public EPubTranslateParameters(File srcFile, File dstFile, String srcLang, String dstLang) {
        this.srcFile = srcFile;
        this.dstFile = dstFile;
        this.srcLang = srcLang;
        this.dstLang = dstLang;
    }

    public File getSrcFile() {
        return srcFile;
    }

    public File getDstFile() {
        return dstFile;
    }

    public String getSrcLang() {
        return srcLang;
    }

    public String getDstLang() {
        return dstLang;
    }


}
