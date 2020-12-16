package net.sharplab.epubtranslator.core.provider.epub

import net.sharplab.epubtranslator.core.model.EPubContentFile
import net.sharplab.epubtranslator.core.model.FileEntry

/**
 * ファイルエントリからEPubContentFileを生成するプロバイダ
 */
interface EPubContentFileProvider {
    /**
     * 対象のファイルエントリを扱うことが出来るかを返却する
     * @param fileEntry ファイルエントリ
     * @return ファイルエントリを扱える場合true
     */
    fun canHandle(fileEntry: FileEntry): Boolean

    /**
     * EPubContentFileを生成する
     * @param fileEntry ファイルエントリ
     * @return EPubContentFile
     */
    fun provide(fileEntry: FileEntry): EPubContentFile
}
