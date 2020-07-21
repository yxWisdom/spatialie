package edu.nju.ws.spatialie.spaceeval;

import com.alibaba.fastjson.JSONObject;
import edu.nju.ws.spatialie.utils.CollectionUtils;
import edu.nju.ws.spatialie.utils.FileUtil;
import edu.nju.ws.spatialie.utils.Pair;
import edu.nju.ws.spatialie.utils.XmlUtil;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static edu.nju.ws.spatialie.spaceeval.SpaceEvalUtils.*;


enum Config {
    CONFIG_1, CONFIG_2, CONFIG_3
}

public class ConvertToXML {

    //xml文件目录
    private static String originXmlDir = "";

    // input path 中文件对应的生成的带xml文件信息的SRL格式的输入文件
    private static String xmlInfoPath = "";

    //输出目录
    private static String outputDir="";

    //需要转换的文件
    private static String inputPath="";

    private static List<String> allLinkTypes = Arrays.asList(QSLINK, OLINK, MOVELINK, MEASURELINK);

    private static Map<String, List<String>> elementAttrMap = new HashMap<String, List<String>>(){{
        String placeAttrs = "comment continent countable country ctv dcl dimensionality domain elevation end form gazref gquant id latLong mod scopes start state text type";
        String pathAttrs = "beginID comment countable dcl dimensionality domain elevation end endID form gazref gquant id latLong midIDs mod scopes start text type";
        String seAttrs = "comment countable dcl dimensionality domain elevation end form gquant id latLong mod scopes start text type";
        String ssAttrs = "cluster comment end id semantic_type start text";
        String motionAttrs = "comment countable domain elevation end gquant id latLong mod motion_class motion_sense motion_type scopes start text";
        String eventAttrs = "comment countable domain elevation end gquant id latLong mod scopes start text";
        String msAttrs = "comment end id motion_signal_type start text";
        String measureAttrs = "comment end id start text unit value";

        put(PLACE, Arrays.asList(placeAttrs.trim().split(" ")));
        put(PATH, Arrays.asList(pathAttrs.trim().split(" ")));
        put(SPATIAL_ENTITY, Arrays.asList(seAttrs.trim().split(" ")));
        put(SPATIAL_SIGNAL, Arrays.asList(ssAttrs.trim().split(" ")));
        put(MOTION, Arrays.asList(motionAttrs.trim().split(" ")));
        put(NONMOTION_EVENT, Arrays.asList(eventAttrs.trim().split(" ")));
        put(MOTION_SIGNAL, Arrays.asList(msAttrs.trim().split(" ")));
        put(MEASURE, Arrays.asList(measureAttrs.trim().split(" ")));
    }};


    private static Map<String, String> elementAbbrMap = new HashMap<String, String>() {{
        put(PLACE, "pl");
        put(PATH, "p");
        put(SPATIAL_ENTITY, "se");
        put(SPATIAL_SIGNAL, "s");
        put(MOTION, "m");
        put(NONMOTION_EVENT, "e");
        put(MOTION_SIGNAL, "ms");
        put(MEASURE, "me");
//        put("BE", "pl");
    }};


    private static Map<String,List<String>> linkAttrMap = new HashMap<String, List<String>>() {{
        String qsLink = "comment fromID fromText id landmark relType toID toText trajector trigger";
        String oLink ="comment frame_type fromID fromText id landmark projective referencePt relType toID toText trajector trigger";
        String moveLink ="comment fromID fromText goal goal_reached id landmark midPoint motion_signalID mover pathID source toID toText trigger";
        String measureLink="comment endPoint1 endPoint2 fromID fromText id landmark relType toID toText trajector val";
        put(QSLINK, Arrays.asList(qsLink.split(" ")));
        put(OLINK, Arrays.asList(oLink.split(" ")));
        put(MOVELINK, Arrays.asList(moveLink.split(" ")));
        put(MEASURELINK,  Arrays.asList(measureLink.split(" ")));
    }};


    private static Map<String, Map<String, Integer>> linkAttrToIdxMap =  new HashMap<String, Map<String, Integer>>() {{
        linkAttrMap.forEach((linkType, attrs) -> {
            Map<String, Integer> Attr2Idx = new HashMap<>();
            for (int i = 0; i < attrs.size(); i++) {
                Attr2Idx.put(attrs.get(i), i);
            }
            put(linkType, Attr2Idx);
        });
    }};


