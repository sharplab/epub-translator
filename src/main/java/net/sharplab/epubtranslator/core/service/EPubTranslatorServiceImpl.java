package net.sharplab.epubtranslator.core.service;

import net.sharplab.epubtranslator.core.driver.translator.Translator;
import net.sharplab.epubtranslator.core.model.EPubChapter;
import net.sharplab.epubtranslator.core.model.EPubContentFile;
import net.sharplab.epubtranslator.core.model.EPubFile;
import net.sharplab.epubtranslator.core.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.enterprise.context.Dependent;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Dependent
public class EPubTranslatorServiceImpl implements EPubTranslatorService {

    /**
     * 一つの翻訳リクエストチャンクに含めることが可能な最大文字数
     */
    private final static int MAX_REQUESTABLE_TEXT_LENGTH = 5000;
    /**
     * インライン要素のタグリスト
     */
    public final static List<String> INLINE_ELEMENT_NAMES = Collections.unmodifiableList(Arrays.asList(
            "a", "abbr", "b", "bdi", "bdo", "br", "cite", "code", "data", "dfn", "em", "i",
            "kbd", "mark", "q", "rp", "rt", "rtc", "ruby", "s", "samp", "small", "span",
            "strong", "sub", "sup", "time", "u", "var", "wbr"));

    /**
     * 翻訳除外要素のリスト
     */
    public final static List<String> EXCLUDED_ELEMENT_NAMES = Collections.unmodifiableList(Arrays.asList("head", "pre", "tt"));

    private Logger logger = LoggerFactory.getLogger(EPubTranslatorServiceImpl.class);

    private Translator translator;

    public EPubTranslatorServiceImpl(Translator translator) {
        this.translator = translator;
    }

    @Override
    public EPubFile translate(EPubFile ePubFile, String srcLang, String dstLang) {
        List<EPubContentFile> contentFiles = ePubFile.getContentFiles();

        List<EPubContentFile> translatedContentFiles = contentFiles.stream().map(contentFile -> {
            if(contentFile instanceof EPubChapter){
                String contents = ((EPubChapter)contentFile).getDataAsString();
                String translatedContents = translateXmlString(contents, srcLang, dstLang);
                EPubChapter ePubChapter = new EPubChapter(contentFile.getName(), translatedContents.getBytes(StandardCharsets.UTF_8));
                logger.info("{} is translated.", ePubChapter.getName());
                return ePubChapter;
            }
            else {
                return contentFile;
            }
        }).collect(Collectors.toList());
        return new EPubFile(translatedContentFiles);
    }

    private String translateXmlString(String xmlString, String srcLang, String dstLang){
        Document document = XmlUtils.parseXmlStringToDocument(xmlString);
        Document translatedDocument = translateDocument(document, srcLang, dstLang);
        return XmlUtils.serialize(translatedDocument);
    }

    private Document translateDocument(Document document, String srcLang, String dstLang) {
        preProcessNode(document);
        List<TranslateRequest> translateRequests = generateTranslateRequests(document);
        List<TranslateRequestChunk> translateRequestChunks = formTranslateRequestChunks(translateRequests);
        replaceWithTranslatedText(translateRequestChunks, srcLang, dstLang);

        return document;
    }

    /**
     * 翻訳リクエストのリストを作成する
     * @param document 翻訳元のXML文書
     * @return 翻訳リクエストのリスト
     */
    List<TranslateRequest> generateTranslateRequests(Document document) {
        List<TranslateRequest> translateRequests = new ArrayList<>();
        TranslateRequest wip = new TranslateRequest(document, new ArrayList<>());
        translateRequests.add(wip);
        walkToGenerateTranslateRequests(document, new HashSet<>(), translateRequests);
        return translateRequests.stream()
                .filter(translateRequest -> !translateRequest.getSourceXmlString().trim().isEmpty())
                .collect(Collectors.toList());
    }


    /**
     * XML文書を走査して翻訳リクエストのリストを作成する
     * @param node 走査対象のノード
     * @param processedSet 処理済ノードのセット
     * @param translateRequests 翻訳リクエストのリスト
     */
    private void walkToGenerateTranslateRequests(Node node, Set<Node> processedSet, List<TranslateRequest> translateRequests){
        Document document = node.getOwnerDocument();
        //処理済のNodeはスキップ
        if(processedSet.contains(node)){
            return;
        }
        if(isExcludedNode(node)){
            return;
        }

        TranslateRequest wip = translateRequests.get(translateRequests.size()-1);
        //翻訳対象ノードの場合
        if(isTranslationTargetNode(node)){
            //テキスト中なので、リストに追加
            wip.getTarget().add(node);
            //同一階層の次以降のノードの探索
            for(Node nextNode = node.getNextSibling(); nextNode != null; nextNode = nextNode.getNextSibling()){
                //翻訳対象Nodeならリストに追加、処理済セットに追加
                if(isTranslationTargetNode(nextNode)){
                    wip.getTarget().add(nextNode);
                    processedSet.add(nextNode);
                }
                //翻訳対象Nodeでない、つまりテキストの末尾なので次のTranslateRequestを準備、但しWIPが0件でない場合に限る
                else if(wip.getTarget().size() > 0){
                    wip = new TranslateRequest(document, new ArrayList<>());
                    translateRequests.add(wip);
                    break;
                }
            }
            //親ノードが、翻訳対象ノード以外の場合、テキストの末尾なので次のTranslateRequestを準備、但しWIPが0件でない場合に限る
            if(!isTranslationTargetNode(node.getParentNode()) && wip.getTarget().size() > 0){
                wip = new TranslateRequest(document, new ArrayList<>());
                translateRequests.add(wip);
            }
        }
        //翻訳対象ノード以外の場合
        else{
            //子ノードを走査し、再帰処理
            NodeList childNodes = node.getChildNodes();
            for (int i=0;i<childNodes.getLength(); i++){
                Node child = childNodes.item(i);
                walkToGenerateTranslateRequests(child, processedSet, translateRequests);
            }
        }
    }

