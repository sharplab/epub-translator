package net.sharplab.epubtranslator.core.service

import net.sharplab.epubtranslator.core.model.EPubFile

interface EPubTranslatorService {
    fun countCharacters(ePubFile: EPubFile): Map<String, Int>
    fun translate(ePubFile: EPubFile, srcLang: String, dstLang: String): Pair<EPubFile, TranslationFailure?>
}
