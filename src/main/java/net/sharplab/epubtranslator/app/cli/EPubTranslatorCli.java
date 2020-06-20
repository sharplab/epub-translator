package net.sharplab.epubtranslator.app.cli;

import net.sharplab.epubtranslator.app.EPubTranslatorSetting;
import net.sharplab.epubtranslator.app.service.EPubTranslateParameters;
import net.sharplab.epubtranslator.app.service.EPubTranslatorAppService;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command
public class EPubTranslatorCli implements Runnable {

    @CommandLine.Option(order = 0, names = {"--src"}, description = "source file", required = true)
    private File src;

    @CommandLine.Option(order = 1, names = {"--dst"}, description = "destination file")
    private File dst;

    @CommandLine.Option(order = 2, names = {"--srcLang"}, description = "source language")
    private String srcLang;

    @CommandLine.Option(order = 3, names = {"--dstLang"}, description = "destination language")
    private String dstLang;

    @CommandLine.Option(order = 9, names = {"--help", "-h"}, description = "print help", usageHelp = true)
    private boolean help;

    private final EPubTranslatorAppService ePubTranslatorAppService;
    private final EPubTranslatorSetting ePubTranslatorSetting;

    public EPubTranslatorCli(EPubTranslatorAppService ePubTranslatorAppService, EPubTranslatorSetting ePubTranslatorSetting) {
        this.ePubTranslatorAppService = ePubTranslatorAppService;
        this.ePubTranslatorSetting = ePubTranslatorSetting;
    }

    @Override
    public void run() {
        String resolvedSrcLang = this.srcLang == null ? ePubTranslatorSetting.getDefaultSrcLang() : this.srcLang;
        String resolvedDstLang = this.dstLang == null ? ePubTranslatorSetting.getDefaultDstLang() : this.dstLang;
        File resolvedDst = this.dst == null ? constructDstFileFromSrcFile(src, resolvedDstLang) : this.dst;

        EPubTranslateParameters ePubTranslateParameters = new EPubTranslateParameters(src, resolvedDst, resolvedSrcLang, resolvedDstLang);
        ePubTranslatorAppService.translateEPubFile(ePubTranslateParameters);
    }

    File constructDstFileFromSrcFile(File src, String dstLang){
        String srcFileName = src.getName();
        String dstFileName;
        if(!srcFileName.contains(".")) {
            dstFileName = srcFileName + "." + dstLang;
        }
        else {
            dstFileName = srcFileName.substring(0, srcFileName.lastIndexOf(".")) + "." + dstLang + srcFileName.substring(srcFileName.lastIndexOf("."), srcFileName.length());
        }
        return new File(src.getParent(), dstFileName);
    }
}
