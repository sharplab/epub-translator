package net.sharplab.epubtranslator.core.provider.epub;

import net.sharplab.epubtranslator.core.model.EPubChapter;
import net.sharplab.epubtranslator.core.model.FileEntry;

/**
 * EPubChapterを生成するプロバイダ
 */
public class EPubChapterProvider implements EPubContentFileProvider {

    @Override
    public boolean canHandle(FileEntry fileEntry) {
        return fileEntry.getName().endsWith(".xhtml") || fileEntry.getName().endsWith(".html")  || fileEntry.getName().endsWith(".htm");
    }

    @Override
    public EPubChapter provide(FileEntry fileEntry) {
        if(!canHandle(fileEntry)){
            throw new IllegalArgumentException();
        }
        return new EPubChapter(fileEntry.getName(), fileEntry.getData());
    }
}
