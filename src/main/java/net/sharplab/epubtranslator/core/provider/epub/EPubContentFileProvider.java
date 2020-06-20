package net.sharplab.epubtranslator.core.provider.epub;


import net.sharplab.epubtranslator.core.model.EPubContentFile;
import net.sharplab.epubtranslator.core.model.FileEntry;

/**
 * ファイルエントリからEPubContentFileを生成するプロバイダ
 */
public interface EPubContentFileProvider {

    /**
     * 対象のファイルエントリを扱うことが出来るかを返却する
     * @param fileEntry ファイルエントリ
     * @return ファイルエントリを扱える場合true
     */
    boolean canHandle(FileEntry fileEntry);

    /**
     * EPubContentFileを生成する
     * @param fileEntry ファイルエントリ
     * @return EPubContentFile
     */
    EPubContentFile provide(FileEntry fileEntry);
}
