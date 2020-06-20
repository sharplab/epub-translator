package net.sharplab.epubtranslator.core.model;

import java.util.Collections;
import java.util.List;

/**
 * EPubFile
 */
public class EPubFile {

    private final List<EPubContentFile> contentFiles;

    public EPubFile(List<EPubContentFile> contentFiles){
        this.contentFiles = Collections.unmodifiableList(contentFiles);
    }

    public List<EPubContentFile> getContentFiles() {
        return contentFiles;
    }
}
