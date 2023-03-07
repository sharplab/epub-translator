package net.sharplab.epubtranslator.app.service

interface EPubTranslatorAppService {
    fun countCharacters(ePubTranslateParameters: EPubTranslateParameters)
    fun translateEPubFile(ePubTranslateParameters: EPubTranslateParameters)
}