package net.sharplab.epubtranslator.core.driver.epub;

import net.sharplab.epubtranslator.core.exception.ContentFileProviderNotFoundException;
import net.sharplab.epubtranslator.core.model.EPubContentFile;
import net.sharplab.epubtranslator.core.model.EPubFile;
import net.sharplab.epubtranslator.core.model.FileEntry;
import net.sharplab.epubtranslator.core.provider.epub.EPubContentFileProvider;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class EPubReader {

    List<EPubContentFileProvider> ePubContentFileProviders;

    public EPubReader(List<EPubContentFileProvider> ePubContentFileProviders) {
        this.ePubContentFileProviders = Collections.unmodifiableList(ePubContentFileProviders);
    }

    public EPubFile read(File srcFile) {
        List<EPubContentFile> contentFiles = new LinkedList<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(srcFile))){
            for (ZipEntry zipEntry = zipInputStream.getNextEntry(); zipEntry != null; zipEntry = zipInputStream.getNextEntry()) {
                if (zipEntry.isDirectory()){
                    continue;
                }
                byte[] data = readZipEntry(zipInputStream);
                FileEntry fileEntry = new FileEntry(zipEntry.getName(), data);
                contentFiles.add(createEPubContentFile(fileEntry));
            }
            return new EPubFile(contentFiles);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private EPubContentFile createEPubContentFile(FileEntry fileEntry) {
        for(EPubContentFileProvider ePubContentFileProvider : ePubContentFileProviders){
            if(ePubContentFileProvider.canHandle(fileEntry)){
                return ePubContentFileProvider.provide(fileEntry);
            }
        }
        throw new ContentFileProviderNotFoundException();
    }


    static byte[] readFile(File file){
        final int bufferSize = 1024;
        final byte[] buffer = new byte[bufferSize];
        try (FileInputStream fileInputStream = new FileInputStream(file)){
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while (true) {
                int readSize = fileInputStream.read(buffer, 0, buffer.length);
                if (readSize < 0){
                    break;
                }
                byteArrayOutputStream.write(buffer, 0, readSize);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private byte[] readZipEntry(ZipInputStream zipInputStream){
        final int bufferSize = 1024;
        final byte[] buffer = new byte[bufferSize];
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){
            while (true) {
                int readSize = zipInputStream.read(buffer, 0, buffer.length);
                if (readSize < 0){
                    break;
                }
                byteArrayOutputStream.write(buffer, 0, readSize);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