    /**
     * 翻訳除外ノードか判定する
     * @param node 判定対象ノード
     * @return 翻訳除外ノードの場合、true
     */
    private boolean isExcludedNode(Node node){
        return EXCLUDED_ELEMENT_NAMES.contains(node.getNodeName()) || (isTranslationTargetNode(node) && lookupNode(node, this::isExcludedNode));
    }


    /**
     * 翻訳リクエストのリストを翻訳リクエストのチャンクに構成する
     * @param translateRequests 翻訳リクエストのリスト
     * @return 翻訳リクエストのチャンクのリスト
     */
    List<TranslateRequestChunk> formTranslateRequestChunks(List<TranslateRequest> translateRequests){
        int textLengthCounter = 0;
        List<TranslateRequestChunk> translateRequestChunks = new ArrayList<>();
        List<TranslateRequest> workingTranslateRequestList = new ArrayList<>();
        for(TranslateRequest translateRequest : translateRequests) {
            String sourceXmlString =  translateRequest.getSourceXmlString();
            if(textLengthCounter + sourceXmlString.length() > MAX_REQUESTABLE_TEXT_LENGTH){
                translateRequestChunks.add(new TranslateRequestChunk(workingTranslateRequestList));
                workingTranslateRequestList = new ArrayList<>();
                textLengthCounter = 0;
            }
            workingTranslateRequestList.add(translateRequest);
            textLengthCounter += sourceXmlString.length();
        }
        translateRequestChunks.add(new TranslateRequestChunk(workingTranslateRequestList));
        return translateRequestChunks;
    }


    /**
     * XML文書中の翻訳リクエストのチャンクで指定された箇所を翻訳する
     * @param translateRequestChunks 翻訳リクエストチャンクのリスト
     */
    void replaceWithTranslatedText(List<TranslateRequestChunk> translateRequestChunks, String srcLang, String dstLang) {
        translateRequestChunks.forEach(translateRequestChunk -> {
            List<TranslateRequest> translateRequests = translateRequestChunk.getTranslateRequests();
            List<String> translateResponse = translator.translate(translateRequests.stream().map(TranslateRequest::getSourceXmlString).collect(Collectors.toList()), srcLang, dstLang);
            for(int i = 0; i< translateRequests.size(); i++){
                TranslateRequest translateRequest = translateRequests.get(i);
                Document document = translateRequest.getDocument();
                Node firstNode = translateRequest.getTarget().get(0);

                DocumentFragment documentFragment = XmlUtils.parseXmlStringToDocumentFragment(document, translateResponse.get(i));
                Element translatedTextContainerDiv = document.createElement("div");
                translatedTextContainerDiv.appendChild(documentFragment);
                firstNode.getParentNode().insertBefore(translatedTextContainerDiv, firstNode);
                Element originalTextContainerDiv = document.createElement("div");
                translatedTextContainerDiv.getParentNode().insertBefore(originalTextContainerDiv, translatedTextContainerDiv);
                List<Node> targetNodes = translateRequest.getTarget();
                for (Node targetNode : targetNodes) {
                    originalTextContainerDiv.appendChild(targetNode);
                }
            }
        });
    }


    void preProcessNode(Node node) {
        NodeList children = node.getChildNodes();
        for(int i=0; i<children.getLength();i++){
            Node child = children.item(i);
            if(isEmptyElement(child)){
                child.appendChild(node.getOwnerDocument().createComment("place holder"));
            }
            else {
                preProcessNode(child);
            }
        }
    }

    /**
     * 翻訳対象ノードか判定する
     * @param node 判定対象ノード
     * @return 翻訳対象ノードの場合、true
     */
    boolean isTranslationTargetNode(Node node){
        return node.getNodeType() == Node.TEXT_NODE || (node.getNodeType() == Node.ELEMENT_NODE && INLINE_ELEMENT_NAMES.contains(node.getNodeName()));
    }

    boolean isEmptyElement(Node node){
        if(node.getNodeType() == Node.ELEMENT_NODE && !node.hasChildNodes()){
            return true;
        }
        return false;
    }

    boolean lookupNode(Node node, Predicate<Node> condition){
        NodeList childNodes = node.getChildNodes();
        for (int i=0;i<childNodes.getLength(); i++){
            Node child = childNodes.item(i);
            if(condition.test(child)){
                return true;
            }
            else {
                if(lookupNode(child, condition)){
                    return true;
                }
            }
        }
        return false;
    }

}
