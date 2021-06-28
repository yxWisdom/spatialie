package edu.nju.ws.spatialie.spaceeval;

import com.alibaba.fastjson.JSONObject;
import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.utils.FileUtil;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static edu.nju.ws.spatialie.spaceeval.SpaceEvalUtils.*;

public class GenerateOpenNRECorpus {

    // 生成传统关系抽取格式的语料
    public static void main(String [] args) {

//        GenerateOpenNRECorpus.saveRelationMap("data/SpaceEval2015/processed_data/openNRE/rel2id.json");

        for (int i=0; i<2; i++) {
            GenerateOpenNRECorpus.onlyCoreRole = i > 0;

            GenerateOpenNRECorpus.run("data/SpaceEval2015/raw_data/training++",
                    "data/SpaceEval2015/processed_data/openNRE", "train", true,false);

            GenerateOpenNRECorpus.run("data/SpaceEval2015/raw_data/gold++",
                    "data/SpaceEval2015/processed_data/openNRE", "val", false,false);

            GenerateOpenNRECorpus.run("data/SpaceEval2015/raw_data/gold++",
                    "data/SpaceEval2015/processed_data/openNRE", "test", false,false);


            GenerateOpenNRECorpus.run("data/SpaceEval2015/raw_data/training++",
                    "data/SpaceEval2015/processed_data/openNRE_xml", "train", true,true);

            GenerateOpenNRECorpus.run("data/SpaceEval2015/raw_data/gold++",
                    "data/SpaceEval2015/processed_data/openNRE_xml", "val", false,true);

            GenerateOpenNRECorpus.run("data/SpaceEval2015/raw_data/gold++",
                    "data/SpaceEval2015/processed_data/openNRE_xml", "test", false,true);
        }


//        GenerateOpenNRECorpus.run_no_trigger("data/SpaceEval2015/raw_data/training++",
//                "data/SpaceEval2015/processed_data/openNRE", "train", true);
//
//        GenerateOpenNRECorpus.run_no_trigger("data/SpaceEval2015/raw_data/gold++",
//                "data/SpaceEval2015/processed_data/openNRE", "val", false);
//
//        GenerateOpenNRECorpus.run_no_trigger("data/SpaceEval2015/raw_data/gold++",
//                "data/SpaceEval2015/processed_data/openNRE", "test", false);

//        for (int i = 6; i <= 16; i+=2) {
//            GenerateOpenNRECorpus.moveLinkDistanceLimit = i;
//            GenerateOpenNRECorpus.nonMoveLinkDistanceLimit = i;
//
////            GenerateTraditionalRelationCorpus.saveRelationMap("data/SpaceEval2015/processed_data/openNRE/rel2id.json");
//
//            GenerateOpenNRECorpus.run("data/SpaceEval2015/raw_data/training++",
//                    "data/SpaceEval2015/processed_data/openNRE", "train", true);
//
//            GenerateOpenNRECorpus.run("data/SpaceEval2015/raw_data/gold++",
//                    "data/SpaceEval2015/processed_data/openNRE", "val", false);
//
//            GenerateOpenNRECorpus.run("data/SpaceEval2015/raw_data/gold++",
//                    "data/SpaceEval2015/processed_data/openNRE", "test", false);
//        }
//
//        for (int j = 1; j <= 7; j++) {
//            GenerateOpenNRECorpus.moveLinkDistanceLimit = 12;
//            GenerateOpenNRECorpus.nonMoveLinkDistanceLimit = 12;
//            GenerateOpenNRECorpus.internalElementNumLimit = j;
//
//            GenerateOpenNRECorpus.run("data/SpaceEval2015/raw_data/training++",
//                    "data/SpaceEval2015/processed_data/openNRE", "train", true);
//
//            GenerateOpenNRECorpus.run("data/SpaceEval2015/raw_data/gold++",
//                    "data/SpaceEval2015/processed_data/openNRE", "val", false);
//
//            GenerateOpenNRECorpus.run("data/SpaceEval2015/raw_data/gold++",
//                    "data/SpaceEval2015/processed_data/openNRE", "test", false);
//        }
    }

    // 对于moveLink 两个元素之间的距离最大值，以字符为单位
    private  static final int moveLinkDistanceLimit = 1000;

    // 对于非moveLink 两个元素之间的距离最大值，以字符为单位
    private  static final int nonMoveLinkDistanceLimit = 1000;

