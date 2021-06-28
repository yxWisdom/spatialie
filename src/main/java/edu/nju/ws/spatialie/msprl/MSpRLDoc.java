package edu.nju.ws.spatialie.msprl;

import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.spaceeval.Span;
import edu.nju.ws.spatialie.utils.XmlUtil;
import org.dom4j.Element;

import java.util.*;

import static edu.nju.ws.spatialie.msprl.MSpRLUtils.*;


/**
 * @author xyu
 * @version 1.0
 * @date 2021/3/26 21:16
 */

class SpRLSentence {
    List<Span> elements = new ArrayList<>();
    Map<String, Span> elementMap = new HashMap<>();
    List<BratEvent> links = new ArrayList<>();
    List<Span> tokens = new ArrayList<>();

    SpRLSentence(Element sentence) {
        String sentenceText = sentence.elementText("TEXT");

        for (Iterator<?> it = sentence.elementIterator(); it.hasNext();) {
            Element element = (Element) it.next();
            String tag = element.getName();
            if (MSpRLUtils.roleTypes.contains(tag)) {
                String id = element.attributeValue("id");
                int start = Integer.parseInt(element.attributeValue("start"));
                int end = Integer.parseInt(element.attributeValue("end"));
                if (start == -1 || end == -1) continue;
                String text = element.attributeValue("text");
                String label = tag.equals(SPATIALINDICATOR) ? SPATIAL_SIGNAL : SPATIAL_ENTITY;
                Span span = new Span(id, text, label, start, end);
                elements.add(span);
                elementMap.put(id, span);
            }
        }
        // parse elements
        Collections.sort(elements);
//        Map<String, String> discardedIdMap = new HashMap<>();
//        for (int i = 1; i < elements.size(); i++) {
//            Span pre = elements.get(i-1);
//            Span cur = elements.get(i);
//            if (cur.start == pre.start && cur.end == pre.end) continue;
//            if (cur.start < pre.end) {
//                String discardId = cur.text.length() > pre.text.length() ? pre.id: cur.id;
//                String retainedId = cur.text.length() > pre.text.length() ? cur.id: pre.id;
//                discardedIdMap.put(discardId, retainedId);
//                System.out.println(sentenceText);
//                System.out.printf("%s:%s  -  %s:%s\n", pre.text, pre.label, cur.text, cur.label);
//                System.out.println();
//            }
//        }
//
//        Map<String, String> span2id = new HashMap<>();
//        Map<String, String> id2span = new HashMap<>();
//
//        elements.forEach(element -> {
//            String span = element.start + "-" + element.end;
//            if (!discardedIdMap.containsKey(element.id) && !span2id.containsKey(span)) {
//                span2id.putIfAbsent(span, element.id);
//                elementMap.put(element.id, element);
//            }
//            id2span.put(element.id, span);
//        });
//
//        discardedIdMap.forEach((k, v) -> id2span.put(k, id2span.get(v)));
//        elements = new ArrayList<>(elementMap.values());
//        Collections.sort(elements);

        // parse relations
        for (Iterator<?> it = sentence.elementIterator("RELATION"); it.hasNext();) {
            Element relation = (Element) it.next();

            String [] generalTypes = relation.attributeValue("general_type").trim().split("/");
            String [] rcc8Values = relation.attributeValue("RCC8_value").trim().split("/");

            if (generalTypes.length != rcc8Values.length) {
                System.err.println("general_type和rcc8Values长度不一致");
            }

            for (int i = 0; i < generalTypes.length; i++) {
//                String linkType = generalType2LinkType.get(generalTypes[i]);
                String linkType = generalTypes[i].trim();
                String rcc8Value = rcc8Values[i];
                BratEvent link = new BratEvent();
                link.setId(relation.attributeValue("id"));
                link.setType(linkType);
                link.addAttribute("relType", rcc8Value);
                for (String attrName: MSpRLUtils.attrNames) {
                    String roleId = relation.attributeValue(attrName);
                    if (!elementMap.containsKey(roleId)) continue;
//                    if (!id2span.containsKey(roleId)) continue;
//                    roleId = span2id.get(id2span.get(roleId));
                    String roleType = roleMap.get(attrName);
                    link.addRole(roleType, roleId);
                }
                links.add(link);
            }
        }

        // parse tokens
        String [] words = sentenceText.split(" ");
        int cursor=0;
        for (String word : words) {
            if (!word.isEmpty()) {
                tokens.add(new Span("", word, "O", cursor, cursor + word.length()));
                cursor += word.length() + 1;
            } else {
                cursor++;
            }

        }

        int idx = 0;
        for (Span element: elements) {
            while (tokens.get(idx).start != element.start) {
//                tokens.get(idx).label = "O";
                idx++;
            }
            tokens.get(idx).id = element.id;
            tokens.get(idx).label = "B-" + element.label;
            idx++;
            while(idx < tokens.size() && tokens.get(idx).end <= element.end) {
                tokens.get(idx).id = element.id;
                tokens.get(idx).label = "I-" + element.label;
                idx++;
            }
        }
    }

//    List<BratEvent> getLinks() {
//        return links;
//    }
//
//    List<Span> getTokens() {
//        return tokens;
//    }
//
//    List<Span> getElements() {
//
//    }
}


public class MSpRLDoc {
    List<SpRLSentence> sentences = new ArrayList<>();
    String path;
    public MSpRLDoc(String filePath) {
        this.path = filePath;
        Element root = XmlUtil.getRootElement(filePath);
        for (Object sceneObj : root.elements("SCENE")) {
            Element scene = (Element) sceneObj;
            for (Object sentenceObj : scene.elements("SENTENCE")) {
                Element sentenceEle = (Element)sentenceObj;
                SpRLSentence sentence = new SpRLSentence(sentenceEle);
                sentences.add(sentence);
            }
        }
    }
    public static void main(String [] args) {
        MSpRLDoc doc1 =  new MSpRLDoc("data/mSpRL2017/clean_data/Sprl2017_train.xml");
        System.out.println("****************************");
        MSpRLDoc doc2 =  new MSpRLDoc("data/mSpRL2017/clean_data/Sprl2017_gold.xml");
    }
}
