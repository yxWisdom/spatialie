package edu.nju.ws.spatialie.spaceeval;

import edu.nju.ws.spatialie.data.BratUtil;
import edu.nju.ws.spatialie.utils.FileUtil;
import edu.nju.ws.spatialie.utils.UnionFind;
import edu.nju.ws.spatialie.utils.XmlUtil;
import org.dom4j.Element;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xyu
 * @version 1.0
 * @date 2019/12/26 20:00
 **/



public class ConvertToBratFormat {

    private String path;
    private String document;

    private Map<String, Span> id2EntityMap = new HashMap<>();
    private List<String> bratLines = new ArrayList<>();
    private Map<String, String> SE2BratLabelMap= new HashMap<>();
    //    private Map<String, String> idMap = new HashMap<>();
    private List<Span> allTokens = new ArrayList<>();
    private Set<String> nullEntities = new HashSet<>();

    private Set<String> elementIdInLinks = new HashSet<>();

    private  final Span NullSpan = new Span("", "", "",-1, -1);


    private int entity_id = 0;
    private int relation_id = 0;
    private int event_id = 0;
    private int attribute_id = 0;
    private int note_id = 0;

    private ConvertToBratFormat() {
        SE2BratLabelMap.put(SpaceEvalUtils.PLACE, BratUtil.PLACE);
        SE2BratLabelMap.put(SpaceEvalUtils.PATH, BratUtil.PATH);
        SE2BratLabelMap.put(SpaceEvalUtils.SPATIAL_ENTITY, BratUtil.SPATIAL_ENTITY);
        SE2BratLabelMap.put(SpaceEvalUtils.NONMOTION_EVENT,  BratUtil.EVENT);
        SE2BratLabelMap.put(SpaceEvalUtils.MOTION, BratUtil.MOTION);
        SE2BratLabelMap.put(SpaceEvalUtils.SPATIAL_SIGNAL, BratUtil.SPATIAL_SIGNAL);
        SE2BratLabelMap.put(SpaceEvalUtils.MOTION_SIGNAL, BratUtil.MOTION_SIGNAL);
        SE2BratLabelMap.put(SpaceEvalUtils.MEASURE, BratUtil.MEASURE);
        SE2BratLabelMap.put(SpaceEvalUtils.QSLINK, BratUtil.TLINK);
        SE2BratLabelMap.put(SpaceEvalUtils.OLINK, BratUtil.OLINK);
        SE2BratLabelMap.put(SpaceEvalUtils.MOVELINK, BratUtil.MLINK);
        SE2BratLabelMap.put(SpaceEvalUtils.MEASURELINK, BratUtil.DLINK);
        SE2BratLabelMap.put(SpaceEvalUtils.METALINK, BratUtil.CONFERENCE);
        SE2BratLabelMap.put("Null", "Null");
    }


    private String nextNoteId() {
        return "#" + ++note_id;
    }

    private String nextAttributeId() {
        return "A" + ++attribute_id;
    }

    private String nextEntityId() {
        return "T" + ++entity_id;
    }

    private String nextEventId() {
        return "E" + ++event_id;
    }

    private String nextRelationId() {
        return "R" + ++relation_id;
    }

    private void addNote(String nid, String tid, String note) {
        if (!note.equals("")) {
            bratLines.add(nid + "\t" + "AnnotatorNotes " + tid + "\t" + note);
        }
    }

    private void addAttribute(String aid, String tid, String attr_name, String attr_value) {
        if (attr_value.length() > 0) {
            String triple=aid + "\t" + attr_name + " " + tid + " " + attr_value;
            bratLines.add(triple);
        }
    }

    private void addEntity(String tid, String tag, int start, int end, String text) {
        if (text.length() ==  0)
            return;
        while(this.document.charAt(start) == ' ') start++;
        while(this.document.charAt(end-1) == ' ') end--;

        String text_ = this.document.substring(start, end);
        String [] spans = text_.split("\n");
        StringBuilder pos = new StringBuilder();
        assert text_.equals(text);

        end = start - 1;
        for (String span: spans) {
            start = end + 1;
            end= start + span.length();
            pos.append(String.format("%d %d;", start, end));
        }
        pos.deleteCharAt(pos.length()-1);
        text_ = text_.replaceAll("\n", " ");
        String triple=tid + "\t" + tag + " " + pos + "\t" + text_;
        bratLines.add(triple);
    }

