package net.sharplab.epubtranslator.core.service

import java.util.*

/**
 * TranslateRequestChunk
 */
class TranslationRequestChunk(translationRequests: List<TranslationRequest>) {
    val translationRequests: List<TranslationRequest> = Collections.unmodifiableList(translationRequests)
}
