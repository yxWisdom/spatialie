package edu.nju.ws.spatialie.spaceeval;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.data.BratRelation;
import edu.nju.ws.spatialie.utils.XmlUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.dom4j.Element;

import java.util.*;
import java.util.stream.Collectors;

import static edu.nju.ws.spatialie.spaceeval.SpaceEvalUtils.MOTION_SIGNAL;
import static edu.nju.ws.spatialie.spaceeval.SpaceEvalUtils.invalidCharFixedMap;

public class SpaceEvalDoc {
    private String document;
    private List<Span> elements = new ArrayList<>();
    private Map<String, Span> elementMap = new HashMap<>();
    private List<Span> tokens = new ArrayList<>();
    private List<List<Span>> sentences=new ArrayList<>();
//    private List<BratEvent> qsLinks=new ArrayList<>();;
//    private List<BratEvent> oLinks=new ArrayList<>();;
//    private List<BratEvent> moveLinks=new ArrayList<>();;
//    private List<BratEvent> measureLinks = new ArrayList<>();
    private List<BratRelation> relations = new ArrayList<>();
    private List<BratEvent> allLinks = new ArrayList<>();

    // Link中的所有element的id集合
    private Set<String> elementIdInLinks = new HashSet<>();
    private String path;

    SpaceEvalDoc() {}

    SpaceEvalDoc(String filePath) {
        this.path = filePath;

        Element root = XmlUtil.getRootElement(filePath);
        String document = root.element("TEXT").getStringValue();
        Element tags = root.element("TAGS");
        this.document = document;
        for(Iterator it = tags.elementIterator(); it.hasNext();) {
            Element element = (Element) it.next();
            if (SpaceEvalUtils.allElementTags.contains(element.getName())) {
                parseElement(element);
            }
        }
        for(Iterator it = tags.elementIterator(); it.hasNext();) {
            Element element = (Element) it.next();
            String tag = element.getName();
            if (SpaceEvalUtils.allLinkTags.contains(tag)) {
                parseLink(element);
            }
            if (tag.equals(SpaceEvalUtils.METALINK)) {
                parseMetaLink(element);
            }
        }

        Collections.sort(elements);
        for (int i = 0; i < elements.size(); i++) {
            Span element = elements.get(i);
            if (element.text.endsWith(" ")) {
                // WARNING:
                element.text = element.text.trim();
                element.end -= 1;
            }
            if (i > 0) {
                Span preElement = elements.get(i-1);
                // 此处是为了解决一个短语可能被标记为了多个标记，大多是MOTION_SIGNAL和其他类型， 这里优先选择其他类型
                if (preElement.end > element.start) {
                    if (preElement.label.equals(MOTION_SIGNAL)) {
                        preElement.start = -1;
                        preElement.end = -1;
                    } else if (element.label.equals(MOTION_SIGNAL)) {
                        element.start = -1;
                        element.end = -1;
                    }
                }
            }
        }
        parseTokens(root);
        dealNullRole();
    }

    // 解析element
    private void parseElement(Element element) {
        String id = element.attributeValue("id");
        String text = element.attributeValue("text");
        int start = Integer.valueOf(element.attributeValue("start"));
        int end = Integer.valueOf(element.attributeValue("end"));
        String label = element.getName();
        Span span = new Span(id, text, label, start, end);
        elements.add(span);
        elementMap.put(id, span);
    }

    // 有的token分词正确，需要将其重新拆分
    private List<Span> split(Span span) {
        String text = span.text;
        List<Span> tokens = new ArrayList<>();
        if (text.length() <= 1) {
            tokens.add(span);
            return tokens;
        }

        List<String> wordList = new ArrayList<>();
        StringBuilder tmp = new StringBuilder();

        for (int i = 0; i < text.length();) {
            int index;
            if (text.startsWith("â€", i)) {
                if (text.substring(i,i+3).equals("â€™") && i != 0 && i+3 != text.length()) {
                    tmp.append(invalidCharFixedMap.get("â€™")).append("  ");
                    i += 3;
                    continue;
                } else {
                    index = i + 3;
                }

            } else if ((text.charAt(i) == ',' || text.charAt(i) == '/')&& (i == 0 || !Character.isDigit(text.charAt(i-1)))
                    && (i == text.length() -1 || !Character.isDigit(text.charAt(i+1)))) {
                index = i + 1;
            } else if (text.charAt(i) == '?' || text.charAt(i) == '(' || text.charAt(i) == '…') {
                index = i + 1;
            } else {
                tmp.append(text.charAt(i));
                i++;
                continue;
            }
            if (tmp.length() > 0) {
                wordList.add(tmp.toString());
            }
            wordList.add(text.substring(i, index));
            i = index;
            tmp = new StringBuilder();
        }
        if (tmp.length() > 0) {
            wordList.add(tmp.toString());
        }
        int start = span.start;
        for (String word: wordList) {
            String t = invalidCharFixedMap.getOrDefault(word, word).replaceAll("\\s+", "");
            tokens.add(new Span(span.id, t, span.label, start, start + word.length()));
            start += word.length();
        }
        return tokens;
    }

