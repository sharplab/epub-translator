package net.sharplab.epubtranslator.core.util;

import net.sharplab.epubtranslator.core.service.EPubTranslatorException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;

/**
 * Utility for Xml handling
 */
public class XmlUtils {

    public static DocumentBuilder createDocumentBuilder() {
        DocumentBuilder builder;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new EPubTranslatorException(e);
        }
        return builder;
    }

    public static Document parseXmlStringToDocument(String xmlString) {
        InputSource inputSource = new InputSource(new StringReader(xmlString));
        try {
            DocumentBuilder builder = createDocumentBuilder();
            return builder.parse(inputSource);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (SAXException e) {
            throw new EPubTranslatorException(e);
        }
    }


    /**
     * Parse xml string and return as {@link DocumentFragment}.
     *
     * @param document  document that the new {@link DocumentFragment} belongs
     * @param xmlString xml string
     * @return {@link DocumentFragment} represents the xml string
     */
    public static DocumentFragment parseXmlStringToDocumentFragment(Document document, String xmlString) {
        try {
            DocumentBuilder builder = createDocumentBuilder();
            String envelopedXmlString = "<envelope>" + xmlString + "</envelope>";
            Document parsed = builder.parse(new InputSource(new StringReader(envelopedXmlString)));
            DocumentFragment fragment = parsed.createDocumentFragment();
            NodeList children = parsed.getDocumentElement().getChildNodes();
            while (children.getLength() > 0) {
                fragment.appendChild(children.item(0));
            }
            return (DocumentFragment) document.adoptNode(fragment);
        } catch (SAXException e) {
            throw new EPubTranslatorException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
