package net.sharplab.epubtranslator.app.service

import net.sharplab.epubtranslator.core.driver.epub.EPubReader
import net.sharplab.epubtranslator.core.driver.epub.EPubWriter
import net.sharplab.epubtranslator.core.service.EPubTranslatorService
import javax.enterprise.context.Dependent

@Dependent
class EPubTranslatorAppServiceImpl(private val ePubTranslatorService: EPubTranslatorService, private val ePubReader: EPubReader, private val ePubWriter: EPubWriter) : EPubTranslatorAppService {
    override fun translateEPubFile(ePubTranslateParameters: EPubTranslateParameters) {
        val ePubFile = ePubReader.read(ePubTranslateParameters.srcFile)
        val translated = ePubTranslatorService.translate(ePubFile, ePubTranslateParameters.srcLang, ePubTranslateParameters.dstLang)
        ePubWriter.write(translated, ePubTranslateParameters.dstFile)
    }

}
