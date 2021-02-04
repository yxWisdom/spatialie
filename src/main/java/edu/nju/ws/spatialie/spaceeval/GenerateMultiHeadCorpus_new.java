package edu.nju.ws.spatialie.spaceeval;

import com.google.common.collect.Multimap;
import com.sun.org.apache.bcel.internal.generic.LAND;
import edu.nju.ws.spatialie.Link.LINK;
import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.data.Sentence;
import edu.nju.ws.spatialie.utils.CollectionUtils;
import edu.nju.ws.spatialie.utils.FileUtil;
import edu.nju.ws.spatialie.utils.Pair;
import edu.nju.ws.spatialie.utils.StanfordNLPUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static edu.nju.ws.spatialie.spaceeval.SpaceEvalUtils.*;


// INFO: 取消trajector和landmark的后缀(_Q, _O), 通过trigger的semantic type判断是，
public class GenerateMultiHeadCorpus_new {
    static Set<String> acceptedLabels = null;
    static boolean onlyCoreRole = true;


//    private static List<Triple<String, String, String>> getBidirectionalSPOTriples(List<BratEvent> links) {
//        List<Triple<String, String, String>> triples = new ArrayList<>();
//        for (BratEvent link: links) {
//            Multimap<String, String> roleMap = link.getRoleMap();
//            if (link.getType().equals(SpaceEvalUtils.MOVELINK)) {
//                if (roleMap.containsKey(MOVER) && roleMap.containsKey(TRIGGER)) {
//                    for (String mover: roleMap.get(MOVER)) {
//                        for (String trigger: roleMap.get(TRIGGER)) {
//                            triples.add(new ImmutableTriple<>(mover, MOVER, trigger));
//                            triples.add(new ImmutableTriple<>(trigger, TRIGGER, mover));
//                        }
//                    }
//                }
//            } else {
//                if (roleMap.containsKey("trigger") || roleMap.containsKey("val")) {
//                    for (String trajector: roleMap.get(TRAJECTOR)) {
//                        for (String trigger: roleMap.get(TRIGGER)) {
//                            triples.add(new ImmutableTriple<>(trajector, TRAJECTOR, trigger));
//                            triples.add(new ImmutableTriple<>(trigger, TRIGGER, trajector));
//                        }
//                    }
//                    for (String landmark: roleMap.get(LANDMARK)) {
//                        for (String trigger: roleMap.get(TRIGGER)) {
//                            triples.add(new ImmutableTriple<>(landmark, LANDMARK, trigger));
//                            triples.add(new ImmutableTriple<>(trigger, TRIGGER, landmark));
//                        }
//                    }
//                } else {
//                    for (String trajector: roleMap.get(TRAJECTOR)) {
//                        for (String landmark: roleMap.get(LANDMARK)) {
//                            triples.add(new ImmutableTriple<>(trajector, "locatedIn", landmark));
//                            triples.add(new ImmutableTriple<>(landmark, "include", trajector));
//                        }
//                    }
//                }
//            }
//        }
//        return triples.stream().distinct().collect(Collectors.toList());
//    }
//
//    private static List<Triple<String, String, String>> getSPOTriples(List<BratEvent> links) {
//
//        List<Triple<String, String, String>> triples = new ArrayList<>();
//        for (BratEvent link: links) {
//            Multimap<String, String> roleMap = link.getRoleMap();
//            if (link.getType().equals(SpaceEvalUtils.MOVELINK)) {
//                if (roleMap.containsKey(MOVER) && roleMap.containsKey(TRIGGER)) {
//                    for (String mover: roleMap.get(MOVER)) {
//                        for (String trigger: roleMap.get(TRIGGER)) {
//                            triples.add(new ImmutableTriple<>(mover, MOVER, trigger));
//                        }
//                    }
//                }
//            } else {
//                if (roleMap.containsKey("trigger") || roleMap.containsKey("val")) {
//                    for (String trajector: roleMap.get(TRAJECTOR)) {
//                        for (String trigger: roleMap.get(TRIGGER)) {
//                            triples.add(new ImmutableTriple<>(trajector, TRAJECTOR, trigger));
//                        }
//                    }
//                    for (String landmark: roleMap.get(LANDMARK)) {
//                        for (String trigger: roleMap.get(TRIGGER)) {
//                            triples.add(new ImmutableTriple<>(landmark, LANDMARK, trigger));
//                        }
//                    }
//                } else {
//                    for (String trajector: roleMap.get(TRAJECTOR)) {
//                        for (String landmark: roleMap.get(LANDMARK)) {
//                            triples.add(new ImmutableTriple<>(trajector, "locatedIn", landmark));
//                        }
//                    }
//                }
//            }
//        }
//        return triples.stream().distinct().collect(Collectors.toList());
//    }


