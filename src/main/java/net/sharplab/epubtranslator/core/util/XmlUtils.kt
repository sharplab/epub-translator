package net.sharplab.epubtranslator.core.util

import net.sharplab.epubtranslator.core.exception.EPubContentHandlingException
import nu.validator.htmlparser.dom.HtmlDocumentBuilder
import org.w3c.dom.Document
import org.w3c.dom.DocumentFragment
import org.w3c.dom.Node
import org.w3c.dom.ls.DOMImplementationLS
import org.w3c.dom.ls.LSSerializer
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.*

/**
 * Utility for Xml handling
 */
object XmlUtils {
    @JvmStatic
    fun getLsSerializer(document: Document): LSSerializer {
        val domImplementation = document.implementation as DOMImplementationLS
        val lsSerializer = domImplementation.createLSSerializer()
        lsSerializer.domConfig.setParameter("xml-declaration", false)
        lsSerializer.domConfig.setParameter("element-content-whitespace", true)
        lsSerializer.domConfig.setParameter("canonical-form", false)
        return lsSerializer
    }

    @JvmStatic
    fun serialize(node: Node): String {
        val document = if (node is Document) node else node.ownerDocument
        return getLsSerializer(document).writeToString(node)
    }

    @Throws(SAXException::class, IOException::class)
    fun load(file: File): Document {
        val inputSource = InputSource(FileReader(file))
        val builder = HtmlDocumentBuilder()
        return builder.parse(inputSource)
    }

    private fun createHtmlDocumentBuilder(): HtmlDocumentBuilder {

        val builder = HtmlDocumentBuilder()
        builder.setIgnoringComments(false)
        return builder
    }

    fun create(): Document {
        val builder = createHtmlDocumentBuilder()
        return builder.newDocument()
    }

    @JvmStatic
    fun parseXmlStringToDocument(xmlString: String): Document {
        val inputSource = InputSource(StringReader(xmlString))
        return try {
            val builder = createHtmlDocumentBuilder()
            builder.parse(inputSource)
        } catch (ex: IOException) {
            throw UncheckedIOException(ex)
        } catch (e: SAXException) {
            throw EPubContentHandlingException(e)
        }
    }

    /**
     * Parse xml string and return as [DocumentFragment].
     * @param document document that the new [DocumentFragment] belongs
     * @param xmlString xml string
     * @return [DocumentFragment] represents the xml string
     */
    @JvmStatic
    fun parseXmlStringToDocumentFragment(document: Document, xmlString: String): DocumentFragment {
        return try {
            val builder = createHtmlDocumentBuilder()
            val fragment = builder.parseFragment(InputSource(StringReader(xmlString)), "")
            document.adoptNode(fragment) as DocumentFragment
        } catch (e: SAXException) {
            throw EPubContentHandlingException(e)
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }
}