    private void addRelation(String rid, String tag, String arg1_id, String arg2_id) {
        if (arg1_id.length() > 0 && arg2_id.length() > 0){
            String triple=rid + "\t" + tag + " Arg1:" + arg1_id + " Arg2:" + arg2_id;
            bratLines.add(triple);
        }
    }

    private void addEvent(String eid, List<String []> elements) {
        List<String> list = elements.stream()
                .filter(e -> e[1].length()>0)
                .map(e -> e[0]+":" +e[1]).collect(Collectors.toList());
        String triple=eid+"\t"+String.join(" ", list);
        bratLines.add(triple);
    }

    private void dealLocation(Element element, String tid) {
        String form = element.attributeValue("form");
        addAttribute(nextAttributeId(), tid, "form", form);
    }


    private void dealPath(Element element, String tid) {
        String form = element.attributeValue("form");
        addAttribute(nextAttributeId(), tid, "form", form);

        String beginID=element.attributeValue("beginID").toLowerCase();
        String endID=element.attributeValue("endID").toLowerCase();
        String midIDs=element.attributeValue("midIDs").toLowerCase();

        addRelation(nextRelationId(),"beginID",tid,id2EntityMap.getOrDefault(beginID, NullSpan).id);
        addRelation(nextRelationId(),"endID",  tid,id2EntityMap.getOrDefault(endID, NullSpan).id);
        addRelation(nextRelationId(),"midIDs", tid,id2EntityMap.getOrDefault(midIDs, NullSpan).id);
    }

    private void dealMotion(Element element, String tid) {
        String motion_type=element.attributeValue("motion_type").toLowerCase();
        String motion_class=element.attributeValue("motion_class").toLowerCase();
        String motion_sense=element.attributeValue("motion_sense").toLowerCase();

        addAttribute(nextAttributeId(), tid, "motion_type", motion_type);
        addAttribute(nextAttributeId(), tid, "motion_class", motion_class);
        addAttribute(nextAttributeId(), tid, "motion_sense", motion_sense);
//        motion_classes.add(motion_class);
    }

    private void dealSpatialSignal(Element element, String tid) {
        String semantic_type = element.attributeValue("semantic_type").toLowerCase();
        addAttribute(nextAttributeId(), tid, "semantic_type", semantic_type);
    }

    private void dealMotionSignal(Element element, String tid) {
        String MS_type=element.attributeValue("motion_signal_type").toLowerCase();
        addAttribute(nextAttributeId(), tid, "MS_type", MS_type);
    }

    private void dealMeasure(Element element, String tid) {
        String unit = element.attributeValue("unit");
        String value = element.attributeValue("value");
        addNote(nextNoteId(), tid, unit + " " + value);
    }




    private void dealMetaLink(Element element) {
        String fromID = element.attributeValue("fromID");
        String toID = element.attributeValue("toID");
        String relType = element.attributeValue("relType");

//        Span s1  = id2EntityMap.get(fromID), s2 = id2EntityMap.get(toID);
//        int start = Math.min(s1.start, s2.start), end = Math.max(s1.end, s2.end);
//        String text = this.document.substring(start, end);
//        boolean isCrossLines = text.contains("\n") && text.contains(".");

//        if (relType.equals("COREFERENCE") && !nullEntities.contains(fromID) && !nullEntities.contains(toID)) {
//            return;
//        }

        if (!nullEntities.contains(fromID) && !nullEntities.contains(toID)) {
            return;
        }

//        if (!nullEntities.contains(fromID) && !nullEntities.contains(toID) &&
//                elementIdInLinks.contains(fromID) && elementIdInLinks.contains(toID)) {
//            return;
//        }

        String rid = nextRelationId();
        addRelation(rid, "coreference", id2EntityMap.getOrDefault(fromID, NullSpan).id,
                id2EntityMap.getOrDefault(toID, NullSpan).id);
        addNote(nextNoteId(), rid, relType);
    }


    private Span selectEventMention(String...mentionIds) {
        for (String mentionId: mentionIds) {
            if (id2EntityMap.containsKey(mentionId))
                return id2EntityMap.get(mentionId);
        }
        return NullSpan;
    }


