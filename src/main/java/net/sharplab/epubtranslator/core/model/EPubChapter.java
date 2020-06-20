package net.sharplab.epubtranslator.core.model;

import java.nio.charset.StandardCharsets;

/**
 * EPubChapter
 */
public class EPubChapter extends EPubContentFile {

    public EPubChapter(String name, byte[] data) {
        super(name, data);
    }

    public String getDataAsString() {
        return new String(getData(), StandardCharsets.UTF_8);
    }
}
