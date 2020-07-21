package edu.nju.ws.spatialie.spaceeval;

import com.google.common.collect.Multimap;
import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.utils.CollectionUtils;
import edu.nju.ws.spatialie.utils.FileUtil;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.nju.ws.spatialie.spaceeval.SpaceEvalUtils.*;

public class GenerateMultiHeadCorpus {


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
        List<Triple<String, String, String>> triples = new ArrayList<>();
        for (BratEvent link: links) {
            char linkType = link.getType().charAt(0);
            Multimap<String, String> roleMap = link.getRoleMap();
            if (link.getType().equals(SpaceEvalUtils.MOVELINK)) {
                if (roleMap.containsKey(MOVER) && roleMap.containsKey(TRIGGER)) {
                    for (String mover: roleMap.get(MOVER)) {
                        for (String trigger: roleMap.get(TRIGGER)) {
                            triples.add(new ImmutableTriple<>(trigger, MOVER, mover));
//                            triples.add(new ImmutableTriple<>(trigger, TRIGGER, trigger));
                        }
                    }
                }
            } else {
                if (roleMap.containsKey("trigger") || roleMap.containsKey("val")) {
                    for (String trajector: roleMap.get(TRAJECTOR)) {
                        for (String trigger: roleMap.get(TRIGGER)) {
//                            triples.add(new ImmutableTriple<>(trajector, TRAJECTOR, trigger));
                            triples.add(new ImmutableTriple<>(trigger, TRAJECTOR + "_" + linkType, trajector));
//                            triples.add(new ImmutableTriple<>(trigger, TRIGGER + "_" + linkType, trigger));
                        }
                    }
                    for (String landmark: roleMap.get(LANDMARK)) {
                        for (String trigger: roleMap.get(TRIGGER)) {
//                            triples.add(new ImmutableTriple<>(landmark, LANDMARK, trigger));
                            triples.add(new ImmutableTriple<>(trigger, LANDMARK + "_" + linkType, landmark));
//                            triples.add(new ImmutableTriple<>(trigger, TRIGGER + "_" + linkType, trigger));
                        }
                    }
                } else {
                    for (String trajector: roleMap.get(TRAJECTOR)) {
                        for (String landmark: roleMap.get(LANDMARK)) {
                            triples.add(new ImmutableTriple<>(trajector, "locatedIn", landmark));
                        }
                    }
                }
            }
        }
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
                    roleMap.get(TRAJECTOR).forEach(trajector -> {
                        roleMap.get(LANDMARK).forEach(landmark -> {
                            triples.add(new ImmutableTriple<>(trajector, "Relation", landmark));
                            triples.add(new ImmutableTriple<>(landmark, "Relation", trajector));
                        });
                    });
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


    private static String lineFormat(int idx, String token, String relations, String heads, String label, String isTrigger) {
        int maxTokenLength = 15, maxRelationLength = 30;
        token = StringUtils.rightPad(token, maxTokenLength);
        label  = StringUtils.rightPad(label, 16);
        relations = StringUtils.rightPad(relations, maxRelationLength);
        heads = StringUtils.rightPad(heads, 15);
        isTrigger = StringUtils.rightPad(isTrigger, 3);
        return String.format("%d\t%s\t%s\t%s\t%s\t%s", idx, token, relations, heads, label, isTrigger);
    }


