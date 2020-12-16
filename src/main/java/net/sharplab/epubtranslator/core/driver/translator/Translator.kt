package net.sharplab.epubtranslator.core.driver.translator

interface Translator {
    fun translate(texts: List<String>, srcLang: String, dstLang: String): List<String>
}