    private void dealQSLINK(Element element) {
        String tag = element.getName();
        String trajector=element.attributeValue("trajector");
        String landmark=element.attributeValue("landmark");
        String trigger=element.attributeValue("trigger");

        Span qslink = selectEventMention(trigger, trajector, landmark);

        if (qslink.id.equals(""))
            return;
        String tid = nextEntityId();
        addEntity(tid, SE2BratLabelMap.get(tag), qslink.start, qslink.end, qslink.text);
        String eid = nextEventId();

        String QS_type=element.attributeValue("relType");
        addAttribute(nextAttributeId(), eid, "QS_type", QS_type);

        List<String []> attributes = new ArrayList<>();
        attributes.add(new String[] {"TLINK", tid});
        attributes.add(new String[] {"trajector", id2EntityMap.getOrDefault(trajector, NullSpan).id});
        attributes.add(new String[] {"landmark", id2EntityMap.getOrDefault(landmark, NullSpan).id});
        attributes.add(new String[] {"trigger", id2EntityMap.getOrDefault(trigger, NullSpan).id});
        addEvent(eid, attributes);

        this.elementIdInLinks.addAll(Arrays.asList(trajector, landmark, trigger));
//        if (idMap.getOrDefault(trajector, NullMention).label.equals(MOTION))
//            System.out.println(tag + " " + "trajector");
//        if (idMap.getOrDefault(landmark, NullMention).label.equals(MOTION))
//            System.out.println(tag + " " + "landmark");
//        trajectors.add(idMap.getOrDefault(trajector, NullMention).label);
//        landmarks.add(idMap.getOrDefault(landmark, NullMention).label);
//        topo_types.add(QS_type);
    }


    private void dealOLINK(Element element) {
        String tag = element.getName();
        String trajector=element.attributeValue("trajector");
        String landmark=element.attributeValue("landmark");
        String trigger=element.attributeValue("trigger");
        String referencePt=element.attributeValue("referencePt");

        Span olink = selectEventMention(trigger, trajector, landmark);

        if (olink.id.equals("")) {
            System.out.print("null");
        }
        String tid = nextEntityId();
        addEntity(tid, SE2BratLabelMap.get(tag), olink.start, olink.end, olink.text);
        String eid = nextEventId();

        String O_type=element.attributeValue("relType");
        String frame_type = element.attributeValue("frame_type");
        String projective = element.attributeValue("frame_type");
        addAttribute(nextAttributeId(), eid, "O_type", O_type);
        addAttribute(nextAttributeId(), eid, "frame_type", frame_type);
        addAttribute(nextAttributeId(), eid, "projective", projective);

        List<String []> attributes = new ArrayList<>();
        attributes.add(new String[] {"OLINK", tid});
        attributes.add(new String [] {"trajector", id2EntityMap.getOrDefault(trajector, NullSpan).id});
        attributes.add(new String [] {"landmark", id2EntityMap.getOrDefault(landmark, NullSpan).id});
        attributes.add(new String [] {"trigger", id2EntityMap.getOrDefault(trigger, NullSpan).id});
        attributes.add(new String [] {"referencePt", id2EntityMap.getOrDefault(referencePt, NullSpan).id});
        addEvent(eid, attributes);

        this.elementIdInLinks.addAll(Arrays.asList(trajector, landmark, trigger, referencePt));
//        if (idMap.getOrDefault(trajector, NullMention).label.equals(MOTION))
//            System.out.println(tag + " " + "trajector");
//        if (idMap.getOrDefault(landmark, NullMention).label.equals(MOTION))
//            System.out.println(tag + " " + "landmark");
//        if (idMap.getOrDefault(trajector, NullMention).label.equals(MOTION))
//            System.out.println(tag + " " + "trajector");
//        if (idMap.getOrDefault(landmark, NullMention).label.equals(MOTION))
//            System.out.println(tag + " " + "landmark");
//        trajectors.add(idMap.getOrDefault(trajector, NullMention).label);
//        landmarks.add(idMap.getOrDefault(landmark, NullMention).label);
//        referencePts.add(idMap.getOrDefault(referencePt, NullMention).label);
//        dir_types.add(O_type);
    }