    private static Map<String,List<String>> essentialAttrMap = new HashMap<String,List<String>>(){{
        put(QSLINK, Arrays.asList(TRAJECTOR, LANDMARK, TRIGGER));
        put(OLINK, Arrays.asList(TRAJECTOR, LANDMARK, TRIGGER));
        put(MOVELINK, Arrays.asList(MOVER, TRIGGER));
        put(MEASURELINK, Arrays.asList(TRAJECTOR, LANDMARK, TRIGGER));
    }};


    private static Map<String,String> linkAbbrMap = new HashMap<String,String>() {{
        put(QSLINK, "qsl");
        put(OLINK, "ol");
        put(MOVELINK, "mvl");
        put(MEASURELINK, "ml");
    }};


//    private static Map<String,Map<String,Integer>> countElementMap = new HashMap<>();

//    private static Map<String, Integer> countElementInit(Element root) {
//        Map<String,Integer> res = new HashMap<>();
//        allLinkTypes.forEach(tag -> res.put(tag, 0));
//        res.put(PLACE, root.elements(PLACE).size());
//        return res;
//    }


//    private static void constructNullPlace(int id, Element nullPlace) {
//        nullPlace.addAttribute("id","pl"+id)
//                .addAttribute("start","-1")
//                .addAttribute("end","-1");
//        for (String attr: elementAttrMap.get(PLACE)){
//            nullPlace.addAttribute(attr,"");
//        }
//    }

    private static void constructNullPlace(Element nullPlace) {
        constructElement(nullPlace, new Span("pl1000", "", PLACE, -1, -1));
    }

    private static void constructElement(Element element, Span spElement) {
        for (String attr: elementAttrMap.get(spElement.label)){
            element.addAttribute(attr,"");
        }
        element.addAttribute("id", spElement.id);
        element.addAttribute("start", String.valueOf(spElement.start));
        element.addAttribute("end", String.valueOf(spElement.end));
        element.addAttribute("text", spElement.text);
    }

    // 如果link的role有多个元素，按照笛卡尔积的形式分解为多个link，即{a,b}×{c} → {a,c}, {b,c}
    private  static List<List<String>> decompose(Collection<List<String>> lists) {
        List<List<String>> res = new ArrayList<>();
        for (List<String> list: lists) {
            if (res.isEmpty()) {
                res = list.stream().map(Arrays::asList).collect(Collectors.toList());
            } else {
                res = res.stream().flatMap(item -> list.stream().map(item2 -> {
                    List<String> newList = new ArrayList<>(item);
                    newList.add(item2);
                    return newList;
                })).collect(Collectors.toList());
            }
        }
        return res;
    }

    private static List<List<String>> getDecomposedLinks(String linkType, Map<String, List<String>> attrMap) {
        attrMap.forEach((attr, values) -> {
            if (values.size() == 0) {
                values.add("");
            }
        });

        Map<String, Integer> attrToIdx = linkAttrToIdxMap.get(linkType);
        List<List<String>> decomposedList = decompose(attrMap.values());

//        if (decomposedList.size() > 1) {
//            System.out.println(1);
//        }

        for (List<String> list: decomposedList) {
            String fromID, toID;
            if (linkType.equals(MOVELINK)) {
                fromID = list.get(attrToIdx.get(TRIGGER));
                toID = list.get(attrToIdx.get(MOVER));
            } else {
                fromID = list.get(attrToIdx.get(TRAJECTOR));
                toID = list.get(attrToIdx.get(LANDMARK));
            }
            list.set(attrToIdx.get("fromID"), fromID);
            list.set(attrToIdx.get("fromText"), fromID);
            list.set(attrToIdx.get("toID"), toID);
            list.set(attrToIdx.get("toText"), toID);
        }
        return decomposedList;
    }

    private static void saveAsXml(String filename, Map<String, List<List<String>>> linkMap) {
        saveAsXml(filename, linkMap, null);
    }

