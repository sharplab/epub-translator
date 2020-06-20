package net.sharplab.epubtranslator.core.util;

import net.sharplab.epubtranslator.core.exception.EPubContentHandlingException;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;

/**
 * Utility for Xml handling
 */
public class XmlUtils {

    public static LSSerializer getLsSerializer(Document document) {
        DOMImplementationLS domImplementation = (DOMImplementationLS) document.getImplementation();
        LSSerializer lsSerializer = domImplementation.createLSSerializer();
        lsSerializer.getDomConfig().setParameter("xml-declaration", false);
        lsSerializer.getDomConfig().setParameter("element-content-whitespace", true);
        lsSerializer.getDomConfig().setParameter("canonical-form", false);
        return lsSerializer;
    }

    public static String serialize(Node node){
        Document document = node instanceof Document ? (Document) node : node.getOwnerDocument();
        return getLsSerializer(document).writeToString(node);
    }

    public static Document load(File file) throws SAXException, IOException {
        InputSource inputSource = new InputSource(new FileReader(file));
        HtmlDocumentBuilder builder = new HtmlDocumentBuilder();
        return builder.parse(inputSource);
    }

    public static HtmlDocumentBuilder createHtmlDocumentBuilder(){
        HtmlDocumentBuilder builder = new HtmlDocumentBuilder();
        builder.setIgnoringComments(false);
        return builder;
    }

    public static Document create(){
        HtmlDocumentBuilder builder = createHtmlDocumentBuilder();
        return builder.newDocument();
    }

    public static Document parseXmlStringToDocument(String xmlString) {
        InputSource inputSource = new InputSource(new StringReader(xmlString));
        try {
            HtmlDocumentBuilder builder = createHtmlDocumentBuilder();
            return builder.parse(inputSource);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (SAXException e) {
            throw new EPubContentHandlingException(e);
        }
    }


    /**
     * Parse xml string and return as {@link DocumentFragment}.
     * @param document document that the new {@link DocumentFragment} belongs
     * @param xmlString xml string
     * @return {@link DocumentFragment} represents the xml string
     */
    public static DocumentFragment parseXmlStringToDocumentFragment(Document document, String xmlString){
        try {
            HtmlDocumentBuilder builder = createHtmlDocumentBuilder();
            DocumentFragment fragment = builder.parseFragment(new InputSource(new StringReader(xmlString)), "");
            return (DocumentFragment) document.adoptNode(fragment);
        } catch (SAXException e) {
            throw new EPubContentHandlingException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
