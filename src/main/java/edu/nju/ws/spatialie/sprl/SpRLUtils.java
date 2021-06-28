package edu.nju.ws.spatialie.sprl;

import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.spaceeval.Span;
import edu.nju.ws.spatialie.utils.FileUtil;
import edu.nju.ws.spatialie.utils.XmlUtil;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.*;

import static edu.nju.ws.spatialie.sprl.SpRLUtils.*;

/**
 * @author xyu
 * @version 1.0
 * @date 2021/5/8 16:13
 */

class Sentence

{
    List<Span> elements = new ArrayList<>();
    Map<String, Span> elementMap = new HashMap<>();
    List<BratEvent> links = new ArrayList<>();
    List<Span> tokens = new ArrayList<>();
    String text;

    Sentence(Element sentence) {
        String sentenceText = sentence.elementText("CONTENT").trim();
        this.text = sentenceText;
        List<String> words = Arrays.asList(sentenceText.trim().split(" "));
//        Set<String> ids = new HashSet<>();
        for (Iterator<?> it = sentence.elementIterator(); it.hasNext();) {
            Element element = (Element) it.next();
            String tag = element.getName();
            if (SpRLUtils.roleTypes.contains(tag)) {
                String id = element.attributeValue("id");
                String text = element.getText().trim();
                if (!id.startsWith("sw") && !id.startsWith("lw") && !id.startsWith("tw")) {
                    System.err.println("id不合法！");
                }
                if (text.toLowerCase().equals("undefined")) {
                    continue;
                }
                int start = Integer.parseInt(id.substring(2));
                int end = start + text.split(" ").length - 1;

                String span1 = String.join(" ", words.subList(start, end+1)).toLowerCase();
                if (!span1.equals(text.toLowerCase())) {
                    System.err.printf("span不一致：[%s:%s/%s] -- %s\n", id, span1, text.toLowerCase(), sentenceText);
                }

                id = id.substring(1);
                String label = tag.equals(SPATIAL_INDICATOR) ? SPATIAL_SIGNAL : SPATIAL_ENTITY;
                Span span = new Span(id, text, label, start, end);
                elements.add(span);
                elementMap.put(id, span);
            }
        }
        // parse elements
        Collections.sort(elements);

        // parse relations
        for (Iterator<?> it = sentence.elementIterator("RELATION"); it.hasNext();) {
            Element relation = (Element) it.next();
            BratEvent link = new BratEvent();
            String  linkType = relation.attributeValue("general_type").trim().toUpperCase();
            link.setId(relation.attributeValue("id"));
            link.setType(linkType);
            attr2roleMap.forEach((attr, role) -> {
                String roleId = relation.attributeValue(attr).substring(1);
                if (!attr.startsWith(relation.attributeValue(attr).substring(0,1))) {
                    System.err.println("不匹配的角色类型！");
                }
                if (!elementMap.containsKey(roleId))
                    return;
                link.addRole(role, roleId);
            });
            links.add(link);
        }

        // parse tokens
        for (int i = 0; i < words.size(); i++) {
            tokens.add(new Span("", words.get(i), "O", i, i));
        }

        for (Span element: elements) {
            int start = element.start, end = element.end;

            if (!tokens.get(start).label.equals("O")) {
                if (tokens.get(start).label.startsWith("I-")) {
                    System.err.println("Token Overlap (I-): " + sentenceText);
                } else {
                    String curLabel = tokens.get(start).label.substring(2);
                    if (!curLabel.equals(element.label)) {
                        System.err.println("Token Overlap：" + sentenceText);
                    }
                }
            }

            tokens.get(start).label =  "B-" + element.label;
            tokens.get(start).id = element.id;
            for (int i = start+1; i <= end; i++) {

                if (!tokens.get(i).label.equals("O")) {
                    if (tokens.get(i).label.startsWith("B-")) {
                        System.err.println("Token Overlap (B-)");
                    } else {
                        String curLabel = tokens.get(i).label.substring(2);
                        if (!curLabel.equals(element.label)) {
                            System.err.println("Token Overlap");
                        }
                    }
                }
                tokens.get(i).label =  "I-" + element.label;
                tokens.get(i).id =  element.id;
            }
        }

    }
}


class SpRLDocument {
    List<Sentence> sentences = new ArrayList<>();
    String path;

    public SpRLDocument(String filePath) {
        this.path = filePath;
        Element root = XmlUtil.getRootElement(filePath);
        for (Object sentenceObj : root.elements("SENTENCE")) {
            Element sentenceEle = (Element) sentenceObj;
            Sentence sentence = new Sentence(sentenceEle);
            sentences.add(sentence);
        }
//        for (Object sceneObj : root.elements("DOC")) {
//            Element scene = (Element) sceneObj;
//
//        }
    }
}


