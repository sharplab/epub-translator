package net.sharplab.epubtranslator.core.model

import net.sharplab.epubtranslator.core.util.ArrayUtil.clone

class FileEntry(val name: String, data: ByteArray) {

    val data: ByteArray = clone(data)
        get() = clone(field)

}
