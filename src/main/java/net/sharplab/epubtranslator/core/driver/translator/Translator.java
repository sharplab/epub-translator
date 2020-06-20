package net.sharplab.epubtranslator.core.driver.translator;

import java.util.List;

public interface Translator {
    List<String> translate(List<String> texts, String srcLang, String dstLang);
}
