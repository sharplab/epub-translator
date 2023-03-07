package net.sharplab.epubtranslator.core.service

data class TranslationFailure(
    val exception: Exception,
    val reason: String?,
    val contentFileName: String
)
