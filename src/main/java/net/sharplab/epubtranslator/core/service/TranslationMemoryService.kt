package net.sharplab.epubtranslator.core.service

interface TranslationMemoryService {

    fun load(source: String, sourceLang: String, targetLang: String): String?
    fun save(source: String, target:String, sourceLang: String, targetLang: String)
}