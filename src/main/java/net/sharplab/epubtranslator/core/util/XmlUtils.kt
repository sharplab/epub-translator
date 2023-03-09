package net.sharplab.epubtranslator.core.util

import net.sharplab.epubtranslator.core.service.EPubTranslatorException
import org.slf4j.LoggerFactory
import org.w3c.dom.*
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
    private val logger = LoggerFactory.getLogger(XmlUtils::class.java)

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

    fun parseXmlStringToDocument(xmlString: String): Document =
        parseXmlStringToDocumentWithoutCheckDoc(xmlString)
            .also { checkDoc(it) } // This removes null-nodes in translated output, even though it doesn't detect any. Very strange

    fun parseXmlStringToDocumentWithoutCheckDoc(xmlString: String): Document {
        val inputSource = InputSource(StringReader(xmlString))
        return try {
            val builder = createDocumentBuilder()
            builder.parse(inputSource)
        } catch (e: SAXException) {
            throw EPubTranslatorException(e)
        }
    }

    // Use checkDoc to avoid or detect this NullPointer issue
    // - java.lang.NullPointerException
    //        at java.xml/com.sun.org.apache.xml.internal.serializer.dom3.DOM3TreeWalker.dispatachChars(DOM3TreeWalker.java:373)
    //        at java.xml/com.sun.org.apache.xml.internal.serializer.dom3.DOM3TreeWalker.serializeText(DOM3TreeWalker.java:1030)
    //        at java.xml/com.sun.org.apache.xml.internal.serializer.dom3.DOM3TreeWalker.startNode(DOM3TreeWalker.java:418)
    //        at java.xml/com.sun.org.apache.xml.internal.serializer.dom3.DOM3TreeWalker.traverse(DOM3TreeWalker.java:263)
    //        at java.xml/com.sun.org.apache.xml.internal.serializer.dom3.DOM3SerializerImpl.serializeDOM3(DOM3SerializerImpl.java:106)
    //        at java.xml/com.sun.org.apache.xml.internal.serializer.dom3.LSSerializerImpl.writeToString(LSSerializerImpl.java:1107)
    // Thanks to: https://stackoverflow.com/a/17009571/197141
    @Suppress("MemberVisibilityCanBePrivate")
    fun checkDoc(n: Node) {
        logger.trace("node: {}", n)
        if (n is Text) {
            if (n.data == null) {
                // If this happens we could probably set n.data =""
                logger.warn("Null data found in XML node. If a NullPointerException is thrown, please report this on the issue tracker")
            }
            if (doLog) logger.info("{} Text: '{}'", n.data?.length ?: -1, n.data)
        } else {
            logger.trace("node: {}, childnodes: {}", n.javaClass.simpleName, n.childNodes)
        }
        val l: NodeList = n.childNodes
        for (i in 0 until l.length) {
            checkDoc(l.item(i))
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

    private const val doLog = false
}