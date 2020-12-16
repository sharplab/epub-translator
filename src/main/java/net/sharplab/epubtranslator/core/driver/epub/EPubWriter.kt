package net.sharplab.epubtranslator.core.driver.epub

import net.sharplab.epubtranslator.core.model.EPubFile
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class EPubWriter {
    fun write(ePubFile: EPubFile, dstFile: File) {
        FileOutputStream(dstFile).use { fileOutputStream ->
            ZipOutputStream(fileOutputStream).use { zipOutputStream ->
                ePubFile.contentFiles.forEach {
                    val zipEntry = ZipEntry(it.name)
                    zipOutputStream.putNextEntry(zipEntry)
                    zipOutputStream.write(it.data)
                }
            }
        }
    }
}