package net.sharplab.epubtranslator.app.cli

import net.sharplab.epubtranslator.app.EPubTranslatorSetting
import net.sharplab.epubtranslator.app.service.EPubTranslateParameters
import net.sharplab.epubtranslator.app.service.EPubTranslatorAppService
import picocli.CommandLine
import java.io.File
import java.lang.IllegalArgumentException

@CommandLine.Command
class EPubTranslatorCli(private val ePubTranslatorAppService: EPubTranslatorAppService, private val ePubTranslatorSetting: EPubTranslatorSetting) : Runnable {
    @CommandLine.Option(order = 0, names = ["--src"], description = ["source file"], required = true)
    private var src: File? = null
    @CommandLine.Option(order = 1, names = ["--dst"], description = ["destination file"])
    private var dst: File? = null
    @CommandLine.Option(order = 2, names = ["--srcLang"], description = ["source language"])
    private var srcLang: String? = null
    @CommandLine.Option(order = 3, names = ["--dstLang"], description = ["destination language"])
    private var dstLang: String? = null
    @CommandLine.Option(order = 9, names = ["--help", "-h"], description = ["print help"], usageHelp = true)
    private var help = false

    override fun run() {
        val srcFile = src?: throw IllegalArgumentException("src must be provided")
        val resolvedSrcLang = srcLang ?: ePubTranslatorSetting.defaultSrcLang ?: throw IllegalArgumentException("srcLang must be provided")
        val resolvedDstLang = dstLang ?: ePubTranslatorSetting.defaultDstLang ?: throw IllegalArgumentException("dstLang must be provided")
        val resolvedDst = dst ?: constructDstFileFromSrcFile(srcFile, resolvedDstLang)
        val ePubTranslateParameters = EPubTranslateParameters(srcFile, resolvedDst, resolvedSrcLang, resolvedDstLang)
        ePubTranslatorAppService.translateEPubFile(ePubTranslateParameters)
    }

    private fun constructDstFileFromSrcFile(src: File, dstLang: String): File {
        val srcFileName = src.name
        val dstFileName: String
        dstFileName = if (!srcFileName.contains(".")) {
            "$srcFileName.$dstLang"
        } else {
            srcFileName.substring(0, srcFileName.lastIndexOf('.')) + "." + dstLang + srcFileName.substring(srcFileName.lastIndexOf('.'), srcFileName.length)
        }
        return File(src.parent, dstFileName)
    }

}