    private static void saveAsXml(String filename, Map<String, List<List<String>>> linkMap, Map<String, Span> predElementMap) {
        String xmlPath = originXmlDir + File.separator + filename;
        SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(xmlPath);

        Element root = XmlUtil.getRootElement(xmlPath);
        Element tagsRoot = root.element("TAGS");
        List<Element> removedElements = new ArrayList<>();
        for (Object obj: tagsRoot.elements()) {
            Element element = (Element) obj;
            if (allElementTags.contains(element.getName())) {
                if (predElementMap == null || predElementMap.containsKey(element.attributeValue("id")))
                    continue;
            }
            removedElements.add(element);
        }
        for (Element element: removedElements) {
            tagsRoot.remove(element);
        }
        Map<String, Span> goldElementMap = spaceEvalDoc.getElementMap();
        Map<String, Span> idToElementMap;

        if (predElementMap != null) {
            predElementMap.forEach((id, element)-> {
                if (goldElementMap.containsKey(id)) {
                    predElementMap.put(id, goldElementMap.get(id));
                } else {
                    Element xmlElement = tagsRoot.addElement(element.label);
                    constructElement(xmlElement, element);
                }
//                Span spElement = goldElementMap.getOrDefault(id, element);
//                predElementMap.put(id, spElement);
            });
            idToElementMap = predElementMap;
        } else {
            idToElementMap = goldElementMap;
        }

        Element nullPlace = tagsRoot.addElement(PLACE);
        constructNullPlace(nullPlace);

        Map<String, Integer> linkCountMap = new HashMap<>();
        allLinkTypes.forEach(tag -> linkCountMap.put(tag, 0));

        linkMap.forEach((linkType, linkList) ->  {
            List<String> linkAttrs = linkAttrMap.get(linkType);
            for (List<String> link: linkList) {
                Element linkElement = tagsRoot.addElement(linkType);
                for (int i = 0; i < linkAttrs.size(); i++) {
                    String attr = linkAttrs.get(i), value = link.get(i);
//                        if (!value.equals("") &&!idToElementMap.containsKey(value)){
//                            System.out.println(3);
//                        }
                    if (value.equals("") && essentialAttrMap.get(linkType).contains(attr)) {
                        value = nullPlace.attributeValue("id");
                    }

                    if (attr.equals("id")) {
                        int count = linkCountMap.get(linkType);
                        linkCountMap.put(linkType, count + 1);
                        String id = linkAbbrMap.get(linkType) + count;
                        linkElement.addAttribute(attr, id);
                    } else if(attr.equals("fromText") || attr.equals("toText")) {
                        if (!value.isEmpty() && idToElementMap.containsKey(value)) {
                            value = idToElementMap.get(value).text;
                        }
                        linkElement.addAttribute(attr, value);
                    } else {
                        linkElement.addAttribute(attr, value);
                    }
                }
            }

        });
        OutputFormat format = OutputFormat.createPrettyPrint();
        try {
            if (!FileUtil.exists(outputDir))
                FileUtil.createDir(outputDir);
            XMLWriter writer = new XMLWriter(new FileOutputStream(new File(outputDir+File.separator+filename)), format);
            writer.write(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     public static void convertSRLToXML() {
        Map<String, Map<String, List<List<String>>>> fileToLinks = new LinkedHashMap<>();
        List<String> xmlInfoLines = FileUtil.readLines(xmlInfoPath);
        List<String> linkTypes = new ArrayList<>();
        List<String> senFilenames = new ArrayList<>();
        Map<String, List<String>> senToElementIdsMap = new LinkedHashMap<>();
        for (String line: xmlInfoLines) {
            String [] arr = line.trim().split("\t");
            String filename = arr[4], sentence = arr[1], linkType = arr[0].split(" ")[2];
            List<String> elementIds = Arrays.asList(arr[5].split(" "));
            linkTypes.add(linkType);
            senFilenames.add(filename);
            senToElementIdsMap.put(filename + "#" + sentence , elementIds);
            fileToLinks.putIfAbsent(filename, new LinkedHashMap<>());
            allLinkTypes.forEach(link -> fileToLinks.get(filename).put(link, new ArrayList<>()));
        }

        List<String> predictLines = FileUtil.readLines(inputPath);
        List<List<String>> groups = CollectionUtils.split(predictLines, "");

        for (int k = 0; k < groups.size(); k++) {
            List<String> group = groups.get(k);
            String curFilename = senFilenames.get(k);
            String linkType = linkTypes.get(k);
            List<String> linkAttrs = linkAttrMap.get(linkType);
//            Map<String, Integer> attrToIdx = linkAttrToIdxMap.get(linkType);
            List<String> tokens = new ArrayList<>();
            List<String> labels = new ArrayList<>();
//            List<String> tags = new ArrayList<>();
            for (String line: group) {
                String [] arr = line.trim().split(" ");
                tokens.add(arr[0]);
//                tags.add(arr[1]);
                labels.add(arr[3]);
            }
            String sentence = String.join(" ", tokens);
            List<String> elementIds = senToElementIdsMap.get(curFilename+"#"+sentence);

            Map<String, List<String>> attrMap = new LinkedHashMap<>();
            linkAttrs.forEach(attr -> attrMap.put(attr, new ArrayList<>()));

            for (int j = 0; j<tokens.size(); j++){
                if (labels.get(j).startsWith("B-")) {
                    String role = labels.get(j).substring(2);
                    if (role.endsWith("trigger")) {
                        role = linkType.equals(MEASURELINK) ? VAL: TRIGGER;
                    }
                    if (attrMap.containsKey(role)) {
                        if (elementIds.get(j).equals("O") || elementIds.get(j).equals("")) {
                            System.err.println("Element id 为空!");
                        }
                        attrMap.get(role).add(elementIds.get(j));
                    } else {
                        System.err.println("role与link type不一致!");
                    }
                }
            }
            List<List<String>> decomposedList = getDecomposedLinks(linkType, attrMap);
            fileToLinks.get(curFilename).get(linkType).addAll(decomposedList);
        }
        fileToLinks.forEach(ConvertToXML::saveAsXml);
    }

    public static void convertNREToXML() {
        Map<String, Map<String, List<List<String>>>> fileToLinks = new LinkedHashMap<>();
        List<String> xmlInfoLines = FileUtil.readLines(xmlInfoPath);
        List<String> predictLines = FileUtil.readLines(inputPath);
        JSONObject lastT = null;
        String lastFile = null;
        List<Pair<String, Map<String, List<String>>>> linkAttrMapList = null;
        Map<String, List<String>> attrMap = null;
        String linkType;
        String lastType = null;
        boolean isDirTopLink = false;
        for (int k=0;k<predictLines.size();k++){
            JSONObject line = JSONObject.parseObject(predictLines.get(k)
                    .replaceAll("True","'True'")
                    .replaceAll("False","'False'"));
            if (line.getString("pred").equals("None")) continue;
            JSONObject infoLine = JSONObject.parseObject(xmlInfoLines.get(k));
            String xmlFileName= infoLine.getString("xmlfile");
            JSONObject t = infoLine.getJSONObject("t");
            String nreType = line.getString("pred");
            switch (nreType.toLowerCase()){
                case "mover":linkType= MOVELINK;break;
                case "locatedin":linkType= QSLINK;break;
                default:
                    if (t.getString(SEMANTIC_TYPE)==null||t.getString(SEMANTIC_TYPE).equals(TOPOLOGICAL))
                        linkType=QSLINK;
                    else {
                        linkType = OLINK;
                    }
            }
            if (!t.equals(lastT)||!linkType.equals(lastType)){
                if (attrMap!=null) {
                    linkAttrMapList.add(new Pair<>(lastType, attrMap));
                    if (isDirTopLink){
                        Map<String, List<String>> newAttrMap = new LinkedHashMap<>();
                        for (String attr:linkAttrMap.get(QSLINK)){
                            newAttrMap.put(attr, new ArrayList<>());
                        }
                        for (String attr:essentialAttrMap.get(QSLINK)){
                            newAttrMap.get(attr).addAll(attrMap.get(attr));
                        }
                        linkAttrMapList.add(new Pair<>(QSLINK, newAttrMap));
                    }
                }

                if (!xmlFileName.equals(lastFile)){
                    if (linkAttrMapList!=null) {
                        if (!fileToLinks.containsKey(lastFile)) {
                            Map<String, List<List<String>>>  linkMap = new LinkedHashMap<>();
                            allLinkTypes.forEach(link -> linkMap.put(link, new ArrayList<>()));
                            fileToLinks.put(lastFile, linkMap);
                        }
                        String finalLastFile = lastFile;
                        linkAttrMapList.forEach(pair -> {
                            String linkTypet = pair.first;
                            Map<String, List<String>> attrMapt = pair.second;
                            List<List<String>> decomposedList = getDecomposedLinks(linkTypet, attrMapt);
                            fileToLinks.get(finalLastFile).get(linkTypet).addAll(decomposedList);
                        });
                    }
                    linkAttrMapList = new ArrayList<>();
                }

                isDirTopLink = linkType.equals(OLINK) && t.getString(SEMANTIC_TYPE).equals(DIR_TOP);
                attrMap = new LinkedHashMap<>();
                for (String attr:linkAttrMap.get(linkType)){
                    attrMap.put(attr, new ArrayList<>());
                }
            }
            String tid= infoLine.getJSONObject("t").getString("id");
            String hid = infoLine.getJSONObject("h").getString("id");
            switch (nreType.toLowerCase()){
                case "mover":attrMap.get(MOVER).add(hid);if (!attrMap.get(TRIGGER).contains(tid)) attrMap.get(TRIGGER).add(tid);break;
                case "locatedin":attrMap.get(TRAJECTOR).add(hid);attrMap.get(LANDMARK).add(tid);break;
                case "landmark":attrMap.get(LANDMARK).add(hid);if (!attrMap.get(TRIGGER).contains(tid)) attrMap.get(TRIGGER).add(tid);break;
                case "trajector":attrMap.get(TRAJECTOR).add(hid);if (!attrMap.get(TRIGGER).contains(tid)) attrMap.get(TRIGGER).add(tid);break;
            }
            lastT = t;
            lastFile= xmlFileName;
            lastType=linkType;
        }
        fileToLinks.forEach(ConvertToXML::saveAsXml);
    }

    public static void convertMHSToXML(Config config ) {
        Map<String, Map<String, List<List<String>>>> fileToLinks = new LinkedHashMap<>();
        Map<String, Map<String, Span>> fileToElements = new LinkedHashMap<>();
        List<String> xmlInfoLines = FileUtil.readLines(xmlInfoPath);
        List<String> predictLines = FileUtil.readLines(inputPath);

        List<List<String>> xmlInfoGroups = CollectionUtils.split(xmlInfoLines, "");
        List<List<String>> predGroups = CollectionUtils.split(predictLines, "");

        int idCount = 2000;

        for(int k = 0; k < predGroups.size(); k++) {
            List<String> xmlGroup = xmlInfoGroups.get(k);
            List<String []> predGroup = predGroups.get(k).stream()
                    .map(line -> line.replaceAll(" ", "").split("\t"))
                    .collect(Collectors.toList());

            String curFilename = xmlGroup.get(0);
//            List<String> elementIds= xmlGroup.subList(1, xmlGroup.size()).stream()
//                    .map(o->o.substring(o.lastIndexOf("\t") + 1)).collect(Collectors.toList());
            List<String> elementIds;

            if (config == Config.CONFIG_1) {
                List<Span> tokens = xmlGroup.subList(1, xmlGroup.size()).stream()
                        .map(line -> {
                            String [] lineArr= line.replace(" ", "").split("\t", -1);
                            String elementId = lineArr[lineArr.length-1], spanStr = lineArr[lineArr.length-2];
                            String [] span = spanStr.substring(1, spanStr.length()-1).split(",");
                            int start = Integer.valueOf(span[0]), end = Integer.valueOf(span[1]);
                            return new Span(elementId, lineArr[1], lineArr[4],start, end);
                        }).collect(Collectors.toList());

                Map<String, Span> elementMap = new HashMap<>();
                for (int i = 0; i < predGroup.size(); i++) {
                    String label = predGroup.get(i)[1];
                    if (label.equals("O"))
                        tokens.get(i).id = "";
                    else if (label.startsWith("B-")){
                        label = label.substring(2);
                        Span element = tokens.get(i);
                        if (element.id.equals("")) {
//                            if (!elementAbbrMap.containsKey(label)) {
//                                System.err.println("unexpected label: "+label);
//                            }

                            element.id = elementAbbrMap.getOrDefault(label, "se") + idCount++;
                            StringBuilder text = new StringBuilder(element.text);
                            while(i + 1 < predGroup.size() && predGroup.get(i+1)[1].equals("I-"+label)) {
                                Span token = tokens.get(i+1);
                                token.id = element.id;
                                for (int j = element.end; j < token.start; j++) {
                                    text.append(" ");
                                }
                                element.end = token.end;
                                text.append(token.text);
                                i++;
                            }
                            element.text = text.toString();
                            element.label = elementAttrMap.containsKey(label) ? label: SPATIAL_ENTITY; // 处理BE标签
                        }
                        elementMap.put(element.id, element);
                    }
                }
                elementIds = tokens.stream().map(t -> t.id).collect(Collectors.toList());
//                fileToElements.put(curFilename, elementMap);
                fileToElements.putIfAbsent(curFilename, new HashMap<>());
                elementMap.forEach((id, element)-> fileToElements.get(curFilename).put(id, element));
            } else {
                elementIds= xmlGroup.subList(1, xmlGroup.size()).stream()
                        .map(o->o.substring(o.lastIndexOf("\t") + 1)).collect(Collectors.toList());
            }


            if (!fileToLinks.containsKey(curFilename)) {
                Map<String, List<List<String>>>  linkMap = new LinkedHashMap<>();
                allLinkTypes.forEach(link -> linkMap.put(link, new ArrayList<>()));
                fileToLinks.put(curFilename, linkMap);
            }
            List<Pair<String, Map<String, List<String>>>> linkAttrMapList = new ArrayList<>();

            for (int i = 0; i < predGroup.size(); i++) {
                String [] arr = predGroup.get(i);

                String roleStr = arr[4], headStr = arr[5], label = arr[1];
                if (roleStr.equals("[NA]")) continue;

                Map<String, Map<String, List<String>>> linkTypeToAttrMap = new HashMap<>();
                List<Integer> heads = Arrays.stream(headStr.substring(1, headStr.length() - 1).split(","))
                        .map(Integer::valueOf).collect(Collectors.toList());

                List<String> roles = Arrays.asList(roleStr.substring(1, roleStr.length() - 1).split(","));

                for (int j = 0; j < roles.size();j++) {
                    String [] roleArr = roles.get(j).split("_");
                    String role = roleArr[0], elementId = elementIds.get(heads.get(j));
                    String linkType;

                    if (elementId.equals("")) {
                        System.out.println(1);
                    }

                    if (roleArr.length == 1) {
                        linkType = role.equals("locatedIn") ? QSLINK: MOVELINK;
                    } else {
                        linkType = roleArr[1].equals("O") ? OLINK : QSLINK;
                    }
                    if (!linkTypeToAttrMap.containsKey(linkType)) {
                        Map<String, List<String>> attrMap = new LinkedHashMap<>();
                        linkAttrMap.get(linkType).forEach(attr->attrMap.put(attr, new ArrayList<>()));
                        linkTypeToAttrMap.put(linkType, attrMap);
                    }
                    if (roles.get(j).equals("locatedIn")) {
                        Map<String, List<String>> attrMap = new LinkedHashMap<>();
                        linkAttrMap.get(linkType).forEach(attr->attrMap.put(attr, new ArrayList<>()));
                        String fromID = elementIds.get(i);
                        attrMap.get(TRAJECTOR).add(fromID);
                        attrMap.get(LANDMARK).add(elementId);
                        linkAttrMapList.add(new Pair<>(linkType, attrMap));

                        if (fromID.equals("")) {
                            System.out.println(1);
                        }

                    } else {
                        linkTypeToAttrMap.get(linkType).get(role).add(elementId);
                        linkTypeToAttrMap.get(linkType).get(TRIGGER).add(elementIds.get(i));
                    }
                }

                linkTypeToAttrMap.forEach((linkType, attrMap) -> {
                    if (!linkType.equals(MOVELINK) && !attrMap.get(TRIGGER).isEmpty() &&
                            attrMap.get(TRAJECTOR).isEmpty() && attrMap.get(LANDMARK).isEmpty()) {
                        System.out.println("asdfasdfa");
                    }
                });
                linkTypeToAttrMap.forEach((linkType, attrMap)-> linkAttrMapList.add(new Pair<>(linkType,attrMap)));
            }
            linkAttrMapList.forEach(pair -> {
                String linkType = pair.first;
                Map<String, List<String>>  attrMap = pair.second;
                List<List<String>> decomposedList = getDecomposedLinks(linkType, attrMap);
                fileToLinks.get(curFilename).get(linkType).addAll(decomposedList);
            });
        }
        fileToLinks.forEach((filename, links) -> {
            Map<String, Span> elementMap = fileToElements.getOrDefault(filename, null);
            saveAsXml(filename, links, elementMap);
        });
    }



    public static void main(String[] args) {
        originXmlDir = "data/SpaceEval2015/raw_data/gold";
        xmlInfoPath = "data/SpaceEval2015/processed_data/SRL_new_xml/AllLink/test.txt";
        inputPath = "data/SpaceEval2015/predict_result/SpRL/configuration3/predict.txt";
        outputDir = "data/SpaceEval2015/predict_result/SpRL/configuration3/XML";
        convertSRLToXML();


        originXmlDir = "data/SpaceEval2015/raw_data/gold";
        xmlInfoPath =  "data/SpaceEval2015/processed_data/MHS_xml/configuration1_1/AllLink-Head/test.txt";
        inputPath = "data/SpaceEval2015/predict_result/MHS/configuration1_1/predict.txt";
        outputDir = "data/SpaceEval2015/predict_result/MHS/configuration1_1/XML";
        convertMHSToXML(Config.CONFIG_1);


        originXmlDir = "data/SpaceEval2015/raw_data/gold";
        xmlInfoPath =  "data/SpaceEval2015/processed_data/MHS_xml/configuration1_2/AllLink-Head/test.txt";
        inputPath = "data/SpaceEval2015/predict_result/MHS/configuration1_2/predict.txt";
        outputDir = "data/SpaceEval2015/predict_result/MHS/configuration1_2/XML";
        convertMHSToXML(Config.CONFIG_1);



        originXmlDir = "data/SpaceEval2015/raw_data/gold";
        xmlInfoPath =  "data/SpaceEval2015/processed_data/MHS_xml/configuration2/AllLink-Head/test.txt";
        inputPath = "data/SpaceEval2015/predict_result/MHS/configuration2/predict.txt";
        outputDir = "data/SpaceEval2015/predict_result/MHS/configuration2/XML";
        convertMHSToXML(Config.CONFIG_2);

        originXmlDir = "data/SpaceEval2015/raw_data/gold";
        xmlInfoPath =  "data/SpaceEval2015/processed_data/MHS_xml/configuration3/AllLink-Head/test.txt";
        inputPath = "data/SpaceEval2015/predict_result/MHS/configuration3/predict.txt";
        outputDir = "data/SpaceEval2015/predict_result/MHS/configuration3/XML";
        convertMHSToXML(Config.CONFIG_3);


        originXmlDir = "data/SpaceEval2015/raw_data/gold";
        xmlInfoPath =  "data/SpaceEval2015/processed_data/MHS_xml/configuration3/AllLink-Head/test.txt";
        inputPath = "data/SpaceEval2015/predict_result/MHS/configuration3_2/predict.txt";
        outputDir = "data/SpaceEval2015/predict_result/MHS/configuration3_2/XML";
        convertMHSToXML(Config.CONFIG_3);


        originXmlDir = "data/SpaceEval2015/raw_data/gold";
        xmlInfoPath =  "data/SpaceEval2015/processed_data/MHS_xml/configuration3/AllLink-Head/test.txt";
        inputPath = "data/SpaceEval2015/predict_result/MHS/configuration3_3/predict.txt";
        outputDir = "data/SpaceEval2015/predict_result/MHS/configuration3_3/XML";
        convertMHSToXML(Config.CONFIG_3);

        originXmlDir = "data/SpaceEval2015/raw_data/gold";
        xmlInfoPath =  "data/SpaceEval2015/processed_data/openNRE_xml/AllLink_1000_100/test.txt";
        inputPath = "data/SpaceEval2015/predict_result/openNRE/configuration3/predict.txt";
        outputDir = "data/SpaceEval2015/predict_result/openNRE/configuration3/XML";
        convertNREToXML();
    }
}
