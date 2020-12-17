package net.sharplab.epubtranslator.core.provider.epub

import net.sharplab.epubtranslator.core.model.EPubChapter
import net.sharplab.epubtranslator.core.model.FileEntry

/**
 * EPubChapterを生成するプロバイダ
 */
class EPubChapterProvider : EPubContentFileProvider {
    override fun canHandle(fileEntry: FileEntry): Boolean {
        return fileEntry.name.endsWith(".xhtml") || fileEntry.name.endsWith(".html") || fileEntry.name.endsWith(".htm")
    }

    override fun provide(fileEntry: FileEntry): EPubChapter {
        require(canHandle(fileEntry))
        return EPubChapter(fileEntry.name, fileEntry.data)
    }
}