    // 两个元素之间的最大元素个数
    private  static final int internalElementNumLimit = 100;
//    private final static int binaryNonMoveLinkDistanceLimit = 15;

    private final static String NONE="None";
    private final static String LOCATED_IN = "locatedIn";

    private static boolean onlyCoreRole = true;


    private static Set<Triple<String, String, String>> getGoldTriples(List<BratEvent> links, String ...types) {
        Set<Triple<String, String, String>> goldTriples = new HashSet<>();
        Set<String> typeSet = new HashSet<>(Arrays.asList(types));
        for (BratEvent link: links) {
            if (typeSet.contains(MOVELINK) && link.hasRole(TRIGGER)) {
//                // TODO: 记得删掉
//                if (link.getRoleIds(TRIGGER).size() > 1) {
//                    System.out.println("不止一个trigger");
//                }
//                Collection<String> triggers = link.getRoleIds(TRIGGER);
//                mLinkCoreRoles.forEach(roleType -> {
//                    link.getRoleIds(roleType).forEach(role -> triggers.forEach(trigger->
//                            goldTriples.add(new ImmutableTriple<>(role, roleType, trigger))));
//                });
//                if (!onlyCoreRole) {
//                    mLinkOptionalRoles.forEach(roleType -> {
//                        link.getRoleIds(roleType).forEach(role -> triggers.forEach(trigger->
//                                goldTriples.add(new ImmutableTriple<>(role, roleType, trigger))));
//                    });
//                }
//                Collection<String> triggers = link.getRoleIds(TRIGGER);
                String trigger = link.getRoleId(TRIGGER);
                mLinkCoreRoles.forEach(roleType -> {
                    link.getRoleIds(roleType).forEach(role ->
                            goldTriples.add(new ImmutableTriple<>(role, roleType, trigger)));
                });
                if (!onlyCoreRole) {
                    mLinkOptionalRoles.forEach(roleType -> {
                        link.getRoleIds(roleType).forEach(role ->
                                goldTriples.add(new ImmutableTriple<>(role, roleType, trigger)));
                    });
                }
            }
            if (typeSet.contains(OLINK) || typeSet.contains(QSLINK) ||  typeSet.contains(MEASURELINK)) {
                Collection<String> trajectors = link.getRoleIds(TRAJECTOR), landmarks = link.getRoleIds(LANDMARK);
                if (link.hasRole(VAL) || link.hasRole(TRIGGER)) {
                    String trigger = link.hasRole(VAL) ? link.getRoleId(VAL) : link.getRoleId(TRIGGER);
                    trajectors.forEach(trajector -> goldTriples.add(new ImmutableTriple<>(trajector, TRAJECTOR, trigger)));
                    landmarks.forEach(landmark -> goldTriples.add(new ImmutableTriple<>(landmark, LANDMARK, trigger)));
                } else {
                    if (link.hasRole(TRAJECTOR) && link.hasRole(LANDMARK)) {
                        trajectors.forEach(trajector -> landmarks.forEach(landmark ->
                                goldTriples.add(new ImmutableTriple<>(trajector, LOCATED_IN, landmark))));
                    }
                }
            }
        }
        return goldTriples;
    }

    private static boolean inGoldTriple(Set<Triple<String, String, String>> triples, String head, String tail) {
        return inGoldTriple(triples, head, tail, LOCATED_IN, TRAJECTOR, LANDMARK, MOVER, SOURCE, MID_POINT, GOAL, PATH_ID, GROUND, MOTION_SIGNAL_ID);
    }

    private static boolean inGoldTriple(Set<Triple<String, String, String>> triples, String head, String tail, String ...types) {
        for (String type: types) {
            if (triples.contains(new ImmutableTriple<>(head, type, tail))) {
                return true;
            }
        }
        return false;
    }