    private static List<Triple<String, String, String>> getSPOTriples(List<BratEvent> links) {
        Map<String, String> trigger2linkType = new HashMap<>();
        Map<String, Map<String, String>> trigger2roleMap = new HashMap<>();

        List<Triple<String, String, String>> triples = new ArrayList<>();
        for (BratEvent link: links) {
            String linkType = link.getType();
            Multimap<String, String> roleMap = link.getRoleMap();
            if (link.getType().equals(MOVELINK)) {
                Collection<String> triggers = roleMap.get(TRIGGER);
                Set<String> roleTypes = new HashSet<String>(){{add(MOVER);}};
                if (!onlyCoreRole) roleTypes.addAll(mLinkOptionalRoles);

                for (String roleType: roleTypes) {
                    Collection<String> roles = roleMap.get(roleType);
                    roles.forEach(roleId -> triggers.forEach(trigger -> {
                        trigger2linkType.putIfAbsent(trigger, linkType);
                        trigger2roleMap.putIfAbsent(trigger, new HashMap<>());
                        if (!linkType.equals(trigger2linkType.get(trigger))) {
                            System.out.println("trigger有多个link type!");
                            System.exit(-1);
                        }
                        trigger2roleMap.get(trigger).putIfAbsent(roleId, roleType);
                        String curRoleType = trigger2roleMap.get(trigger).get(roleId);
                        if (!curRoleType.equals(roleType)) {
                            System.out.println("MoveLink: 一个token与trigger有多个role type!");
                        }
                    }));
                }
            } else {
                Collection<String> trajectors = roleMap.get(TRAJECTOR);
                Collection<String> landmarks = roleMap.get(LANDMARK);
                Set<String> roleTypes = new HashSet<>(Arrays.asList(TRAJECTOR, LANDMARK));
                if (roleMap.containsKey(TRIGGER) || roleMap.containsKey(VAL)) {
                    Collection<String> triggers = roleMap.containsKey(TRIGGER) ? roleMap.get(TRIGGER) : roleMap.get(VAL);
                    for (String roleType: roleTypes) {
                        Collection<String> roles = roleMap.get(roleType);
                        roles.forEach(roleId -> triggers.forEach(trigger -> {
                            trigger2linkType.putIfAbsent(trigger, linkType);
                            trigger2roleMap.putIfAbsent(trigger, new HashMap<>());
                            if (!linkType.equals(trigger2linkType.get(trigger))) {
                                trigger2linkType.put(trigger, OTLINK);
                            }
                            trigger2roleMap.get(trigger).putIfAbsent(roleId, roleType);
                            String curRoleType = trigger2roleMap.get(trigger).get(roleId);
                            if (!curRoleType.equals(roleType)) {
                                System.out.println("一个token与trigger有多个role type!");
                            }
                        }));
                    }
                } else {
                    trajectors.forEach(trajector ->
                            landmarks.forEach(landmark ->
                                    triples.add(new ImmutableTriple<>(trajector, "locatedIn", landmark))));
                }
            }
        }
        trigger2roleMap.forEach((trigger, roleMap) -> {
            String linkType = trigger2linkType.get(trigger);
            roleMap.forEach((roleId, roleType) -> {
                String relation = roleType;
                if (linkType.equals(QSLINK) || linkType.equals(OLINK)) {
                    relation = roleType + "_" + linkType.charAt(0);
                }
                triples.add(new ImmutableTriple<>(trigger, relation, roleId));
            });
        });
        return triples.stream().distinct().collect(Collectors.toList());
    }



