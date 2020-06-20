package net.sharplab.epubtranslator.core.model;

/**
 * EPubContentFile
 */
public class EPubContentFile {

    private final String name;
    private final byte[] data;

    public EPubContentFile(String name, byte[] data){
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

}