    private static String getRelLine(String relation, List<Span> tokens, Span head, Span tail, String xmlFileName, boolean includeXmlInfo) {
        int head_start=-1, head_end=-1, tail_start=-1, tail_end=-1;
        for (int i = 0; i < tokens.size(); i++) {
            Span token = tokens.get(i);
            if (head.start == token.start) head_start = i;
            if (tail.start == token.start) tail_start = i;
            if (head.end == token.end) head_end = i + 1;
            if (tail.end == token.end) tail_end = i + 1;
        }
        List<String> tokenList = tokens.stream().map(o->o.text).collect(Collectors.toList());
        JSONObject jsonObject = new JSONObject(true);
        JSONObject head_entity = new JSONObject(true);
        JSONObject tail_entity = new JSONObject(true);
        head_entity.put("name", head.text);
        head_entity.put("pos", new Integer[] {head_start, head_end});

        tail_entity.put("name", tail.text);
        tail_entity.put("pos", new Integer[] {tail_start, tail_end});

        if (includeXmlInfo){
            jsonObject.put("xmlfile",xmlFileName);
            head_entity.put("id",head.id);
            tail_entity.put("id",tail.id);
            head_entity.put("semantic_type",head.getAttribute(SEMANTIC_TYPE));
            head_entity.put("label",head.label);
            tail_entity.put("semantic_type",tail.getAttribute(SEMANTIC_TYPE));
            tail_entity.put("label", tail.label);
        }
        jsonObject.put("token", tokenList);
        jsonObject.put("h", head_entity);
        jsonObject.put("t", tail_entity);
        jsonObject.put("relation", relation);
        return jsonObject.toJSONString();
    }

    private static List<String> getQSAndOLinkWithoutTrigger(String srcDir, String...linkTypes) {
        List<File> files = FileUtil.listFiles(srcDir);
        List<String> linkLines = new ArrayList<>();
        Set<String> linkTypeSet = new HashSet<>(Arrays.asList(linkTypes));
        for (File file : files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<List<Span>> sentences = spaceEvalDoc.getSentences();
            List<Span> allElements = spaceEvalDoc.getElements().stream().filter(o -> o.start >= 0).collect(Collectors.toList());
            List<BratEvent> links = spaceEvalDoc.getAllLinks().stream().filter(o -> linkTypeSet.contains(o.getType()))
                    .collect(Collectors.toList());

            Set<Triple<String, String, String>> goldTriples = new HashSet<>();
            for (BratEvent link : links) {
                Collection<String> trajectors = link.getRoleIds(TRAJECTOR), landmarks = link.getRoleIds(LANDMARK);
                if (!link.hasRole(VAL) && !link.hasRole(TRIGGER)) {
                    if (link.hasRole(TRAJECTOR) && link.hasRole(LANDMARK)) {
                        trajectors.forEach(trajector -> landmarks.forEach(landmark ->
                                goldTriples.add(new ImmutableTriple<>(trajector, LOCATED_IN, landmark))));
                    }
                }
            }
            for (List<Span> tokensInSentence : sentences) {
                int start = tokensInSentence.get(0).start;
                int end = tokensInSentence.get(tokensInSentence.size() - 1).end;
                List<Span> elementsInSentence = spaceEvalDoc.getElementsInSentence(start, end);
                for (Span trajector : elementsInSentence) {
                    for (Span landmark : elementsInSentence) {
                        if (trajector.equals(landmark)) continue;
                        int elementNum = calElementNumBetweenElements(allElements, Arrays.asList(trajector, landmark));
                        int distance = calElementTokenLevelDistance(tokensInSentence, Arrays.asList(trajector, landmark));
                        if (goldTriples.contains(new ImmutableTriple<>(trajector.id, LOCATED_IN, landmark.id))) {
                            linkLines.add(getRelLine(LOCATED_IN, tokensInSentence, trajector, landmark, file.getName(), false));
                        } else if (trajectorTypes.contains(trajector.label) && landmarkTypes.contains(landmark.label) &&
                                distance <= nonMoveLinkDistanceLimit && elementNum <= internalElementNumLimit) {
                            linkLines.add(getRelLine(NONE, tokensInSentence, trajector, landmark, file.getName(), false));
                        }
                    }
                }
            }

        }
        return linkLines;
    }