    private void dealMLINK(Element element) {
        String tag = element.getName();
        Map<String, String> roleMap = new HashMap<>();
        for (String role: SpaceEvalUtils.moveLinkRoles) {
            if (element.attributeValue(role) == null) continue;
            String [] roleValues = element.attributeValue(role).replaceAll("\\s", "").split(",");
            // gold++和train++标注不一致
            if (role.equals("landmark")) role = "ground";
            if (role.equals("adjunctID")) role = "motion_signalID";
            for (int i = 1; i <= roleValues.length; i++) {
                String roleWithIdx = i == 1 ? role: role + i;
                roleMap.put(roleWithIdx, roleValues[i-1]);
            }
        }
        Span mLink = selectEventMention(roleMap.get("trigger"), roleMap.get("mover"));
        if (mLink.id.equals(""))
            return;
        String tid = nextEntityId();
        addEntity(tid, SE2BratLabelMap.get(tag), mLink.start, mLink.end, mLink.text);
        String eid = nextEventId();
        String goal_reached=element.attributeValue("goal_reached").toLowerCase();
        addAttribute(nextAttributeId(), eid, "goal_reached", goal_reached);

        List<String []> attributes = new ArrayList<>();
        attributes.add(new String[] {"MLINK", tid});
        for (String key: roleMap.keySet()) {
            attributes.add(new String [] {key, id2EntityMap.getOrDefault(roleMap.get(key), NullSpan).id});
        }
        addEvent(eid, attributes);

        this.elementIdInLinks.addAll(roleMap.values());

//        sources.add(idMap.getOrDefault(roleMap.get("source"), NullMention).label);
//        goals.add(idMap.getOrDefault(roleMap.get("goal"), NullMention).label);
//        movers.add(idMap.getOrDefault(roleMap.get("mover"), NullMention).label);
//        grounds.add(idMap.getOrDefault(roleMap.get("ground"), NullMention).label);
    }

    private void dealDLink(Element element) {
        String tag = element.getName();
        String trajector=element.attributeValue("trajector");
        String landmark=element.attributeValue("landmark");
        String val=element.attributeValue("val");

        Span dLink = selectEventMention(val, trajector, landmark);
        if (dLink.id.equals(""))
            return;
        String tid = nextEntityId();
        addEntity(tid, SE2BratLabelMap.get(tag), dLink.start, dLink.end, dLink.text);
        String eid = nextEventId();

        List<String []> attributes = new ArrayList<>();
        attributes.add(new String[] {"DLINK", tid});
        attributes.add(new String [] {"trajector", id2EntityMap.getOrDefault(trajector, NullSpan).id});
        attributes.add(new String [] {"landmark", id2EntityMap.getOrDefault(landmark, NullSpan).id});
        attributes.add(new String [] {"val", id2EntityMap.getOrDefault(val, NullSpan).id});
        addEvent(eid, attributes);

        this.elementIdInLinks.addAll(Arrays.asList(trajector, landmark, val));

//        if (idMap.getOrDefault(trajector, NullMention).label.equals(MOTION))
//            System.out.println(tag + " " + "trajector" + " " + idMap.getOrDefault(trajector, NullMention).text);
//        if (idMap.getOrDefault(landmark, NullMention).label.equals(MOTION))
//            System.out.println("dlink " + "land");
//        trajectors.add(idMap.getOrDefault(trajector, NullMention).label);
//        landmarks.add(idMap.getOrDefault(landmark, NullMention).label);
    }


    // 将XML格式的SpaceEval2015 task8语料转换为Brat 格式
    private void convert(String inPath, String outPath) {
        this.path = inPath;
        Element root = XmlUtil.getRootElement(inPath);
        Element tags = root.element("TAGS");
        Element text = root.element("TEXT");

        this.document = text.getStringValue();

        dealNullEntity(root);

//        List<Span> a = id2EntityMap.values().stream()
//                .sorted(Comparator.comparing(o->Integer.valueOf(o.id.substring(1))))
//                .collect(Collectors.toList());

//        List<Span> b = id2EntityMap.values().stream()
//                .sorted(Comparator.comparing(o->Integer.valueOf(o.id.substring(1))))
//                .collect(Collectors.toList());

//        for (Span span: id2EntityMap.values()) {
//            addEntity(span.id, span.label, span.start, span.end, span.text);
//        }
        for(Iterator it=tags.elementIterator(); it.hasNext();) {
            Element element = (Element) it.next();
            String elementTag = element.getName();
            if (!SE2BratLabelMap.containsKey(elementTag))
                continue;
            switch (elementTag) {
                case SpaceEvalUtils.QSLINK: dealQSLINK(element);break;
                case SpaceEvalUtils.OLINK: dealOLINK(element);break;
                case SpaceEvalUtils.MOVELINK: dealMLINK(element);break;
                case SpaceEvalUtils.MEASURELINK: dealDLink(element);break;
                case SpaceEvalUtils.METALINK: dealMetaLink(element);break;
                default: dealElement(element);break;
            }
        }
        String name = inPath.substring(inPath.lastIndexOf('\\'),inPath.lastIndexOf('.'));
        String textFileName = outPath + "/" + name + ".txt";
        String annFileName = outPath + "/" +name + ".ann";
        FileUtil.writeFile(textFileName, document,false);
        FileUtil.writeFile(annFileName, bratLines, false);
    }

//    private void dealNullEntity(List<Set<String>> disjointSet) {
//
//    }

