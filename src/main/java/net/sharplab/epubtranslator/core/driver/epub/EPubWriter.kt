package net.sharplab.epubtranslator.core.driver.epub

import net.sharplab.epubtranslator.core.model.EPubFile
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.CRC32

class EPubWriter {
    fun write(ePubFile: EPubFile, dstFile: File) {
        FileOutputStream(dstFile).use { fileOutputStream ->
            ZipOutputStream(fileOutputStream).use { zipOutputStream ->
                val mimetypeEntry = ZipEntry("mimetype")

                mimetypeEntry.method = ZipEntry.STORED
                val mimetypeBytes = "application/epub+zip".toByteArray()
                mimetypeEntry.size = mimetypeBytes.size.toLong()

                val crc = CRC32()
                crc.update(mimetypeBytes, 0, mimetypeBytes.size)
                mimetypeEntry.crc = crc.value

                zipOutputStream.putNextEntry(mimetypeEntry)
                zipOutputStream.write("application/epub+zip".toByteArray())

                ePubFile.contentFiles.forEach {
                    if (it.name != "mimetype") { // mimetypeはすでに追加されているのでスキップ
                        val zipEntry = ZipEntry(it.name)
                        zipOutputStream.putNextEntry(zipEntry)
                        zipOutputStream.write(it.data)
                    }
                }
            }
        }
    }
}