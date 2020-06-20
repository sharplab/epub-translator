package net.sharplab.epubtranslator.core.model;


public class FileEntry {

    private String name;
    private byte[] data;

    public FileEntry(String name, byte[] data){
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