    private void dealNullEntity(Element rootElement) {
        Element tags = rootElement.element("TAGS");
        Element tokens = rootElement.element("TOKENS");

        for(Iterator it = tokens.elementIterator(); it.hasNext();) {
            Element element = (Element) it.next();
            if (!element.getName().equals("s")) continue;
            for (Iterator t_it = element.elementIterator(); t_it.hasNext();) {
                Element tokenElement = (Element) t_it.next();
                String tokenText = tokenElement.getText();
                int start = Integer.valueOf(tokenElement.attributeValue("begin"));
                int end = Integer.valueOf(tokenElement.attributeValue("end"));
                Span token = new Span("", tokenText, "", start, end);
                this.allTokens.add(token);
            }
        }

        UnionFind<String> unionFind = new UnionFind<>();

        Set<String> pathAttrs= new HashSet<>(Arrays.asList("beginID", "endID", "midIDs"));

        for(Iterator it=tags.elementIterator(); it.hasNext();) {
            Element element = (Element) it.next();
            String elementTag = element.getName();
            if (SpaceEvalUtils.allElementTags.contains(elementTag)) {
                String tag = element.getName();
                String id = element.attributeValue("id");
                String text = element.attributeValue("text");
                int start = Integer.valueOf(element.attributeValue("start"));
                int end = Integer.valueOf(element.attributeValue("end"));
                id2EntityMap.put(id, new Span(nextEntityId(), text, tag, start, end));

                if (start == -1) {
                    nullEntities.add(id);
                }
            }

            if (elementTag.equals(SpaceEvalUtils.PATH)) {
                String id = element.attributeValue("id");
                for (String pathAttr: pathAttrs) {
                    String attrValue = element.attributeValue(pathAttr);
                    if (attrValue == null ) continue;
                    String [] attrIds = attrValue.replaceAll("\\s", "").split(",");
                    for (String attrId: attrIds) {
                        if (id2EntityMap.containsKey(attrId) && id2EntityMap.get(attrId).start == -1) {
                            unionFind.unite(id, attrId);
                        }
                    }
                }
            }

            if (elementTag.equals(SpaceEvalUtils.METALINK)) {
                String fromID = element.attributeValue("fromID");
                String toID = element.attributeValue("toID");
                Span fromObj = id2EntityMap.get(fromID), toObj = id2EntityMap.get(toID);
                if (fromObj.start == - 1 || toObj.start == -1)
                    unionFind.unite(fromID, toID);
            }
            if (SpaceEvalUtils.allLinkTags.contains(elementTag)) {
                List<String> entityIds = new ArrayList<>();
                boolean hasNull = false;
                for (String role: SpaceEvalUtils.allRoles) {
                    String value = element.attributeValue(role);
                    if (value == null) continue;
                    String [] roleIds = value.replaceAll("\\s", "").split(",");
                    for (String roleId: roleIds) {
                        if (id2EntityMap.containsKey(roleId)) {
                            if (id2EntityMap.get(roleId).start == -1) {
                                hasNull = true;
                            }
                            entityIds.add(roleId);
                        }
                    }
                }
                if (hasNull) {
                    entityIds = entityIds.stream().distinct().collect(Collectors.toList());
                    for (int i = 1; i < entityIds.size(); i++) {
                        unionFind.unite(entityIds.get(i-1), entityIds.get(i));
                    }
                }
            }
        }
        List<Set<String>> disjointSet = unionFind.getDisjointSet();
        List<Span> allEntities =id2EntityMap.values().stream().sorted().collect(Collectors.toList());


//        List<Span> oriEntities = id2EntityMap.values().stream()
//                .sorted(Comparator.comparing(o->Integer.valueOf(o.id.substring(1))))
//                .collect(Collectors.toList());

        for (Set<String> set: disjointSet) {
            List<String> nonNullEntityList = set.stream().filter(o -> id2EntityMap.get(o).start > -1)
                    .sorted(Comparator.comparing(o->id2EntityMap.get(o))).collect(Collectors.toList());
            List<String> nullEntityList = set.stream().filter(o -> id2EntityMap.get(o).start == -1)
                    .sorted(Comparator.comparing(o->id2EntityMap.get(o))).collect(Collectors.toList());
//            Span midSpan = id2EntityMap.get(nonNullEntityList.get((nonNullEntityList.size()) / 2));
            Span midSpan = id2EntityMap.get(nonNullEntityList.get(0));


            LinkedList<Span> availableSpans = allTokens.stream()
                    .filter(o -> o.start > midSpan.start)
                    .filter(o -> !o.text.equals(".") && !o.text.equals(",") && !o.text.equals("(") && !o.text.equals(")"))
                    .filter(o -> {
                        int index = Collections.binarySearch(allEntities, o);
                        return index == -1 || (index < -1 && allEntities.get(-index - 2).end < o.start);
                    })
                    .sorted(Comparator.comparingInt(o -> Math.abs(o.start - midSpan.start)))
                    .collect(Collectors.toCollection(LinkedList::new));

            for (String eid: nullEntityList) {
                Span span = availableSpans.poll();
                Span oriSpan = id2EntityMap.get(eid);
                assert span != null;
                id2EntityMap.put(eid, new Span(oriSpan.id, span.text, oriSpan.label + " Null", span.start, span.end));
            }
        }
        List<Span> currentEntities = id2EntityMap.values().stream()
                .sorted(Comparator.comparing(o->Integer.valueOf(o.id.substring(1))))
                .collect(Collectors.toList());

        List<Span> nullEntities = currentEntities.stream().filter(o->o.start == -1).collect(Collectors.toList());
        if (nullEntities.size() > 0) {
            System.out.println(this.path);
        }
        for (Map.Entry<String, Span> entry: id2EntityMap.entrySet()) {
            if (entry.getValue().start == -1) {
                System.out.println(entry.getKey() + " " + entry.getValue().toString());
            }
        }
    }

