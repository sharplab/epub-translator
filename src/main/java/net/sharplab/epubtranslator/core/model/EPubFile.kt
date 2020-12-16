package net.sharplab.epubtranslator.core.model

import java.util.*

/**
 * EPubFile
 */
class EPubFile(contentFiles: List<EPubContentFile>) {
    val contentFiles: List<EPubContentFile> = Collections.unmodifiableList(contentFiles)

}