    private static List<Triple<String, String, String>> getBidirectionalSPOTriples(List<BratEvent> links) {
        List<Triple<String, String, String>> triples = new ArrayList<>();
        for (BratEvent link: links) {
            char linkType = link.getType().charAt(0);
            Multimap<String, String> roleMap = link.getRoleMap();
            if (link.getType().equals(SpaceEvalUtils.MOVELINK)) {
                if (roleMap.containsKey(MOVER) && roleMap.containsKey(TRIGGER)) {
                    for (String mover: roleMap.get(MOVER)) {
                        for (String trigger: roleMap.get(TRIGGER)) {
                            triples.add(new ImmutableTriple<>(trigger, MOVER, mover));
                            triples.add(new ImmutableTriple<>(mover, "Relation", trigger));
                        }
                    }
                }
            } else {
                if (roleMap.containsKey("trigger") || roleMap.containsKey("val")) {
                    for (String trajector: roleMap.get(TRAJECTOR)) {
                        for (String trigger: roleMap.get(TRIGGER)) {
                            triples.add(new ImmutableTriple<>(trigger, TRAJECTOR + "_" + linkType, trajector));
                            triples.add(new ImmutableTriple<>(trajector, "Relation", trigger));
                        }
                    }
                    for (String landmark: roleMap.get(LANDMARK)) {
                        for (String trigger: roleMap.get(TRIGGER)) {
                            triples.add(new ImmutableTriple<>(trigger, LANDMARK + "_" + linkType, landmark));
                            triples.add(new ImmutableTriple<>(landmark, "Relation", trigger));
                        }
                    }
                    roleMap.get(TRAJECTOR).forEach(trajector ->
                            roleMap.get(LANDMARK).forEach(landmark -> {
                                triples.add(new ImmutableTriple<>(trajector, "Relation", landmark));
                                triples.add(new ImmutableTriple<>(landmark, "Relation", trajector));
                            }));
                } else {
                    for (String trajector: roleMap.get(TRAJECTOR)) {
                        for (String landmark: roleMap.get(LANDMARK)) {
                            triples.add(new ImmutableTriple<>(trajector, "locatedIn", landmark));
                            triples.add(new ImmutableTriple<>(landmark, "Relation", trajector));
                        }
                    }
                }
            }
        }
        return triples.stream().distinct().collect(Collectors.toList());
    }


    private static String lineFormat(int idx, String token, String relations, String heads, String label,
                                     String elementId, String depHead, String depLabel) {
        int maxTokenLength = 15, maxRelationLength = 30;
        token = StringUtils.rightPad(token, maxTokenLength);
        label  = StringUtils.rightPad(label, 21);
        relations = StringUtils.rightPad(relations, maxRelationLength);
        heads = StringUtils.rightPad(heads, 20);
//        depHead = StringUtils.right(depHead, 10);
//        depLabel = StringUtils.right(depLabel, 10);
//        isTrigger = StringUtils.rightPad(isTrigger, 5);
        elementId = StringUtils.rightPad(elementId, 5);
        String depInfo = StringUtils.rightPad(depHead + "," + depLabel, 15);
        return String.format("%d\t%s\t%s\t%s\t%s\t%s\t%s", idx, token, label, depInfo, elementId, heads, relations);
//        return String.format("%d\t%s\t%s\t%s\t%s\t%s", idx, token, relations, heads, label, isTrigger);
    }


