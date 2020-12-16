package net.sharplab.epubtranslator.core.model

import net.sharplab.epubtranslator.core.util.ArrayUtil

class FileEntry(val name: String, data: ByteArray) {

    val data: ByteArray = ArrayUtil.clone(data)
        get() = ArrayUtil.clone(field)

}