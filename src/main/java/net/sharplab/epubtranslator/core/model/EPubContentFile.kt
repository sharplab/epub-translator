package net.sharplab.epubtranslator.core.model

import net.sharplab.epubtranslator.core.util.ArrayUtil.clone

/**
 * EPubContentFile
 */
open class EPubContentFile(val name: String, data: ByteArray) {

    val data: ByteArray = clone(data)
        get() = clone(field)

}