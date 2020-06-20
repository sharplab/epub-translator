package net.sharplab.epubtranslator.core.service;

import java.util.Collections;
import java.util.List;

/**
 * TranslateRequestChunk
 */
public class TranslateRequestChunk {

    public List<TranslateRequest> translateRequests;

    public TranslateRequestChunk(List<TranslateRequest> translateRequests) {
        this.translateRequests = Collections.unmodifiableList(translateRequests);
    }

    public List<TranslateRequest> getTranslateRequests() {
        return translateRequests;
    }
}
