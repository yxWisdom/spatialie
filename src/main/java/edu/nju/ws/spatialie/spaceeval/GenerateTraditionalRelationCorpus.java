package edu.nju.ws.spatialie.spaceeval;

import com.alibaba.fastjson.JSONObject;
import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.utils.FileUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static edu.nju.ws.spatialie.spaceeval.SpaceEvalUtils.*;

public class GenerateTraditionalRelationCorpus {

    // 生成传统关系抽取格式的语料
    public static void main(String [] args) {

        GenerateTraditionalRelationCorpus.saveRelationMap("data/SpaceEval2015/processed_data/openNRE/rel2id.json");

        GenerateTraditionalRelationCorpus.run("data/SpaceEval2015/raw_data/training++",
                "data/SpaceEval2015/processed_data/openNRE", "train", false);

        GenerateTraditionalRelationCorpus.run("data/SpaceEval2015/raw_data/gold++",
                "data/SpaceEval2015/processed_data/openNRE", "val", false);

        GenerateTraditionalRelationCorpus.run("data/SpaceEval2015/raw_data/gold++",
                "data/SpaceEval2015/processed_data/openNRE", "test", false);
    }
    //    public static void get
    private final static int moveLinkDistanceLimit = 25;
    private final static int nonMoveLinkDistanceLimit = 25;
//    private final static int binaryNonMoveLinkDistanceLimit = 15;

    private final static String NONE="None";
    private final static String LOCATED_IN = "LocatedIn";


    private static String getRelLine(String relation, List<Span> tokens, Span head, Span tail) {
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
        jsonObject.put("token", tokenList);
        jsonObject.put("h", head_entity);
        jsonObject.put("t", tail_entity);
        jsonObject.put("relation", relation);
        return jsonObject.toJSONString();
    }


