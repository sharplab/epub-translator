package net.sharplab.epubtranslator.core.provider.epub;

import net.sharplab.epubtranslator.core.model.EPubContentFile;
import net.sharplab.epubtranslator.core.model.FileEntry;


/**
 * EPubContentFileを返却するプロバイダ
 */
public class DefaultEPubContentFileProvider implements EPubContentFileProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canHandle(FileEntry fileEntry) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EPubContentFile provide(FileEntry fileEntry) {
        if (!canHandle(fileEntry)) {
            throw new IllegalArgumentException();
        }
        return new EPubContentFile(fileEntry.getName(), fileEntry.getData());
    }
}
