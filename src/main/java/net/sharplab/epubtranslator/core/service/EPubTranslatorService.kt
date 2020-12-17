package net.sharplab.epubtranslator.core.service

import net.sharplab.epubtranslator.core.model.EPubFile

interface EPubTranslatorService {
    fun translate(ePubFile: EPubFile, srcLang: String, dstLang: String): EPubFile
}
