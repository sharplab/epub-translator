package net.sharplab.epubtranslator.core.service

import net.sharplab.epubtranslator.core.util.XmlUtils.getLsSerializer
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.util.*

/**
 * 翻訳リクエスト
 *
 * 対象のドキュメントにおける翻訳要求の一つを表現するデータ構造
 */
@Suppress("ConvertSecondaryConstructorToPrimary")
class TranslateRequest {

    /**
     * 翻訳対象ドキュメント
     */
    val document: Document
    /**
     * 翻訳対象ノードのリスト
     */
    val target: MutableList<Node>

    /**
     * 翻訳リクエストのコンストラクタ
     * @param document 翻訳対象ドキュメント
     * @param target 翻訳対象ノードのリスト
     */
    constructor(document: Document, target: List<Node>) {
        this.document = document
        this.target = target.toMutableList() //TODO
    }

    /**
     * 翻訳対象ノードの文字列表現を返却する
     */
    val sourceXmlString: String
        get() {
            val stringBuilder = StringBuilder()
            val lsSerializer = getLsSerializer(document)
            for (node in target) {
                stringBuilder.append(lsSerializer.writeToString(node))
            }
            return stringBuilder.toString()
        }

    override fun toString(): String {
        return sourceXmlString
    }

}