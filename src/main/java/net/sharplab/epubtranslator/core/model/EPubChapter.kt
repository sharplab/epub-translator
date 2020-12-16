package net.sharplab.epubtranslator.core.model

import java.nio.charset.StandardCharsets

/**
 * EPubChapter
 */
class EPubChapter(name: String, data: ByteArray) : EPubContentFile(name, data) {
    val dataAsString: String
        get() = String(data, StandardCharsets.UTF_8)
}
