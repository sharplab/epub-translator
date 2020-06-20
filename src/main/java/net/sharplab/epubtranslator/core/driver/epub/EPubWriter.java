package net.sharplab.epubtranslator.core.driver.epub;

import net.sharplab.epubtranslator.core.model.EPubContentFile;
import net.sharplab.epubtranslator.core.model.EPubFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class EPubWriter {
    public void write(EPubFile ePubFile, File dstFile) {
        try(FileOutputStream fileOutputStream = new FileOutputStream(dstFile);
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)){
            for(EPubContentFile contentFile : ePubFile.getContentFiles()){
                ZipEntry zipEntry = new ZipEntry(contentFile.getName());
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(contentFile.getData());
            }
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