public class SpRLUtils {
    static final String TRAJECTOR="trajector";
    static final String LANDMARK="landmark";
    static final String TRIGGER="trigger";
    static final String SPATIAL_ENTITY="SPATIAL_ENTITY";
    static final String SPATIAL_SIGNAL="SPATIAL_SIGNAL";
    static final String SPATIAL_INDICATOR = "SPATIAL_INDICATOR";
    static List<String> roleTypes = Arrays.asList("SPATIAL_INDICATOR", "TRAJECTOR", "LANDMARK");
    static List<String> attrTypes = Arrays.asList("sp", "tr", "ld");
    static Map<String, String> attr2roleMap = new HashMap<String, String>() {{
        put("sp", "trigger");
        put("tr", "trajector");
        put("lm", "landmark");
    }};

    public static void prettyFormat(String inDir, String outDir) {
        List<File> files = FileUtil.listFiles(inDir);
        for (File file: files) {
            System.out.println(file.getName());
            String fileName = file.getName();
            String outPath = outDir + "/" + fileName;
            Element root = XmlUtil.getRootElement(file.getPath());
            try {
                OutputFormat format = OutputFormat.createPrettyPrint();
                format.setIndentSize(4);
                format.setPadText(false);
                XMLWriter writer;
                writer = new XMLWriter(new FileOutputStream(new File(outPath)), format);
                writer.write(root);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static void cleaningData(String inDir, String outDir) {
        List<File> files = FileUtil.listFiles(inDir);
        for (File file: files) {
            String inPath = file.getPath();
            String fileName = file.getName();
            String outPath = outDir + "/" + fileName;
            Element root = XmlUtil.getRootElement(inPath);
            for (Object sentenceObj : root.elements("SENTENCE")) {
                Element sentence = (Element) sentenceObj;
                String sentenceText = sentence.elementText("CONTENT").trim();
                List<String> words = Arrays.asList(sentenceText.trim().split(" "));

                Map<String, String> eleIdMap = new HashMap<>();
//        Set<String> ids = new HashSet<>();
                for (Iterator<?> it = sentence.elementIterator(); it.hasNext();) {
                    Element element = (Element) it.next();
                    String tag = element.getName();
                    if (SpRLUtils.roleTypes.contains(tag)) {
                        String id = element.attributeValue("id");
                        String text = element.getText().trim();
                        String prefix = id.substring(0,2);
                        if (!id.startsWith("sw") && !id.startsWith("lw") && !id.startsWith("tw")) {
                            System.err.println("id不合法！");
                        }
                        if (text.toLowerCase().equals("undefined")) {
                            continue;
                        }
                        int start = Integer.parseInt(id.substring(2));
                        int end = start + text.split(" ").length - 1;

                        for (int i = 0; i<2 && start >= 0; i++) {
                            String span1 = String.join(" ", words.subList(start, end+1)).toLowerCase();
                            if (span1.equals(text.toLowerCase())) {
                                if (i != 0) {
                                    String newId = prefix + start;
                                    element.addAttribute("id", newId);
                                    eleIdMap.putIfAbsent(id, newId);
                                }
                                break;
                            }
                            start -= 1;
                            end -= 1;
                        }
                    }
                }
                for (Iterator<?> it = sentence.elementIterator("RELATION"); it.hasNext();) {
                    Element relation = (Element) it.next();
                    attr2roleMap.forEach((attr, role) -> {
                        String roleId = relation.attributeValue(attr);
                        if (!attr.startsWith(relation.attributeValue(attr).substring(0,1))) {
                            System.err.println("不匹配的角色类型！");
                        }
                        if (eleIdMap.containsKey(roleId)) {
                            relation.addAttribute(attr, eleIdMap.get(roleId));
                        }
                    });
                }

            }
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setIndentSize(4);
            format.setPadText(false);
            try {
                XMLWriter writer = new XMLWriter(new FileOutputStream(new File(outPath)), format);
                writer.write(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    static void checkElementOverlap(String path) {
        List<File> files = FileUtil.listFiles(path);
        for (File file: files) {
            System.out.println(file.getName());
            SpRLDocument doc = new SpRLDocument(file.getPath());
            Set<String> sentenceSet = new HashSet<>();
            doc.sentences.forEach(sentence -> {
                String text = sentence.text;
                if (sentenceSet.contains(text)) {
                    System.out.println(text);
                } else {
                    sentenceSet.add(text);
                }
            });
        }
    }

    static void runCleaningData() {
        String rawDir = "data/SpRL2012/raw_data";
        String inDir = "data/SpRL2012/cleaning_data/input";
        String outDir = "data/SpRL2012/cleaning_data/output";
        prettyFormat(rawDir,inDir);
        cleaningData(inDir, outDir);
    }
    public static void main(String [] args) {
//        runCleaningData();
//        prettyFormat();
        checkElementOverlap("data/SpRL2012/modified_data_bak");
    }
}
