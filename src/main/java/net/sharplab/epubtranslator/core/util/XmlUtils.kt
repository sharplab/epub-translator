package net.sharplab.epubtranslator.core.util

import net.sharplab.epubtranslator.core.service.EPubTranslatorException
import org.w3c.dom.Document
import org.w3c.dom.DocumentFragment
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.StringReader
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

/**
 * Utility for Xml handling
 */
object XmlUtils {

    private fun createDocumentBuilder(): DocumentBuilder {
        val builder: DocumentBuilder
        try {
            val factory = DocumentBuilderFactory.newDefaultInstance()
            factory.isNamespaceAware = true
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            builder = factory.newDocumentBuilder()
        } catch (e: ParserConfigurationException) {
            throw EPubTranslatorException(e)
        }
        return builder
    }

    @JvmStatic
    fun parseXmlStringToDocument(xmlString: String): Document {
        val inputSource = InputSource(StringReader(xmlString))
        return try {
            val builder = createDocumentBuilder()
            builder.parse(inputSource)
        } catch (e: SAXException) {
            throw EPubTranslatorException(e)
        }
    }

    /**
     * Parse xml string and return as [DocumentFragment].
     *
     * @param document  document that the new [DocumentFragment] belongs
     * @param xmlString xml string
     * @return [DocumentFragment] represents the xml string
     */
    @JvmStatic
    fun parseXmlStringToDocumentFragment(document: Document, xmlString: String): DocumentFragment {
        return try {
            val builder = createDocumentBuilder()
            val envelopedXmlString = "<envelope>$xmlString</envelope>"
            val parsed = builder.parse(InputSource(StringReader(envelopedXmlString)))
            val fragment = parsed.createDocumentFragment()
            val children = parsed.documentElement.childNodes
            while (children.length > 0) {
                fragment.appendChild(children.item(0))
            }
            document.adoptNode(fragment) as DocumentFragment
        } catch (e: SAXException) {
            throw EPubTranslatorException(e)
        }
    }
}