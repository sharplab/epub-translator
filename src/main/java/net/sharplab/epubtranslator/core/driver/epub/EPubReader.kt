package net.sharplab.epubtranslator.core.driver.epub

import net.sharplab.epubtranslator.core.model.EPubContentFile
import net.sharplab.epubtranslator.core.model.EPubFile
import net.sharplab.epubtranslator.core.model.FileEntry
import net.sharplab.epubtranslator.core.provider.epub.ContentFileProviderNotFoundException
import net.sharplab.epubtranslator.core.provider.epub.EPubContentFileProvider
import java.io.*
import java.util.*
import java.util.zip.ZipInputStream

class EPubReader(ePubContentFileProviders: List<EPubContentFileProvider>) {

    private var ePubContentFileProviders: List<EPubContentFileProvider> = Collections.unmodifiableList(ePubContentFileProviders)

    fun read(srcFile: File): EPubFile {
        val contentFiles: MutableList<EPubContentFile> = LinkedList()
        ZipInputStream(FileInputStream(srcFile)).use {
            var zipEntry = it.nextEntry
            while (zipEntry != null) {
                if (zipEntry.isDirectory) {
                    zipEntry = it.nextEntry
                    continue
                }
                val data = readZipEntry(it)
                val fileEntry = FileEntry(zipEntry.name, data)
                contentFiles.add(createEPubContentFile(fileEntry))
                zipEntry = it.nextEntry
            }
            return EPubFile(contentFiles)
        }
    }

    private fun createEPubContentFile(fileEntry: FileEntry): EPubContentFile {
        ePubContentFileProviders.forEach {
            if (it.canHandle(fileEntry)) {
                return it.provide(fileEntry)
            }
        }
        throw ContentFileProviderNotFoundException()
    }

    private fun readZipEntry(zipInputStream: ZipInputStream): ByteArray {
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        ByteArrayOutputStream().use { byteArrayOutputStream ->
            while (true) {
                val readSize = zipInputStream.read(buffer, 0, buffer.size)
                if (readSize < 0) {
                    break
                }
                byteArrayOutputStream.write(buffer, 0, readSize)
            }
            return byteArrayOutputStream.toByteArray()
        }
    }

}