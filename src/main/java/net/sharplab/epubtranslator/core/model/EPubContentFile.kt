package net.sharplab.epubtranslator.core.model

import net.sharplab.epubtranslator.core.util.ArrayUtil

/**
 * EPubContentFile
 */
open class EPubContentFile(val name: String, data: ByteArray) {

    val data: ByteArray = ArrayUtil.clone(data)
        get() = ArrayUtil.clone(field)

}