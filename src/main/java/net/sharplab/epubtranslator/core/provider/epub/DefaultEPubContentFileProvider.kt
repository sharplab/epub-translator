package net.sharplab.epubtranslator.core.provider.epub

import net.sharplab.epubtranslator.core.model.EPubContentFile
import net.sharplab.epubtranslator.core.model.FileEntry

/**
 * EPubContentFileを返却するプロバイダ
 */
class DefaultEPubContentFileProvider : EPubContentFileProvider {
    /**
     * {@inheritDoc}
     */
    override fun canHandle(fileEntry: FileEntry): Boolean {
        return true
    }

    /**
     * {@inheritDoc}
     */
    override fun provide(fileEntry: FileEntry): EPubContentFile {
        require(canHandle(fileEntry))
        return EPubContentFile(fileEntry.name, fileEntry.data)
    }
}