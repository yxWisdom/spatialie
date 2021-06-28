package edu.nju.ws.spatialie.msprl;

import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.spaceeval.Span;
import edu.nju.ws.spatialie.utils.FileUtil;
import edu.nju.ws.spatialie.utils.XmlUtil;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;


/**
 * @author xyu
 * @version 1.0
 * @date 2021/3/26 23:11
 */
public class MSpRLUtils {
    static final String SPATIAL_ENTITY="SPATIAL_ENTITY";
    static final String SPATIAL_SIGNAL="SPATIAL_SIGNAL";
    static final String SPATIALINDICATOR = "SPATIALINDICATOR";
    static final String TRIGGER="trigger";
    static final String TRAJECTOR="trajector";
    static final String LANDMARK="landmark";

    static final String QSLINK = "QSLINK";
    static final String OLINK = "OLINK";
    static final String MEASURELINK = "MEASURELINK";
    static final String REGION = "REGION";
    static final String DIRECTION = "DIRECTION";
    static final String DISTANCE = "DISTANCE";

    static Set<String> distanceTypes = new HashSet<>(Arrays.asList("10METERS", "CLOSE", "FAR", "LAST", "MIDDLE", "NEAR", "SECOND"));
    static Set<String>  directionTypes = new HashSet<>(Arrays.asList("ABOVE", "BEHIND", "BELOW", "FRONT", "LEFT", "RIGHT"));
    static Set<String>  regionTypes= new HashSet<>(Arrays.asList("DC", "EC", "EQ", "NTPP", "NTPPI", "NTTP", "PO", "TPP", "TPPI"));

    static List<String> roleTypes = Arrays.asList("SPATIALINDICATOR", "LANDMARK", "TRAJECTOR");
    static List<String> attrNames = Arrays.asList("spatial_indicator_id", "trajector_id", "landmark_id");
    static Map<String, String> roleMap = new HashMap<String, String>() {{
        put("spatial_indicator_id", "trigger");
        put("trajector_id", "trajector");
        put("landmark_id", "landmark");
    }};

//    static Map<String, String> generalType2LinkType = new HashMap<String, String>() {{
//        put(REGION, QSLINK);
//        put(DIRECTION, OLINK);
//        put(DISTANCE, MEASURELINK);
//    }};


