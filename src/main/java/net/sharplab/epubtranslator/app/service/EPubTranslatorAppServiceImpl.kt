package net.sharplab.epubtranslator.app.service

import net.sharplab.epubtranslator.core.driver.epub.EPubReader
import net.sharplab.epubtranslator.core.driver.epub.EPubWriter
import net.sharplab.epubtranslator.core.service.EPubTranslatorService
import org.slf4j.LoggerFactory
import java.io.File
import javax.enterprise.context.Dependent

@Dependent
class EPubTranslatorAppServiceImpl(private val ePubTranslatorService: EPubTranslatorService, private val ePubReader: EPubReader, private val ePubWriter: EPubWriter) : EPubTranslatorAppService {

    private val logger = LoggerFactory.getLogger(EPubTranslatorAppServiceImpl::class.java)

    override fun countCharacters(ePubTranslateParameters: EPubTranslateParameters) {
        val ePubFile = ePubReader.read(ePubTranslateParameters.srcFile)
        val countMap = ePubTranslatorService.countCharacters(ePubFile)
        logger.info("Total characters ${countMap.values.sum()}")
        logger.info("- Note that the total characters includes formatting tags which seem to not counted in DeepL usage limit.")
    }

    override fun translateEPubFile(ePubTranslateParameters: EPubTranslateParameters) {
        val ePubFile = ePubReader.read(ePubTranslateParameters.srcFile)
        val (translatedEpub, failure) =
            ePubTranslatorService.translate(
                ePubFile, ePubTranslateParameters.srcLang, ePubTranslateParameters.dstLang, ePubTranslateParameters.limitCredits, ePubTranslateParameters.abortOnError
            )

        val dstFile =
            failure?.let {
                val orgDst = ePubTranslateParameters.dstFile
                logger.warn("DeepL failed with reason: ${it.reason}")
                val newDstFile = File(orgDst.parentFile, "${orgDst.nameWithoutExtension}-failed-${it.contentFileName.filename}.${orgDst.extension}")
                logger.info("Written to output file: $newDstFile")
                newDstFile
            } ?: ePubTranslateParameters.dstFile

        ePubWriter.write(translatedEpub, dstFile)
    }

    // Removes extension and potentially illegal chars
    private val String.filename
        get() = split('.')[0]
            .replace("[^a-zA-Z\\d\\-]+".toRegex(), "_")

}
