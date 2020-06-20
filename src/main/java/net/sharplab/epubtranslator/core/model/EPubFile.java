package net.sharplab.epubtranslator.core.model;

import java.util.List;

/**
 * EPubFile
 */
public class EPubFile {

    private final List<EPubContentFile> contentFiles;

    public EPubFile(List<EPubContentFile> contentFiles){
        this.contentFiles = contentFiles;
    }

    public List<EPubContentFile> getContentFiles() {
        return contentFiles;
    }
}