    private static List<String> getMultiHeadFormatLinkLines(String srcDir, Collection<String> linkTypes, boolean bidirectional,
                                                            boolean use_head, boolean includeXmlInfo, boolean shuffle) {
        List<File> files = FileUtil.listFiles(srcDir);
        List<String> lines = new ArrayList<>();
        int maxTokenLength = 0, maxRelationLength = 0;
        for (File file: files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            Map<String, Span> elementMap = spaceEvalDoc.getElementMap();
            List<BratEvent> allLinks = spaceEvalDoc.getAllLinks().stream()
                    .filter(o -> linkTypes.contains(o.getType()))
                    .collect(Collectors.toList());
            List<Triple<String, String, String>> oriTriples = bidirectional ? getBidirectionalSPOTriples(allLinks) : getSPOTriples(allLinks);
            List<Triple<Span, String, Span>> triples = oriTriples.stream()
                    .map(o -> new ImmutableTriple<>(elementMap.get(o.getLeft()), o.getMiddle(), elementMap.get(o.getRight())))
                    .sorted(Comparator.comparing(o->o.getLeft().start)).collect(Collectors.toList());
            List<List<Span>> sentences = spaceEvalDoc.getSentences();

            if (shuffle) {
                Collections.shuffle(sentences);
            }

            for (List<Span> tokens: sentences) {
                String sentenceText = tokens.stream().map(o->o.text).collect(Collectors.joining(" "));

//                if (sentenceText.startsWith("The GPS ' said '"))
//                    System.out.println("XXX");

                int sentenceStart = tokens.get(0).start, sentenceEnd = tokens.get(tokens.size()-1).end;
                List<Triple<Span, String, Span>> triplesInSentence = triples.stream().filter(o -> {
                    int start = Math.min(o.getLeft().start, o.getRight().start);
                    int end = Math.max(o.getLeft().end, o.getRight().end);
                    return sentenceStart <= start && end <= sentenceEnd;
                }).collect(Collectors.toList());
                List<List<String>> relations = tokens.stream().map(o->new ArrayList<String>()).collect(Collectors.toList());
                List<List<Integer>> heads = tokens.stream().map(o->new ArrayList<Integer>()).collect(Collectors.toList());
                List<String> triggers = new ArrayList<>(Collections.nCopies(tokens.size(), "0"));

                Map<Pair<Integer, Integer>, List<String>> pairRelations = new HashMap<>();


                for (Triple<Span, String, Span> triple: triplesInSentence) {
                    int subjectIdx, objectIdx;
                    if (use_head) {
                        subjectIdx = Collections.binarySearch(tokens, triple.getLeft(), Comparator.comparing(o->o.start));
                        objectIdx = Collections.binarySearch(tokens, triple.getRight(), Comparator.comparing(o->o.start));
                    } else {
                        subjectIdx = Collections.binarySearch(tokens, triple.getLeft(), Comparator.comparing(o->o.end));
                        objectIdx = Collections.binarySearch(tokens, triple.getRight(), Comparator.comparing(o->o.end));
                    }

                    if (subjectIdx >= 0 && objectIdx >=0) {
                        relations.get(subjectIdx).add(triple.getMiddle());
                        heads.get(subjectIdx).add(objectIdx);
                        triggers.set(objectIdx, "1");
                        for (int i = objectIdx + 1; i < tokens.size(); i++) {
                            if (!tokens.get(i).label.startsWith("I-")) break;
                            triggers.set(i, "1");
                        }

                        Pair<Integer, Integer> pair = new Pair<>(subjectIdx, objectIdx);
                        pairRelations.putIfAbsent(pair, new ArrayList<>());
                        pairRelations.get(pair).add(triple.getMiddle());
                    }
                }

//                if (tokens.stream().noneMatch(t -> t.label.endsWith(SPATIAL_SIGNAL) || t.label.endsWith(MOTION))) {
//                    continue;
//                }

                boolean noRelation = relations.stream().noneMatch(r -> r.size() > 0);
                boolean noSignal = tokens.stream().map(t->t.label).
                        noneMatch(l -> l.endsWith(MOTION) || l.endsWith(SPATIAL_SIGNAL));
//                if (noRelation && !noSignal) {
//                    System.out.println("1241245");
//                }

                if (noRelation && noSignal) {
                    continue;
                }

                // 依存关系
                List<Pair<Integer, String>> dependencyHeads = new ArrayList<>();
                List<Sentence> coreNlpSen = StanfordNLPUtil.getSentences(sentenceText);
                int offset = 0;
                for (Sentence sentence: coreNlpSen) {
                    List<Pair<Integer, String>> depHeads = sentence.getDependencyHeads();
                    for (Pair<Integer, String> head: depHeads) {
                        head.setFirst(head.first + offset);
                    }
                    dependencyHeads.addAll(depHeads);
                    offset += depHeads.size();
                }

                if (dependencyHeads.size() != tokens.size()) {
                    System.err.println("分词不一致！");
                }


//                if (coreNlpSen.size() > 1) {
//                    System.out.println(sentenceText);
//                    coreNlpSen.forEach(x -> System.out.println(x.getText()));
//                    System.out.println();
//                }


//                if (includeXmlInfo) {
//                    lines.add(file.getName());
//                }

                lines.add(file.getName());

                if (acceptedLabels != null) {
                    for (Span token: tokens) {
                        String label = token.label;
                        if (!label.equals("O") && !acceptedLabels.contains(label.substring(2))) {
                            token.label = label.substring(0,2) + "Element";
                        }
                    }
                }

                for (int i=0; i < tokens.size();i++) {
                    Span token = tokens.get(i);
                    List<String> relationList = relations.get(i);
                    List<Integer> headList = heads.get(i);
                    if (relationList.size() == 0) {
                        relationList.add("NA");
                        headList.add(i);
                    }
                    String isTrigger = triggers.get(i);
//                    String relationsStr = "[" + StringUtils.join(relations.get(i), ",") + "]";
//                    String headStr = "[" + StringUtils.join(heads.get(i), ",") + "]";

                    for (String relation: relationList) {
                        if (relation.endsWith("_O") || relation.endsWith("_Q")) {
                            String suffix= relation.substring(relation.length()-2);
                            for (int j=i; j < tokens.size();j++) {
                                tokens.get(j).label = tokens.get(j).label + suffix;
                                if (j < tokens.size()-1 && !tokens.get(j+1).label.startsWith("I-")) break;
                            }
                            break;
                        }
                    }

                    relationList = relationList.stream().map(x -> x.replaceAll("_([OQ])$",""))
                            .collect(Collectors.toList());

                    String relationsStr = relationList.toString();
                    String headStr = headList.toString();

                    Integer depHead = dependencyHeads.get(i).first;
                    String depLabel = dependencyHeads.get(i).second;

                    maxTokenLength = Math.max(token.text.length(), maxTokenLength);
                    maxRelationLength = Math.max(relationsStr.length(), maxRelationLength);
                    String line = lineFormat(i, token.text, relationsStr, headStr, token.label, token.id, depHead.toString(), depLabel);
                    if (includeXmlInfo) {
                        line += String.format("\t(%d,%d)\t%s", token.start, token.end, token.id);
                    }
                    lines.add(line);
                }

                lines.add("");
            }
        }
        System.out.println(maxTokenLength + " " + maxRelationLength);
        return lines;
    }


