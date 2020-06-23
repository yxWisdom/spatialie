package edu.nju.ws.spatialie.spaceeval;

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
        String placeTypeStr = "comment continent countable country ctv dcl dimensionality domain elevation form gazref gquant latLong mod scopes state text type";
        put(PLACE, Arrays.asList(placeTypeStr.trim().split(" ")));
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


    private static void constructNullPlace(int id, Element nullPlace) {
        nullPlace.addAttribute("id","pl"+id)
                .addAttribute("start","-1")
                .addAttribute("end","-1");
        for (String attr: elementAttrMap.get(PLACE)){
            nullPlace.addAttribute(attr,"");
        }
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
        String xmlPath = originXmlDir + File.separator + filename;
        SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(xmlPath);
        Map<String, Span> idToElementMap = spaceEvalDoc.getElementMap();

        Element root = XmlUtil.getRootElement(xmlPath);
        Element tagsRoot = root.element("TAGS");
        List<Element> removedElements = new ArrayList<>();
        for (Object obj: tagsRoot.elements()) {
            Element element = (Element) obj;
            switch (element.getName()) {
                case SpaceEvalUtils.QSLINK:
                case SpaceEvalUtils.OLINK:
                case SpaceEvalUtils.MOVELINK:
                case SpaceEvalUtils.MEASURELINK:
                case SpaceEvalUtils.METALINK:removedElements.add(element);
            }
        }

        for (Element element: removedElements) {
            tagsRoot.remove(element);
        }

        Element nullPlace = tagsRoot.addElement(PLACE);
        constructNullPlace(1000, nullPlace);

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


    public static void convertMHSToXML() {
        Map<String, Map<String, List<List<String>>>> fileToLinks = new LinkedHashMap<>();
        List<String> xmlInfoLines = FileUtil.readLines(xmlInfoPath);
        List<String> predictLines = FileUtil.readLines(inputPath);

        List<List<String>> xmlInfoGroups = CollectionUtils.split(xmlInfoLines, "");
        List<List<String>> predGroups = CollectionUtils.split(predictLines, "");

        for(int k = 0; k < predGroups.size(); k++) {
            List<String> xmlGroup = xmlInfoGroups.get(k);
            List<String> predGroup = predGroups.get(k);
            String curFilename = xmlGroup.get(0);
            List<String> elementIds= xmlGroup.subList(1, xmlGroup.size()).stream()
                    .map(o->o.substring(o.lastIndexOf("\t") + 1)).collect(Collectors.toList());

            if (!fileToLinks.containsKey(curFilename)) {
                Map<String, List<List<String>>>  linkMap = new LinkedHashMap<>();
                allLinkTypes.forEach(link -> linkMap.put(link, new ArrayList<>()));
                fileToLinks.put(curFilename, linkMap);
            }
            List<Pair<String, Map<String, List<String>>>> linkAttrMapList = new ArrayList<>();

            for (int i = 0; i < predGroup.size(); i++) {
                String line = predGroup.get(i).replaceAll(" ", "");
                String [] arr = line.trim().split("\t");

                String roleStr = arr[4], headStr = arr[5];
                if (roleStr.equals("[NA]")) continue;

                Map<String, Map<String, List<String>>> linkTypeToAttrMap = new HashMap<>();
                List<Integer> heads = Arrays.stream(headStr.substring(1, headStr.length() - 1).split(","))
                        .map(Integer::valueOf).collect(Collectors.toList());

                List<String> roles = Arrays.asList(roleStr.substring(1, roleStr.length() - 1).split(","));

                for (int j = 0; j < roles.size();j++) {
                    String [] roleArr = roles.get(j).split("_");
                    String role = roleArr[0], elementId = elementIds.get(heads.get(j));
                    String linkType;

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
                    } else {
                        linkTypeToAttrMap.get(linkType).get(role).add(elementId);
                    }
                }
                linkTypeToAttrMap.forEach((linkType, attrMap)-> linkAttrMapList.add(new Pair<>(linkType,attrMap)));
            }
            linkAttrMapList.forEach(pair -> {
                String linkType = pair.first;
                Map<String, List<String>>  attrMap = pair.second;
                List<List<String>> decomposedList = getDecomposedLinks(linkType, attrMap);
                fileToLinks.get(curFilename).get(linkType).addAll(decomposedList);
            });
        }
        fileToLinks.forEach(ConvertToXML::saveAsXml);
    }



    public static void main(String[] args) {
        originXmlDir = "data/SpaceEval2015/raw_data/gold";
        xmlInfoPath = "data/SpaceEval2015/processed_data/SRL_new_xml/AllLink/test.txt";
        inputPath = "data/SpaceEval2015/predict_result/SpRL/configuration3/predict.txt";
        outputDir = "data/SpaceEval2015/predict_result/SpRL/configuration3/XML";
        convertSRLToXML();

        originXmlDir = "data/SpaceEval2015/raw_data/gold";
        xmlInfoPath =  "data/SpaceEval2015/processed_data/MHS_xml/AllLink-Head/test.txt";
        inputPath = "data/SpaceEval2015/predict_result/MHS/configuration3/predict.txt";
        outputDir = "data/SpaceEval2015/predict_result/MHS/configuration3/XML";
        convertMHSToXML();
    }
}