    private void dealElement(Element element) {
        String tag = element.getName();
        String id = element.attributeValue("id");
//        String text = element.attributeValue("text");
//        int start = Integer.valueOf(element.attributeValue("start"));
//        int end = Integer.valueOf(element.attributeValue("end"));
        String comment = element.attributeValue("comment");

        Span span = id2EntityMap.get(id);
        String tid = span.id;

        if (span.start == -1 && span.end == -1)
            return;

        if (span.label.endsWith("Null")) {
            String ori_label = span.label.substring(0, span.label.indexOf(" "));
            comment = ori_label + " "  + comment;
            span.label = "Null";
         }

        addEntity(tid,SE2BratLabelMap.get(span.label), span.start, span.end, span.text);
        addNote(nextNoteId(), tid, comment);

        if (span.label.endsWith("Null"))
            return;

        switch (tag) {
            case SpaceEvalUtils.MOTION: dealMotion(element, tid);break;
            case SpaceEvalUtils.SPATIAL_SIGNAL: dealSpatialSignal(element,tid); break;
            case SpaceEvalUtils.MOTION_SIGNAL: dealMotionSignal(element, tid);break;
            case SpaceEvalUtils.MEASURE: dealMeasure(element, tid);
            case SpaceEvalUtils.NONMOTION_EVENT:break;
            case SpaceEvalUtils.PATH: dealPath(element, tid);break;
            default:dealLocation(element, tid); break;
        }
    }

//    Span parseElementToSpan(Element element) {
//        String tag = element.getName();
//        String id = element.attributeValue("id");
//        String text = element.attributeValue("text");
//        int start = Integer.valueOf(element.attributeValue("start"));
//        int end = Integer.valueOf(element.attributeValue("end"));
//        return new Span(id, text, tag, start, end);
//    }

    public static void main(String [] args) {
        List<File> files = FileUtil.listFiles("data/SpaceEval2015/raw_data/training++");
        for (File file:files) {
            ConvertToBratFormat convertToBratFormat = new ConvertToBratFormat();
            convertToBratFormat.convert(file.getPath(), "data/SpaceEval2015/brat_format_data/train");
        }

        files = FileUtil.listFiles("data/SpaceEval2015/raw_data/gold++");
        for (File file:files) {
            ConvertToBratFormat convertToBratFormat = new ConvertToBratFormat();
            convertToBratFormat.convert(file.getPath(), "data/SpaceEval2015/brat_format_data/test");
        }
//        ConvertToBratFormat convertToBratFormat = new ConvertToBratFormat();
//        convertToBratFormat.convert("data/SpaceEval2015/raw_data/training++/CP/46_N_23_E.xml",
//                "data/SpaceEval2015/brat_format_data/train");
    }

}