    private static List<String> getQSAndOLinkWithTrigger(String srcDir,boolean includeXmlInfo, String...linkTypes) {
        List<File> files = FileUtil.listFiles(srcDir);
        List<String> linkLines = new ArrayList<>();
        Set<String> linkTypeSet = new HashSet<>(Arrays.asList(linkTypes));
        for (File file : files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<List<Span>> sentences = spaceEvalDoc.getSentences();
            List<Span> allElements = spaceEvalDoc.getElements().stream().filter(o -> o.start >= 0).collect(Collectors.toList());
            List<BratEvent> links = spaceEvalDoc.getAllLinks().stream().filter(o -> linkTypeSet.contains(o.getType()))
                    .collect(Collectors.toList());

            Set<Triple<String, String, String>> goldTriples = getGoldTriples(links, OLINK, QSLINK);
            for (List<Span> tokensInSentence : sentences) {
                int start = tokensInSentence.get(0).start;
                int end = tokensInSentence.get(tokensInSentence.size() - 1).end;
                List<Span> elementsInSentence = spaceEvalDoc.getElementsInSentence(start, end);
                List<Span> triggers = elementsInSentence.stream()
                        .filter(o -> {
                            boolean hasMLink = linkTypeSet.contains(MEASURELINK) && o.label.equals(MEASURE);
                            boolean hasQSorOLink = (linkTypeSet.contains(QSLINK) || linkTypeSet.contains(OLINK)) && o.label.equals(SPATIAL_SIGNAL);
                            return hasMLink || hasQSorOLink;
                        })
                        .collect(Collectors.toList());

                for (Span trigger : triggers) {
                    for (Span element: elementsInSentence) {
                        int elementNum = calElementNumBetweenElements(allElements, Arrays.asList(trigger, element));
                        int distance = calElementTokenLevelDistance(tokensInSentence, Arrays.asList(element, trigger));
                        if (inGoldTriple(goldTriples, element.id, trigger.id, TRAJECTOR)) {
                            linkLines.add(getRelLine(TRAJECTOR, tokensInSentence, element, trigger,file.getName(),includeXmlInfo));
                        } else if (inGoldTriple(goldTriples, element.id, trigger.id, LANDMARK)){
                            linkLines.add(getRelLine(LANDMARK, tokensInSentence, element, trigger, file.getName(), includeXmlInfo));
                        } else if (!inGoldTriple(goldTriples, element.id, trigger.id)
                                && trajectorTypes.contains(element.label)
                                && distance <= nonMoveLinkDistanceLimit
                                && elementNum <= internalElementNumLimit) {
                            linkLines.add(getRelLine(NONE, tokensInSentence, element, trigger, file.getName(), includeXmlInfo));
                        }
                    }
                }

                for (Span trajector : elementsInSentence) {
                    for (Span landmark : elementsInSentence) {
                        if (trajector.equals(landmark)) continue;
                        int elementNum = calElementNumBetweenElements(allElements, Arrays.asList(trajector, landmark));
                        int distance = calElementTokenLevelDistance(tokensInSentence, Arrays.asList(trajector, landmark));
                        if (goldTriples.contains(new ImmutableTriple<>(trajector.id, LOCATED_IN, landmark.id))) {
                            linkLines.add(getRelLine(LOCATED_IN, tokensInSentence, trajector, landmark, file.getName(), includeXmlInfo));
                        } else if (!inGoldTriple(goldTriples,trajector.id,landmark.id) &&
                                trajectorTypes.contains(trajector.label) && landmarkTypes.contains(landmark.label) &&
                                distance <= nonMoveLinkDistanceLimit && elementNum <= internalElementNumLimit) {
                            linkLines.add(getRelLine(NONE, tokensInSentence, trajector, landmark, file.getName(), includeXmlInfo));
                        }
                    }
                }
            }
        }
        return linkLines;
    }



    private static List<String> getMoveLinkLines(String srcDir,boolean includeXmlInfo) {
        List<File> files = FileUtil.listFiles(srcDir);
        List<String> linkLines = new ArrayList<>();
        for (File file : files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<List<Span>> sentences = spaceEvalDoc.getSentences();
            List<BratEvent> links = spaceEvalDoc.getMoveLink();
            List<Span> allElements = spaceEvalDoc.getElements().stream().filter(o -> o.start >= 0).collect(Collectors.toList());

            Set<Triple<String, String, String>> goldTriples = getGoldTriples(links, MOVELINK);
            for (List<Span> tokensInSentence : sentences) {
                int start = tokensInSentence.get(0).start;
                int end = tokensInSentence.get(tokensInSentence.size() - 1).end;
                List<Span> elementsInSentence = spaceEvalDoc.getElementsInSentence(start, end);
                List<Span> triggers = elementsInSentence.stream()
                        .filter(o -> o.label.equals(MOTION))
                        .collect(Collectors.toList());

                Set<String> roleTypes = new HashSet<>(mLinkCoreRoles);
                if (!onlyCoreRole)  roleTypes.addAll(mLinkOptionalRoles);

                for (Span candidate: elementsInSentence) {
                    for (Span trigger: triggers) {
                        for (String roleType: roleTypes) {
                            if (candidate.equals(trigger)) continue;
                            int distance = calElementTokenLevelDistance(tokensInSentence, Arrays.asList(candidate, trigger));
                            int elementNum = calElementNumBetweenElements(allElements, Arrays.asList(candidate, trigger));

                            if (goldTriples.contains(new ImmutableTriple<>(candidate.id, roleType, trigger.id))) {
                                linkLines.add(getRelLine(roleType, tokensInSentence, candidate, trigger, file.getName(), includeXmlInfo));
                            } else if (!inGoldTriple(goldTriples, candidate.id, trigger.id)
                                    && validCandidateRoleType(candidate, roleType)
                                    && distance <= moveLinkDistanceLimit
                                    && elementNum <= internalElementNumLimit ) {
                                linkLines.add(getRelLine(NONE, tokensInSentence, candidate, trigger, file.getName(), includeXmlInfo));
                            }
                        }
                    }
                }
            }
        }
        return linkLines;
    }

