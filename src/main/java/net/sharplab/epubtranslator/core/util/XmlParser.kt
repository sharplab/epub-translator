package net.sharplab.epubtranslator.core.util

import org.w3c.dom.Document
import org.w3c.dom.DocumentFragment
import javax.enterprise.context.Dependent

interface XmlParser {
    fun parse(xmlString: String): Document

    fun parseStringToDocumentFragment(document: Document, xmlString: String): DocumentFragment
}

@Dependent
class XmlParserImpl : XmlParser {
    override fun parse(xmlString: String): Document =
        XmlUtils.parseXmlStringToDocument(xmlString)

    override fun parseStringToDocumentFragment(document: Document, xmlString: String): DocumentFragment =
        XmlUtils.parseXmlStringToDocumentFragment(document, xmlString)
}