    private static List<String> getMultiHeadFormatLinkLines(String srcDir, Collection<String> linkTypes, boolean bidirectional,
                                                            boolean use_head, boolean includeXmlInfo, boolean shuffle) {
        List<File> files = FileUtil.listFiles(srcDir);
        List<String> lines = new ArrayList<>();
        int maxTokenLength = 0, maxRelationLength = 0;
        for (File file: files) {

//            if (file.getPath().endsWith("RideForClimateUSA.xml")) {
//                System.out.println(1);
//            }

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
                if (sentenceText.startsWith("The GPS ' said '"))
                    System.out.println("XXX");

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
                if (noRelation && !noSignal) {
                    System.out.println("1241245");
                }

                if (noRelation && noSignal) {
                    continue;
                }

                if (includeXmlInfo) {
                    lines.add(file.getName());
                }

                for (int i=0; i < tokens.size();i++) {
                    Span token = tokens.get(i);
                    if (relations.get(i).size() == 0) {
                        relations.get(i).add("NA");
                        heads.get(i).add(i);
                    }
                    String isTrigger = triggers.get(i);
//                    String relationsStr = "[" + StringUtils.join(relations.get(i), ",") + "]";
//                    String headStr = "[" + StringUtils.join(heads.get(i), ",") + "]";

                    String relationsStr = relations.get(i).toString();
                    String headStr = heads.get(i).toString();


                    if (relations.get(i).contains("trajector_Q") || relations.get(i).contains("landmark_Q")) {
                        for (int j=i; j < tokens.size();j++) {
                            tokens.get(j).label = tokens.get(j).label + "_Q";
                            if (j < tokens.size()-1 && !tokens.get(j+1).label.startsWith("I-")) break;
                        }
                    }
                    if (relations.get(i).contains("trajector_O") || relations.get(i).contains("landmark_O")) {
                        for (int j=i; j < tokens.size();j++) {
                            tokens.get(j).label = tokens.get(j).label + "_O";
                            if (j < tokens.size()-1 && !tokens.get(j+1).label.startsWith("I-")) break;
                        }
                    }

                    maxTokenLength = Math.max(token.text.length(), maxTokenLength);
                    maxRelationLength = Math.max(relationsStr.length(), maxRelationLength);
                    String line = lineFormat(i, token.text, relationsStr, headStr, token.label, isTrigger);
                    if (includeXmlInfo) {
                        line += String.format("\t(%d,%d)\t%s", token.start, token.end, token.id);
                    }
                    lines.add(line);
                }


//                pairRelations.forEach((k,v)-> {
//                    if (v.size() > 1) {
//                        System.out.println(2);
//                    }
//                });

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

        lines = getMultiHeadFormatLinkLines(srcDir, linkTypes, true, true, includeXmlInfo, shuffle);
        FileUtil.createDir(targetFilePath + "/" + "Bi-AllLink-Head/");
        FileUtil.writeFile(targetFilePath + "/" + "Bi-AllLink-Head/" + mode + ".txt", lines);

//        lines = getMultiHeadFormatLinkLines(srcDir, linkTypes, false,false,includeXmlInfo, shuffle);
//        FileUtil.writeFile(targetFilePath + "/" + "AllLink-Tail/" + mode + ".txt", lines);

//        lines = getMultiHeadFormatLinkLines(srcDir, linkTypes, true, false, shuffle);
//        FileUtil.writeFile(targetFilePath + "/" + "Bi-AllLink-Tail/" + mode + ".txt", lines);

    }

    // 生成Multi-head selection格式的语料
    private static void run_config2(String srcDir, String targetFilePath,List<String> linkTypes, String mode,
                                    boolean includeXmlInfo,  boolean shuffle) {

        List<String> lines;
        lines = getMultiHeadFormatLinkLines(srcDir, linkTypes, false,true, includeXmlInfo,shuffle);
        lines = lines.stream().map(line->line.replaceAll("SPATIAL_SIGNAL(_.)+\t", "SPATIAL_SIGNAL\t"))
                .collect(Collectors.toList());
        FileUtil.createDir(targetFilePath + "/" + "AllLink-Head/");
        FileUtil.writeFile(targetFilePath + "/" + "AllLink-Head/" + mode + ".txt", lines);


        lines = getMultiHeadFormatLinkLines(srcDir, linkTypes, true,true, includeXmlInfo,shuffle);
        lines = lines.stream().map(line->line.replaceAll("SPATIAL_SIGNAL(_.)+\t", "SPATIAL_SIGNAL\t"))
                .collect(Collectors.toList());
        FileUtil.createDir(targetFilePath + "/" + "Bi-AllLink-Head/");
        FileUtil.writeFile(targetFilePath + "/" + "Bi-AllLink-Head/" + mode + ".txt", lines);

    }


    private static List<String> get_config1_lines(String srcDir, String elementPredPath, List<String> linkTypes,
                                   boolean includeXmlInfo,  boolean shuffle, boolean bidirectional) {
        List<String> predElementLines = FileUtil.readLines(elementPredPath);
        List<String> oriGoldLines = getMultiHeadFormatLinkLines(srcDir, linkTypes, bidirectional,true, includeXmlInfo,shuffle);
        oriGoldLines = oriGoldLines.stream().map(line->line.replaceAll("SPATIAL_SIGNAL(_.)+\t", "SPATIAL_SIGNAL\t"))
                .collect(Collectors.toList());
        List<String> newGoldLines = new ArrayList<>();

        List<List<String>> predGroups = CollectionUtils.split(predElementLines, "");
        List<List<String>> goldGroups = CollectionUtils.split(oriGoldLines, "");
        int idx = 0;
        for (List<String> goldGroup : goldGroups) {
            String goldSen = goldGroup.stream().map(x -> x.split("\\s+")[1]).collect(Collectors.joining(" "));;
            do {
                List<String> predGroup = predGroups.get(idx++);
                if (goldGroup.size() != predGroup.size()) continue;
                String predSen = predGroup.stream().map(x -> x.split(" ")[0]).collect(Collectors.joining(" "));
                if (goldSen.equals(predSen)) {
                    for (int j = 0; j < goldGroup.size(); j++) {
                        String[] goldArr = goldGroup.get(j).split("\t");
                        String[] predArr = predGroup.get(j).split(" ");
                        String oriLabel = goldArr[goldArr.length - 2];
                        String newLabel = predArr[predArr.length - 1];
                        goldArr[goldArr.length - 2] = StringUtils.rightPad(newLabel, oriLabel.length());
                        goldGroup.set(j, String.join("\t", goldArr));
                    }
                    break;
                }
            } while (true);
            newGoldLines.addAll(goldGroup);
            newGoldLines.add("");
        }
        return newGoldLines;
    }

    private static void run_config1(String srcDir, String targetFilePath, String elementPredPath, List<String> linkTypes, String mode,
                                    boolean includeXmlInfo,  boolean shuffle) {
        List<String> lines;
        lines = get_config1_lines(srcDir, elementPredPath, linkTypes, includeXmlInfo, shuffle, false);
        FileUtil.createDir(targetFilePath + "/" + "AllLink-Head/");
        FileUtil.writeFile(targetFilePath + "/" + "AllLink-Head/" + mode + ".txt", lines);

        lines = get_config1_lines(srcDir, elementPredPath, linkTypes, includeXmlInfo, shuffle, true);
        FileUtil.createDir(targetFilePath + "/" + "Bi-AllLink-Head/");
        FileUtil.writeFile(targetFilePath + "/" + "Bi-AllLink-Head/" + mode + ".txt", lines);
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
        String target_dir;

        target_dir = "data/SpaceEval2015/processed_data/MHS/configuration3";
        GenerateMultiHeadCorpus.run_config3(train_path, target_dir, linkTypes,"train",false, false);
        GenerateMultiHeadCorpus.run_config3(gold_path, target_dir, linkTypes, "dev", false, false);
        GenerateMultiHeadCorpus.run_config3(gold_path, target_dir, linkTypes, "test", false, false);


        target_dir = "data/SpaceEval2015/processed_data/MHS_xml/configuration3";
        GenerateMultiHeadCorpus.run_config3(train_path, target_dir, linkTypes,"train",true, false);
        GenerateMultiHeadCorpus.run_config3(gold_path, target_dir, linkTypes, "dev", true, false);
        GenerateMultiHeadCorpus.run_config3(gold_path, target_dir, linkTypes, "test", true, false);


        target_dir = "data/SpaceEval2015/processed_data/MHS/configuration2";
        GenerateMultiHeadCorpus.run_config2(train_path, target_dir, linkTypes,"train",false, false);
        GenerateMultiHeadCorpus.run_config2(gold_path, target_dir, linkTypes, "dev", false, false);
        GenerateMultiHeadCorpus.run_config2(gold_path, target_dir, linkTypes, "test", false, false);

        target_dir = "data/SpaceEval2015/processed_data/MHS_xml/configuration2";
        GenerateMultiHeadCorpus.run_config2(train_path, target_dir, linkTypes,"train",true, false);
        GenerateMultiHeadCorpus.run_config2(gold_path, target_dir, linkTypes, "dev", true, false);
        GenerateMultiHeadCorpus.run_config2(gold_path, target_dir, linkTypes, "test", true, false);


        String elementPredPath = "data/SpaceEval2015/processed_data/NER/config1/predict.txt";
        target_dir = "data/SpaceEval2015/processed_data/MHS/configuration1_1";

        GenerateMultiHeadCorpus.run_config2(train_path, target_dir, linkTypes,"train",false, false);
        GenerateMultiHeadCorpus.run_config1(gold_path, target_dir, elementPredPath, linkTypes,"dev",false, false);
        GenerateMultiHeadCorpus.run_config1(gold_path, target_dir, elementPredPath, linkTypes,"test",false, false);

        target_dir = "data/SpaceEval2015/processed_data/MHS_xml/configuration1_1";
        GenerateMultiHeadCorpus.run_config2(train_path, target_dir, linkTypes,"train",true, false);
        GenerateMultiHeadCorpus.run_config2(gold_path, target_dir, linkTypes,"dev",true, false);
        GenerateMultiHeadCorpus.run_config2(gold_path, target_dir, linkTypes,"test",true, false);


        String elementTrainPath = "data/SpaceEval2015/processed_data/NER/config2/train.txt";
        elementPredPath = "data/SpaceEval2015/processed_data/NER/config2/predict.txt";
        target_dir = "data/SpaceEval2015/processed_data/MHS/configuration1_2";

        GenerateMultiHeadCorpus.run_config1(train_path, target_dir, elementTrainPath,linkTypes,"train",false, false);
        GenerateMultiHeadCorpus.run_config1(gold_path, target_dir, elementPredPath, linkTypes,"dev",false, false);
        GenerateMultiHeadCorpus.run_config1(gold_path, target_dir, elementPredPath, linkTypes,"test",false, false);

        target_dir = "data/SpaceEval2015/processed_data/MHS_xml/configuration1_2";
        GenerateMultiHeadCorpus.run_config2(train_path, target_dir, linkTypes,"train",true, false);
        GenerateMultiHeadCorpus.run_config2(gold_path, target_dir, linkTypes,"dev",true, false);
        GenerateMultiHeadCorpus.run_config2(gold_path, target_dir, linkTypes,"test",true, false);

    }
}
