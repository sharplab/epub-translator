package net.sharplab.epubtranslator.core.exception;


import org.xml.sax.SAXException;

public class EPubContentHandlingException extends RuntimeException {
    public EPubContentHandlingException(SAXException e) {
        super(e);
    }
}