    private static List<String> getQSAndOLinkWithTrigger(String srcDir, String...linkTypes) {
        List<File> files = FileUtil.listFiles(srcDir);
        List<String> linkLines = new ArrayList<>();
        Set<String> linkTypeSet = new HashSet<>(Arrays.asList(linkTypes));
        for (File file : files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<List<Span>> sentences = spaceEvalDoc.getSentences();
            List<BratEvent> links = spaceEvalDoc.getAllLinks().stream().filter(o -> linkTypeSet.contains(o.getType()))
                    .collect(Collectors.toList());

            Set<Triple<String, String, String>> goldTriples = new HashSet<>();
            for (BratEvent link : links) {
                Collection<String> trajectors = link.getRoleIds(TRAJECTOR), landmarks = link.getRoleIds(LANDMARK);
                if (link.hasRole(VAL) || link.hasRole(TRIGGER)) {
                    String trigger = link.hasRole(VAL) ? link.getRoleId(VAL) : link.getRoleId(TRIGGER);
                    if (link.hasRole(TRAJECTOR)) {
                        trajectors.forEach(trajector -> goldTriples.add(new ImmutableTriple<>(trajector, TRAJECTOR, trigger)));
                    }
                    if (link.hasRole(LANDMARK)) {
                        landmarks.forEach(landmark -> goldTriples.add(new ImmutableTriple<>(landmark, LANDMARK, trigger)));
                    }
                } else {
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
                List<Span> triggers = elementsInSentence.stream()
                        .filter(o -> {
                            boolean hasMLink = linkTypeSet.contains(MEASURELINK) && o.label.equals(MEASURE);
                            boolean hasQSorOLink = (linkTypeSet.contains(QSLINK) || linkTypeSet.contains(OLINK)) && o.label.equals(SPATIAL_SIGNAL);
                            return hasMLink || hasQSorOLink;
                        })
                        .collect(Collectors.toList());

                for (Span trigger : triggers) {
                    for (Span trajector : elementsInSentence) {
                        int distance = calElementTokenLevelDistance(tokensInSentence, Arrays.asList(trajector, trigger));
                        if (goldTriples.contains(new ImmutableTriple<>(trajector.id, TRAJECTOR, trigger.id))) {
                            linkLines.add(getRelLine(TRAJECTOR, tokensInSentence, trajector, trigger));
                        } else if (trajectorTypes.contains(trajector.label) && distance < nonMoveLinkDistanceLimit) {
                            linkLines.add(getRelLine(NONE, tokensInSentence, trajector, trigger));
                        }
                    }
                    for (Span landmark : elementsInSentence) {
                        int distance = calElementTokenLevelDistance(tokensInSentence, Arrays.asList(landmark, trigger));
                        if (goldTriples.contains(new ImmutableTriple<>(landmark.id, LANDMARK, trigger.id)))
                            linkLines.add(getRelLine(LANDMARK, tokensInSentence, landmark, trigger));
                        else if (landmarkTypes.contains(landmark.label) && distance < nonMoveLinkDistanceLimit)
                            linkLines.add(getRelLine(NONE, tokensInSentence, landmark, trigger));
                    }
                }

                for (Span trajector : elementsInSentence) {
                    for (Span landmark : elementsInSentence) {
                        if (trajector.equals(landmark)) continue;
                        int distance = calElementTokenLevelDistance(tokensInSentence, Arrays.asList(trajector, landmark));
                        if (goldTriples.contains(new ImmutableTriple<>(trajector.id, LOCATED_IN, landmark.id))) {
                            linkLines.add(getRelLine(LOCATED_IN, tokensInSentence, trajector, landmark));
                        } else if (trajectorTypes.contains(trajector.label) && landmarkTypes.contains(landmark.label) &&
                                distance < nonMoveLinkDistanceLimit) {
                            linkLines.add(getRelLine(NONE, tokensInSentence, trajector, landmark));
                        }
                    }
                }
            }

        }
        return linkLines;
    }



    private static List<String> getMoveLinkLines(String srcDir) {
        List<File> files = FileUtil.listFiles(srcDir);
        List<String> linkLines = new ArrayList<>();
        for (File file : files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<List<Span>> sentences = spaceEvalDoc.getSentences();
            List<BratEvent> links = spaceEvalDoc.getMoveLink();
            Set<Pair<String, String>> goldPairs = new HashSet<>();
            for (BratEvent link: links) {
                if (link.hasRole(MOVER) && link.hasRole(TRIGGER)) {
                    Collection<String> movers = link.getRoleIds(MOVER), triggers = link.getRoleIds(TRIGGER);
                    movers.forEach(mover -> triggers.forEach(trigger->
                            goldPairs.add(new ImmutablePair<>(mover, trigger))));
                }
            }
            for (List<Span> tokensInSentence : sentences) {
                int start = tokensInSentence.get(0).start;
                int end = tokensInSentence.get(tokensInSentence.size() - 1).end;
                List<Span> elementsInSentence = spaceEvalDoc.getElementsInSentence(start, end);
                List<Span> triggers = elementsInSentence.stream()
                        .filter(o -> o.label.equals(MOTION))
                        .collect(Collectors.toList());
                for (Span mover : elementsInSentence) {
                    for (Span trigger : triggers) {
                        if (mover.equals(trigger))
                            continue;
                        int distance = calElementTokenLevelDistance(tokensInSentence, Arrays.asList(mover, trigger));
                        if (goldPairs.contains(new ImmutablePair<>(mover.id, trigger.id))) {
                            linkLines.add(getRelLine(MOVER, tokensInSentence, mover, trigger));
                        } else if (moverTypes.contains(mover.label) && distance < moveLinkDistanceLimit) {
                            linkLines.add(getRelLine(NONE, tokensInSentence, mover, trigger));
                        }
                    }
                }
            }
        }
        return linkLines;
    }

    private static void saveRelationMap(String targetFilePath) {
        List<String> relations = Arrays.asList(NONE, TRAJECTOR, LANDMARK, MOVER, LOCATED_IN);
        JSONObject relationsObj = new JSONObject(true);
        for (int i=0; i<relations.size();i++) {
            relationsObj.put(relations.get(i), i);
        }
        FileUtil.writeFile(targetFilePath, relationsObj.toJSONString());
    }


    private static void run(String srcDir, String targetFilePath, String mode, boolean shuffle) {
        List<String> nonMoveLinkLines = getQSAndOLinkWithTrigger(srcDir, QSLINK, OLINK);
        List<String> moveLinkLines = getMoveLinkLines(srcDir);
        List<String> allLinkLines = new ArrayList<>();
        allLinkLines.addAll(nonMoveLinkLines);
        allLinkLines.addAll(moveLinkLines);

        if (shuffle) {
            Collections.shuffle(allLinkLines);
        }
        FileUtil.writeFile(targetFilePath + "/" + "AllLink/" + mode + ".txt", allLinkLines);
    }
}
