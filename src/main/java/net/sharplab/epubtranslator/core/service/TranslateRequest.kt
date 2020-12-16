package net.sharplab.epubtranslator.core.service

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.ls.DOMImplementationLS

/**
 * 翻訳リクエスト
 *
 *
 * 対象のドキュメントにおける翻訳要求の一つを表現するデータ構造
 */
class TranslateRequest
/**
 * 翻訳リクエストのコンストラクタ
 *
 * @param document 翻訳対象ドキュメント
 * @param target   翻訳対象ノードのリスト
 */(
        /**
         * 翻訳対象ドキュメント
         */
        val document: Document,
        /**
         * 翻訳対象ノードのリスト
         */
        val target: MutableList<Node>) {

    /**
     * 翻訳対象ノードの文字列表現を返却する
     */
    val sourceXmlString: String
        get() {
            val stringBuilder = StringBuilder()
            val domImplementation = document.implementation as DOMImplementationLS
            val lsSerializer = domImplementation.createLSSerializer()
            lsSerializer.domConfig.setParameter("xml-declaration", false)
            lsSerializer.domConfig.setParameter("element-content-whitespace", true)
            lsSerializer.domConfig.setParameter("canonical-form", false)
            for (node in target) {
                stringBuilder.append(lsSerializer.writeToString(node))
            }
            return stringBuilder.toString()
        }

    override fun toString(): String {
        return sourceXmlString
    }

}