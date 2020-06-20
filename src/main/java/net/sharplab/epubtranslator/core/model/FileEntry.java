package net.sharplab.epubtranslator.core.model;


import net.sharplab.epubtranslator.core.util.ArrayUtil;

public class FileEntry {

    private String name;
    private byte[] data;

    public FileEntry(String name, byte[] data){
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