    // 生成Multi-head selection格式的语料
    private static void run_config3(String srcDir, String targetFilePath,List<String> linkTypes, String mode,
                                    boolean includeXmlInfo,  boolean shuffle) {

        List<String> lines;
        lines = getMultiHeadFormatLinkLines(srcDir, linkTypes, false,true, includeXmlInfo,shuffle);
        FileUtil.createDir(targetFilePath + "/" + "AllLink-Head/");
        FileUtil.writeFile(targetFilePath + "/" + "AllLink-Head/" + mode + ".txt", lines);
    }


    public static void main(String [] args) {
//        List<String> linkTypes = Arrays.asList(MOVELINK, QSLINK, OLINK);
//        GenerateMultiHeadCorpus.run("data/SpaceEval2015/raw_data/training++",
//                "data/SpaceEval2015/processed_data/multi-head", "train", true);
//
//        GenerateMultiHeadCorpus.run("data/SpaceEval2015/raw_data/gold++",
//                "data/SpaceEval2015/processed_data/multi-head", "dev", false);
//
//        GenerateMultiHeadCorpus.run("data/SpaceEval2015/raw_data/gold++",
//                "data/SpaceEval2015/processed_data/multi-head", "test", false);

        List<String> linkTypes = Arrays.asList(MOVELINK, QSLINK, OLINK);
        String train_path = "data/SpaceEval2015/raw_data/training++";
        String gold_path = "data/SpaceEval2015/raw_data/gold++";
        String base_dir, target_dir;

//        GenerateMultiHeadCorpus.run_config3(train_path, "", linkTypes,"train",false, false);
//        GenerateMultiHeadCorpus.run_config3(gold_path, "", linkTypes, "dev", false, false);
//        GenerateMultiHeadCorpus.run_config3("", "", linkTypes, "test", false, false);
//        System.exit(0);
        SpaceEvalDoc.ignoreElementNotInLink = true;

        for (int i=0; i<2; i++) {
            onlyCoreRole = i > 0;
            String mode = onlyCoreRole ? "part" : "full";
//            target_dir = "data/SpaceEval2015/processed_data/MHS/" + mode + "/configuration3_trigger";
//            acceptedLabels = new HashSet<String>(){{add(SPATIAL_SIGNAL); add(MOTION);}};
//            GenerateMultiHeadCorpus.run_config3(train_path, target_dir, linkTypes,"train",false, false);
//            GenerateMultiHeadCorpus.run_config3(gold_path, target_dir, linkTypes, "dev", false, false);
//            GenerateMultiHeadCorpus.run_config3(gold_path, target_dir, linkTypes, "test", false, false);
//            acceptedLabels = null

            target_dir = "data/SpaceEval2015/processed_data/MHS_v2/" + mode + "/configuration3";
//            GenerateMultiHeadCorpus_new.run_config3(train_path, target_dir, linkTypes,"train",false, false);
            GenerateMultiHeadCorpus_new.run_config3(gold_path, target_dir, linkTypes, "dev", false, false);
            GenerateMultiHeadCorpus_new.run_config3(gold_path, target_dir, linkTypes, "test", false, false);

//            SpaceEvalDoc.useCoreference = true;
//            target_dir = "data/SpaceEval2015/processed_data/MHS_new/" + mode + "/configuration3";
//            GenerateMultiHeadCorpus_new.run_config3(train_path, target_dir, linkTypes,"train",false, false);
//            GenerateMultiHeadCorpus_new.run_config3(gold_path, target_dir, linkTypes, "dev", false, false);
//            GenerateMultiHeadCorpus_new.run_config3(gold_path, target_dir, linkTypes, "test", false, false);
//            SpaceEvalDoc.useCoreference = false;

//            target_dir = "data/SpaceEval2015/processed_data/MHS_xml/" + mode + "/configuration3";
//            GenerateMultiHeadCorpus_new.run_config3(train_path, target_dir, linkTypes,"train",true, false);
//            GenerateMultiHeadCorpus_new.run_config3(gold_path, target_dir, linkTypes, "dev", true, false);
//            GenerateMultiHeadCorpus_new.run_config3(gold_path, target_dir, linkTypes, "test", true, false);

//
//            target_dir = "data/SpaceEval2015/processed_data/MHS/" + mode + "/configuration2";
//            GenerateMultiHeadCorpus.run_config2(train_path, target_dir, linkTypes,"train",false, false);
//            GenerateMultiHeadCorpus.run_config2(gold_path, target_dir, linkTypes, "dev", false, false);
//            GenerateMultiHeadCorpus.run_config2(gold_path, target_dir, linkTypes, "test", false, false);
//
//            target_dir = "data/SpaceEval2015/processed_data/MHS_xml/" + mode + "/configuration2";
//            GenerateMultiHeadCorpus.run_config2(train_path, target_dir, linkTypes,"train",true, false);
//            GenerateMultiHeadCorpus.run_config2(gold_path, target_dir, linkTypes, "dev", true, false);
//            GenerateMultiHeadCorpus.run_config2(gold_path, target_dir, linkTypes, "test", true, false);
//
//
//            String elementPredPath = "data/SpaceEval2015/processed_data/NER/config1/predict.txt";
//            target_dir = "data/SpaceEval2015/processed_data/MHS/" + mode + "/configuration1_1";
//
//            GenerateMultiHeadCorpus.run_config2(train_path, target_dir, linkTypes,"train",false, false);
//            GenerateMultiHeadCorpus.run_config1(gold_path, target_dir, elementPredPath, linkTypes,"dev",false, false);
//            GenerateMultiHeadCorpus.run_config1(gold_path, target_dir, elementPredPath, linkTypes,"test",false, false);
//
//            target_dir = "data/SpaceEval2015/processed_data/MHS_xml/" + mode + "/configuration1_1";
//            GenerateMultiHeadCorpus.run_config2(train_path, target_dir, linkTypes,"train",true, false);
//            GenerateMultiHeadCorpus.run_config2(gold_path, target_dir, linkTypes,"dev",true, false);
//            GenerateMultiHeadCorpus.run_config2(gold_path, target_dir, linkTypes,"test",true, false);
//
//
//            String elementTrainPath = "data/SpaceEval2015/processed_data/NER/config2/train.txt";
//            elementPredPath = "data/SpaceEval2015/processed_data/NER/config2/predict.txt";
//            target_dir = "data/SpaceEval2015/processed_data/MHS/" + mode + "/configuration1_2";
//
//            GenerateMultiHeadCorpus.run_config1(train_path, target_dir, elementTrainPath,linkTypes,"train",false, false);
//            GenerateMultiHeadCorpus.run_config1(gold_path, target_dir, elementPredPath, linkTypes,"dev",false, false);
//            GenerateMultiHeadCorpus.run_config1(gold_path, target_dir, elementPredPath, linkTypes,"test",false, false);
//
//            target_dir = "data/SpaceEval2015/processed_data/MHS_xml/" + mode + "/configuration1_2";
//            GenerateMultiHeadCorpus.run_config2(train_path, target_dir, linkTypes,"train",true, false);
//            GenerateMultiHeadCorpus.run_config2(gold_path, target_dir, linkTypes,"dev",true, false);
//            GenerateMultiHeadCorpus.run_config2(gold_path, target_dir, linkTypes,"test",true, false);
        }


    }
}
