package net.sharplab.epubtranslator.core.model;

import net.sharplab.epubtranslator.core.util.ArrayUtil;

/**
 * EPubContentFile
 */
public class EPubContentFile {

    private final String name;
    private final byte[] data;

    public EPubContentFile(String name, byte[] data){
        this.name = name;
        this.data = ArrayUtil.clone(data);
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return ArrayUtil.clone(data);
    }

}