    public static void cleaningData(String inPath, String outPath) {
        Set<String> roles = new HashSet<>(Arrays.asList("SPATIALINDICATOR", "LANDMARK", "TRAJECTOR"));
        List<String> roleTypes = Arrays.asList("spatial_indicator_id", "trajector_id", "landmark_id");
        Element root = XmlUtil.getRootElement(inPath);
        for (Object sceneObj: root.elements("SCENE")) {
            Element scene = (Element) sceneObj;
            for (Object sentenceObj : scene.elements("SENTENCE")) {
                Map<String, String> span2id = new HashMap<>();
                Map<String, String> id2span = new HashMap<>();
                Set<String> usedElementIds = new HashSet<>();
                Element sentence = (Element) sentenceObj;
                String sentenceText = sentence.elementText("TEXT");

                List<Span> elements = new ArrayList<>();
                for (Object obj : sentence.elements("RELATION")) {
                    Element element = (Element) obj;
                    roleTypes.forEach(type -> usedElementIds.add(element.attributeValue(type)));
                }
                for (Iterator<?> it = sentence.elementIterator(); it.hasNext(); ) {
                    Element element = (Element) it.next();
                    String tag = element.getName();
                    if (roles.contains(tag)) {
                        String id = element.attributeValue("id");
                        if (!usedElementIds.contains(id)) {
                            it.remove();
                            continue;
                        }
                        int start = Integer.parseInt(element.attributeValue("start"));
                        int end = Integer.parseInt(element.attributeValue("end"));

                        if (start != -1 && end != -1) {
                            String text = element.attributeValue("text");
                            if (text.trim().equals("")) {
                                System.err.println("text为空");
                            }
                            if (!sentenceText.substring(start, end).equals(text)) {
                                System.err.println("text span错误");
                            }
                            while (start < end && sentenceText.charAt(start) == ' ') {
                                start++;
                            }
                            while (start < end && sentenceText.charAt(end - 1) == ' ') {
                                end--;
                            }
                            if (start == end) {
                                System.err.println("span错误");
                            }

                            String newText = sentenceText.substring(start, end);
                            if (!text.trim().equals(newText)) {
                                System.err.println("non space trim");
                            }
                            element.addAttribute("start", String.valueOf(start));
                            element.addAttribute("end", String.valueOf(end));
                            element.addAttribute("text", newText);
                        }
//                        String span = tag + " " + start + " " + end;
                        String span =  start + " " + end;

                        if (span2id.containsKey(span)) {
                            System.out.println("span重复： " + sentenceText);
                            it.remove();
                        } else {
                            span2id.put(span, id);
                            elements.add(new Span(id, element.attributeValue("text"), "", start, end));
                        }
                        if (id2span.containsKey(id)) {
                            System.out.println("id重复: " + sentenceText);
                            it.remove();
                        } else {
                            id2span.put(id, span);
                        }

                    }
                }
                elements.sort(Comparator.comparing((Span x) -> x.start).thenComparing(x -> x.end));
                Map<String, String> discardedIdMap = new HashMap<>();
                for (int i = 1; i < elements.size(); i++) {
                    Span pre = elements.get(i - 1), cur = elements.get(i);
                    if (cur.start == pre.start && cur.end == pre.end) continue;
                    if (cur.start < pre.end) {
                        String discardId = cur.text.length() > pre.text.length() ? pre.id : cur.id;
                        String retainedId = cur.text.length() > pre.text.length() ? cur.id : pre.id;
                        discardedIdMap.put(discardId, retainedId);
                        span2id.remove(id2span.get(discardId));
                        id2span.put(discardId, id2span.get(retainedId));
                        System.out.println("span重叠！" + sentenceText);
                        System.out.printf("%s:%s  -  %s:%s\n", pre.text, pre.label, cur.text, cur.label);
                        System.out.println();
                    }
                }

                for (Iterator<?> it = sentence.elementIterator(); it.hasNext(); ){
                    Element element = (Element) it.next();
                    if (discardedIdMap.containsKey(element.attributeValue("id"))) {
                        it.remove();
                    }
                }

                for (Iterator<?> it = sentence.elementIterator("RELATION"); it.hasNext(); ) {
                    Element relation = (Element) it.next();
                    for (String attrName : roleTypes) {
                        String id = relation.attributeValue(attrName);
                        String newId = span2id.get(id2span.get(id));
                        relation.addAttribute(attrName, newId);
                    }
                    String linkType = relation.attributeValue("general_type");
                    String relType = relation.attributeValue("RCC8_value");
                    if (linkType.equals(REGION) && directionTypes.contains(relType)) {
                        relation.addAttribute("general_type", DIRECTION);
                    }
                    if (linkType.equals(DIRECTION) && regionTypes.contains(relType)) {
                        relation.addAttribute("general_type", REGION);
                    }
                    if (linkType.equals(DISTANCE) && directionTypes.contains(relType)) {
                        relation.addAttribute("general_type", DIRECTION);
                    }
                }
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

    public static void analysisRelTypes() {
//        Map<String, Integer> type2Cnt = new HashMap<>();
        List<File> files = FileUtil.listFiles("data/mSpRL2017/clean_data");
        for (File file: files) {
            MSpRLDoc doc = new MSpRLDoc(file.getPath());
            Map<String, Map<String, Integer>> linkType2relTypeCnt = new TreeMap<>();
            Map<String, Map<String, Integer>> token2relTypes = new HashMap<>();
            for (SpRLSentence sentence : doc.sentences) {
                for (BratEvent link : sentence.links) {
                    String relType = link.getAttribute("relType");
                    linkType2relTypeCnt.putIfAbsent(link.getType(), new TreeMap<>());
                    int cnt = linkType2relTypeCnt.get(link.getType()).getOrDefault(relType, 0);
                    linkType2relTypeCnt.get(link.getType()).put(relType, cnt+1);
                    String token = sentence.elementMap.get(link.getRoleId(TRIGGER)).text;
                    token2relTypes.putIfAbsent(token, new HashMap<>());
                    cnt = token2relTypes.get(token).getOrDefault(relType, 0);
                    token2relTypes.get(token).put(relType, cnt+1);
                }
            }
            List<String> lines = new ArrayList<>();
            linkType2relTypeCnt.forEach((link, relType2Cnt) -> {
                lines.add(link);
                relType2Cnt.forEach((relType, cnt) -> lines.add(relType + " " + cnt));
                lines.add("");
            });

            token2relTypes.forEach((token, relType2Cnt) -> {
                lines.add(token);
                relType2Cnt.forEach((relType, cnt) -> lines.add(relType + " " + cnt));
                lines.add("");
            });

//            FileUtil.writeFile("data/mSpRL2017/analysis/relTypes.txt", lines);
            String name = file.getName().substring(0, file.getName().lastIndexOf("."));
            FileUtil.writeFile("data/mSpRL2017/analysis/" + name + ".txt", lines);
        }
    }


    public static void analysisRelTypesOfTokens() {
//        Map<String, Integer> type2Cnt = new HashMap<>();
        List<File> files = FileUtil.listFiles("data/mSpRL2017/clean_data");
        for (File file: files) {
            MSpRLDoc doc = new MSpRLDoc(file.getPath());
            Map<String, Map<String, Integer>> linkType2relTypeCnt = new TreeMap<>();
            Map<String, Map<String, Integer>> token2relTypes = new HashMap<>();
            for (SpRLSentence sentence : doc.sentences) {
                for (BratEvent link : sentence.links) {
                    String relType = link.getAttribute("relType");
                    linkType2relTypeCnt.putIfAbsent(link.getType(), new TreeMap<>());
                    int cnt = linkType2relTypeCnt.get(link.getType()).getOrDefault(relType, 0);
                    linkType2relTypeCnt.get(link.getType()).put(relType, cnt+1);
                    String token = sentence.elementMap.get(link.getRoleId(TRIGGER)).text;
                    token2relTypes.putIfAbsent(token, new HashMap<>());
                    cnt = token2relTypes.get(token).getOrDefault(link.getType(), 0);
                    token2relTypes.get(token).put(relType, cnt+1);
                }
            }
            List<String> lines = new ArrayList<>();
            linkType2relTypeCnt.forEach((link, relType2Cnt) -> {
                lines.add(link);
                relType2Cnt.forEach((relType, cnt) -> lines.add(relType + " " + cnt));
                lines.add("");
            });

            token2relTypes.forEach((token, relType2Cnt) -> {
                lines.add(token);
                relType2Cnt.forEach((relType, cnt) -> lines.add(relType + " " + cnt));
                lines.add("");
            });

//            FileUtil.writeFile("data/mSpRL2017/analysis/relTypes.txt", lines);
            String name = file.getName().substring(0, file.getName().lastIndexOf("."));
            FileUtil.writeFile("data/mSpRL2017/analysis/" + name + ".txt", lines);
        }
    }

    public static void main(String [] args) {
//        cleaningData("data/mSpRL2017/modified_data/Sprl2017_train.xml",
//                "data/mSpRL2017/clean_data/Sprl2017_train.xml");
//        cleaningData("data/mSpRL2017/modified_data/Sprl2017_gold.xml",
//                "data/mSpRL2017/clean_data/Sprl2017_gold.xml");
        analysisRelTypes();
    }

}

