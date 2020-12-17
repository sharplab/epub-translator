package net.sharplab.epubtranslator.core.service

import java.util.*

/**
 * TranslateRequestChunk
 */
class TranslateRequestChunk(translateRequests: List<TranslateRequest>) {
    val translateRequests: List<TranslateRequest> = Collections.unmodifiableList(translateRequests)
}