    private List<Span> processInvalidChars(List<Span> tokens) {
        List<Span> res = new ArrayList<>();
        for (Span token: tokens) {
            res.addAll(split(token));
        }
        return res;
    }



    private void parseTokens(Element rootElement) {
        Element tokens = rootElement.element("TOKENS");
//        int tokenIdx = 0;
        List<List<Span>> sentencesTmp = new ArrayList<>();
        for(Iterator it = tokens.elementIterator(); it.hasNext();) {
            Element element = (Element) it.next();
            if (!element.getName().equals("s"))
                // WARNING: 有的语料中有不在<s></s>中的<lex>
                continue;
            List<Span> sentence = new ArrayList<>();
            for (Iterator t_it = element.elementIterator(); t_it.hasNext();) {
                Element tokenElement = (Element) t_it.next();
                String text = tokenElement.getText();
                String id = tokenElement.attributeValue("id");
                id = id == null ? "" : id;
                int start = Integer.valueOf(tokenElement.attributeValue("begin"));
                int end = Integer.valueOf(tokenElement.attributeValue("end"));

                /* WARNING  此处id的取值需要注意*/
//                Span token = new Span(String.valueOf(tokenIdx++), text, "O", start, end);
                Span token = new Span(id, text, "O", start, end);
                int index = Collections.binarySearch(this.elements, token);
                if (index >= 0) {
                    token.label = "B-" + this.elements.get(index).label;
                } else if(index < -1) {
                    index = -index - 2;
                    if (token.end <= elements.get(index).end) {
                        token.label = "I-" + elements.get(index).label;
                    }
                }
//                this.tokens.add(token);
                sentence.add(token);
            }
            sentencesTmp.add(sentence);
        }

        List<Span> elements = this.elements.stream().filter(x -> x.start != -1).sorted().collect(Collectors.toList());
        int k = 0;

        for (List<Span> sentence: sentencesTmp) {
            sentence = processInvalidChars(sentence);
            List<Span> newSentence = new ArrayList<>();
            for (int i = 0; i < sentence.size();) {
                Span span = sentence.get(i);
                while (k < elements.size() && elements.get(k).start < span.start) k++;
                Span element = k < elements.size() ? elements.get(k): null;
                if (element != null && element.start == span.start) {
                    if (span.end > element.end) {
                        String t = span.text.substring(element.end-span.start);
                        sentence.add(i + 1, new Span("", t, "O", element.end, span.end));
                        Span spanTmp = new Span(element.id, element.text, "B-" + element.label, element.start, element.end);
                        newSentence.add(spanTmp);
                    } else {
                        Span spanTmp = new Span(element.id, span.text, "B-" + element.label, span.start, span.end);
                        newSentence.add(spanTmp);
                    }
                    String eleText = span.end < element.end ? element.text.substring(span.end-span.start) : "";

                    for (i = i + 1; i < sentence.size();i++) {
                        Span span1 = sentence.get(i);
                        if (span1.start >= element.end)
                            break;
                        eleText = eleText.substring(span1.start - sentence.get(i -1).end);
                        if (span1.end > element.end) {
                            String t1 = span1.text.substring(0, element.end-span1.start), t2 = span1.text.substring(element.end-span1.start);
                            sentence.add(i + 1, new Span("", t2, "O", element.end, span1.end));
                            Span spanTmp = new Span(element.id, t1, "I-" + element.label, span1.start, element.end);
                            newSentence.add(spanTmp);
                        } else {
                            Span spanTmp = new Span(element.id, span1.text, "I-" + element.label, span1.start, span1.end);
                            newSentence.add(spanTmp);
                        }
                        eleText = span.end < element.end ? element.text.substring(span1.end-span1.start) : "";
                    }
                } else {
                    newSentence.add(span);
                    i++;
                }
            }
//            if (sentence.size() != newSentence.size()) {
//                System.out.println("长度不一致");
//            } else {
//                for (int i = 0; i < newSentence.size(); i++) {
//                    if (!newSentence.get(i).equals(sentence.get(i))) {
//                        System.out.println(newSentence.get(i) + " " + sentence.get(i));
//                    }
//                }
//            }
            this.sentences.add(newSentence);
        }

        this.tokens = this.sentences.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    private void parseLink(Element element) {
        String id = element.attributeValue("id");
        String tag = element.getName();

        BratEvent bratEvent = new BratEvent();
        bratEvent.setId(id);
        bratEvent.setType(tag);

        for (String role: SpaceEvalUtils.allRoles) {
            String value = element.attributeValue(role);
            if (value==null || value.equals(""))
                continue;
            if (tag.equals(SpaceEvalUtils.MOVELINK)) {
                // 语料中的格式错误
                if (role.equals("landmark")) role = "ground";
                if (role.equals("adjunctID")) role = "motion_signalID";
            }
            String [] elementIds = value.replaceAll("\\s", "").split(",");
//            if (elementIds.length > 1) {
//                System.out.println(role + ":" + value);
//            }
            for (String eid: elementIds) {
                if (!elementMap.containsKey(eid)) {
//                    System.out.println(this.path + " " + tag + " " + role + " " + eid);
                    continue;
                }
                bratEvent.addRole(role, eid);
                this.elementIdInLinks.add(eid);
            }
        }
        allLinks.add(bratEvent);
//        switch(tag) {
//            case SpaceEvalUtils.QSLINK: qsLinks.add(bratEvent);break;
//            case SpaceEvalUtils.MOVELINK: moveLinks.add(bratEvent);break;
//            case SpaceEvalUtils.OLINK: oLinks.add(bratEvent);break;
//            case SpaceEvalUtils.MEASURELINK: measureLinks.add(bratEvent);break;
//            default:break;
//        }
    }

    private void parseMetaLink(Element element) {
        String id = element.attributeValue("fromID");
        String fromID = element.attributeValue("fromID");
        String toID = element.attributeValue("toID");
        String relType = element.attributeValue("relType");

        BratRelation bratRelation = new BratRelation(id, relType, fromID, toID);
        this.relations.add(bratRelation);
    }



    // 获取elements所在的句子
    public List<List<Span>> getSentencesOfElements(List<Span> elements) {
        List<Pair<Integer, Integer>> sentenceOffsets = new ArrayList<>();
        for(List<Span> sentence: sentences) {
            int start = sentence.get(0).start;
            int end = sentence.get(sentence.size()-1).end;
            sentenceOffsets.add(new ImmutablePair<>(start, end));
        }
        Set<Integer> sentenceIdSet = new TreeSet<>();
        for (Span element: elements) {
            for (int i=0; i < sentenceOffsets.size();i++) {
                Pair<Integer, Integer> pair = sentenceOffsets.get(i);
                if (pair.getLeft() <= element.start && element.end <= pair.getRight()) {
                    sentenceIdSet.add(i);
                }
            }
        }
        List<List<Span>> sentences = new ArrayList<>();
        for (int i: sentenceIdSet) {
            sentences.add(this.sentences.get(i));
        }
        return sentences;
    }

    // 获取link所在的句子
    public List<List<Span>> getAllSentencesOfLink(BratEvent link) {
        List<Span> elements = link.getRoleMap().values().stream()
                .map(o -> elementMap.get(o)).collect(Collectors.toList());
        return getSentencesOfElements(elements);
    }

    // 获取link所在的句子的所有token
    public List<Span> getAllTokenOfLink(BratEvent event) {
        List<List<Span>> sentences = getAllSentencesOfLink(event);
        return sentences.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }
    // 获取element集合所在的句子的所有token
    public List<Span> getAllTokenOfElements(List<Span> elements) {
        List<List<Span>> sentences = getSentencesOfElements(elements);
        return sentences.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    // 获取start到end范围内的所有元素
    public List<Span> getElementsInSentence(int start, int end) {
        return this.elements.stream().filter(o -> start <= o.start && o.end <= end)
                .collect(Collectors.toList());
    }

    // 处理空实体的情况
    public void dealNullRole() {
        for (BratEvent link: allLinks) {

            List<Span> tokensOfLink = getAllTokenOfLink(link);
            int linkStart = (tokensOfLink.size() > 0) ? tokensOfLink.get(0).start: 0;
            int linkEnd =  (tokensOfLink.size() > 0) ? tokensOfLink.get(tokensOfLink.size()-1).end: 0x7fffff;

            Multimap<String, String> tmpRoleMap = HashMultimap.create(link.getRoleMap());
            for (Map.Entry<String, String> entry: tmpRoleMap.entries()) {
                String role = entry.getKey(), elementId = entry.getValue();
                Span element = this.elementMap.get(elementId);
                for (BratRelation relation: relations) {
                    String sourceId = relation.getSourceId(), targetId = relation.getTargetId();
                    Span source = elementMap.get(sourceId), target = elementMap.get(targetId);
                    if (relation.getTag().equals("SUBCOREFERENCE")) {
//                        if (elementId.equals(sourceId)) {
//                            System.out.println("abc");
//                        }
                        if (elementId.equals(targetId)) {
                            if (linkStart <= source.start && source.end <= linkEnd &&
                                    !elementIdInLinks.contains(sourceId)) {
                                link.addRole(role, sourceId);
                            }
//                            else {
//                                System.out.println("def");
//                            }
                        }
                    }
                }
                if (element.start == -1) {
                    link.removeRole(role, elementId);
                }
            }
        }
    }

    // 合并trigger相同的Link
    public List<BratEvent> getMergedLinks() {
        Map<String, List<BratEvent>> group = new HashMap<>();
        List<BratEvent> mergedLinks = new ArrayList<>();
        for (BratEvent link: this.allLinks) {
            String key = null;
            if (!link.getRoleId("trigger").equals("")) {
                key = link.getRoleId("trigger");
            }
            if (!link.getRoleId("val").equals("")) {
                key = link.getRoleId("val");
            }
            if (key != null) {
                group.putIfAbsent(key + link.getType(), new ArrayList<>());
                group.get(key + link.getType()).add(link);
            } else {
                mergedLinks.add(link);
            }
        }
        for (Map.Entry<String, List<BratEvent>> entry: group.entrySet()) {
            Multimap<String, String> map = HashMultimap.create();
            for (BratEvent bratEvent: entry.getValue()) {
                map.putAll(bratEvent.getRoleMap());
            }
            BratEvent bratEvent = entry.getValue().get(0);
            bratEvent.setRoleMap(map);
            mergedLinks.add(bratEvent);
        }
        return mergedLinks;
    }

    public String getDocument() { return this.document; }

    public List<Span> getTokens() { return this.tokens; }

    public List<Span> getElements() { return this.elements; }

    public List<BratEvent> getQSLink() {
        return allLinks.stream().filter(o -> o.getType().equals(SpaceEvalUtils.QSLINK)).collect(Collectors.toList());
    }

    public List<BratEvent> getOLink() {
        return allLinks.stream().filter(o -> o.getType().equals(SpaceEvalUtils.OLINK)).collect(Collectors.toList());
    }

    public List<BratEvent> getMeasureLinks() {
        return allLinks.stream().filter(o -> o.getType().equals(SpaceEvalUtils.MEASURELINK)).collect(Collectors.toList());
    }

    public List<BratEvent> getMoveLink() {
        return allLinks.stream().filter(o -> o.getType().equals(SpaceEvalUtils.MOVELINK)).collect(Collectors.toList());
    }

    public List<BratEvent> getAllLinks() {
        return this.allLinks;
    }

    public void setDocument(String document) { this.document = document; }

    public List<List<Span>> getSentences() { return sentences; }

    public Map<String, Span> getElementMap() {
        return elementMap;
    }


    // 获取NER任务需要识别的元素
    public List<Span> getElementsOfNERTask() {
        return this.elements.stream()
                .filter(o -> o.start != -1)
                .filter(o -> SpaceEvalUtils.mainElementTags.contains(o.label))
                .collect(Collectors.toList());
    }

    public String getPath() {
        return path;
    }

    public static void main(String [] args) {
//        List<File> files = FileUtil.listFiles("data/SpaceEval2015/raw_data/training++");
//        files.addAll(FileUtil.listFiles("data/SpaceEval2015/raw_data/gold++"));
//        for (File file: files) {
//            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
//        }
        SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc("data/SpaceEval2015/raw_data/training++/ANC/WhereToJapan/Ginza.xml");
    }
}