    private static void saveRelationMap(String targetFilePath) {
        List<String> relations = new ArrayList<>(Arrays.asList(NONE, TRAJECTOR, LANDMARK, LOCATED_IN));
        relations.addAll(mLinkCoreRoles);
        if (!onlyCoreRole) {
            relations.addAll(mLinkOptionalRoles);
        }
        JSONObject relationsObj = new JSONObject(true);
        for (int i=0; i<relations.size();i++) {
            relationsObj.put(relations.get(i), i);
        }
        FileUtil.writeFile(targetFilePath, relationsObj.toJSONString());
    }

    private static void checkConflict(List<String> lines) {
        Map<String, List<String>> map = new TreeMap<>();
        for (String line: lines) {
            int index = line.indexOf("\"relation\":");
            String key = line.substring(0, index);
            map.putIfAbsent(key, new ArrayList<>());
            map.get(key).add(line.substring(index));
        }

        map.forEach((x,y)-> {
            if (y.size() > 1) {
                System.out.println(x + y);
            }
        });
    }

    private static void run(String srcDir, String targetFilePath, String mode, boolean shuffle,boolean includeXmlInfo) {
        List<String> nonMoveLinkLines = getQSAndOLinkWithTrigger(srcDir, includeXmlInfo, QSLINK, OLINK);
        List<String> moveLinkLines = getMoveLinkLines(srcDir,includeXmlInfo);
        List<String> allLinkLines = new ArrayList<>();
        allLinkLines.addAll(nonMoveLinkLines);
        allLinkLines.addAll(moveLinkLines);
        allLinkLines = allLinkLines.stream().distinct().collect(Collectors.toList());



//        allLinkLines=removeConflict(allLinkLines);
        checkConflict(allLinkLines);
        if (shuffle) {
            Collections.shuffle(allLinkLines);
        }
        String dirname = String.format("%s/AllLink_%d_%d", targetFilePath, moveLinkDistanceLimit, internalElementNumLimit);
        if (!onlyCoreRole) dirname += "_full";
        FileUtil.createDir(dirname);
        FileUtil.writeFile(String.format("%s/%s.txt", dirname, mode), allLinkLines);

        GenerateOpenNRECorpus.saveRelationMap(dirname + "/rel2id.json");
    }

    private static List<String> removeConflict(List<String> lines) {
        Map<String, List<String>> map = new TreeMap<>();
        for (String line: lines) {
            int index = line.indexOf("\"relation\":");
            String key = line.substring(0, index);
            map.putIfAbsent(key, new ArrayList<>());
            map.get(key).add(line.substring(index));
        }
        for (String sent:map.keySet()){
            for (String rel:map.get(sent)){
                if (rel.contains("None")){
                    lines.remove(sent+rel);
                }
            }
        }
        return lines;
    }


    private static void run_no_trigger(String srcDir, String targetFilePath, String mode, boolean shuffle) {
        List<String> no_trigger_links = getQSAndOLinkWithoutTrigger(srcDir, QSLINK, OLINK);

        if (shuffle) {
            Collections.shuffle(no_trigger_links);
        }

        String dirname = targetFilePath + "/noTriggerLinks_" + moveLinkDistanceLimit + "_" + internalElementNumLimit + "/";
        FileUtil.createDir(dirname);
        FileUtil.writeFile(dirname + mode + ".txt", no_trigger_links);
    }

}
