package edu.nju.ws.spatialie.spaceeval;

import com.google.common.collect.Multimap;
import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.utils.FileUtil;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static edu.nju.ws.spatialie.spaceeval.SpaceEvalUtils.*;

public class GenerateMultiHeadCorpus {


    private static List<Triple<String, String, String>> getBidirectionalSPOTriples(List<BratEvent> links) {
        List<Triple<String, String, String>> triples = new ArrayList<>();
        for (BratEvent link: links) {
            Multimap<String, String> roleMap = link.getRoleMap();
            if (link.getType().equals(SpaceEvalUtils.MOVELINK)) {
                if (roleMap.containsKey(MOVER) && roleMap.containsKey(TRIGGER)) {
                    for (String mover: roleMap.get(MOVER)) {
                        for (String trigger: roleMap.get(TRIGGER)) {
                            triples.add(new ImmutableTriple<>(mover, MOVER, trigger));
                            triples.add(new ImmutableTriple<>(trigger, TRIGGER, mover));
                        }
                    }
                }
            } else {
                if (roleMap.containsKey("trigger") || roleMap.containsKey("val")) {
                    for (String trajector: roleMap.get(TRAJECTOR)) {
                        for (String trigger: roleMap.get(TRIGGER)) {
                            triples.add(new ImmutableTriple<>(trajector, TRAJECTOR, trigger));
                            triples.add(new ImmutableTriple<>(trigger, TRIGGER, trajector));
                        }
                    }
                    for (String landmark: roleMap.get(LANDMARK)) {
                        for (String trigger: roleMap.get(TRIGGER)) {
                            triples.add(new ImmutableTriple<>(landmark, LANDMARK, trigger));
                            triples.add(new ImmutableTriple<>(trigger, TRIGGER, landmark));
                        }
                    }
                } else {
                    for (String trajector: roleMap.get(TRAJECTOR)) {
                        for (String landmark: roleMap.get(LANDMARK)) {
                            triples.add(new ImmutableTriple<>(trajector, "locatedIn", landmark));
                            triples.add(new ImmutableTriple<>(landmark, "include", trajector));
                        }
                    }
                }
            }
        }
        return triples.stream().distinct().collect(Collectors.toList());
    }

    private static List<Triple<String, String, String>> getSPOTriples(List<BratEvent> links) {

        List<Triple<String, String, String>> triples = new ArrayList<>();
        for (BratEvent link: links) {
            Multimap<String, String> roleMap = link.getRoleMap();
            if (link.getType().equals(SpaceEvalUtils.MOVELINK)) {
                if (roleMap.containsKey(MOVER) && roleMap.containsKey(TRIGGER)) {
                    for (String mover: roleMap.get(MOVER)) {
                        for (String trigger: roleMap.get(TRIGGER)) {
                            triples.add(new ImmutableTriple<>(mover, MOVER, trigger));
                        }
                    }
                }
            } else {
                if (roleMap.containsKey("trigger") || roleMap.containsKey("val")) {
                    for (String trajector: roleMap.get(TRAJECTOR)) {
                        for (String trigger: roleMap.get(TRIGGER)) {
                            triples.add(new ImmutableTriple<>(trajector, TRAJECTOR, trigger));
                        }
                    }
                    for (String landmark: roleMap.get(LANDMARK)) {
                        for (String trigger: roleMap.get(TRIGGER)) {
                            triples.add(new ImmutableTriple<>(landmark, LANDMARK, trigger));
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


    private static List<Triple<String, String, String>> getSPOTriples_2(List<BratEvent> links) {
        List<Triple<String, String, String>> triples = new ArrayList<>();
        for (BratEvent link: links) {
            char linkType = link.getType().charAt(0);
            Multimap<String, String> roleMap = link.getRoleMap();
            if (link.getType().equals(SpaceEvalUtils.MOVELINK)) {
                if (roleMap.containsKey(MOVER) && roleMap.containsKey(TRIGGER)) {
                    for (String mover: roleMap.get(MOVER)) {
                        for (String trigger: roleMap.get(TRIGGER)) {
//                            triples.add(new ImmutableTriple<>(mover, MOVER, trigger));
                            triples.add(new ImmutableTriple<>(trigger, MOVER, mover));
                            triples.add(new ImmutableTriple<>(trigger, TRIGGER, trigger));
                        }
                    }
                }
            } else {
                if (roleMap.containsKey("trigger") || roleMap.containsKey("val")) {
                    for (String trajector: roleMap.get(TRAJECTOR)) {
                        for (String trigger: roleMap.get(TRIGGER)) {
//                            triples.add(new ImmutableTriple<>(trajector, TRAJECTOR, trigger));
                            triples.add(new ImmutableTriple<>(trigger, TRAJECTOR + "_" + linkType, trajector));
                            triples.add(new ImmutableTriple<>(trigger, TRIGGER + "_" + linkType, trigger));
                        }
                    }
                    for (String landmark: roleMap.get(LANDMARK)) {
                        for (String trigger: roleMap.get(TRIGGER)) {
//                            triples.add(new ImmutableTriple<>(landmark, LANDMARK, trigger));
                            triples.add(new ImmutableTriple<>(trigger, LANDMARK + "_" + linkType, landmark));
                            triples.add(new ImmutableTriple<>(trigger, TRIGGER + "_" + linkType, trigger));
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


    private static String lineFormat(int idx, String token, String relations, String heads, String label, String isTrigger) {
        int maxTokenLength = 15, maxRelationLength = 20;
        token = StringUtils.rightPad(token, maxTokenLength);
        label  = StringUtils.rightPad(label, 16);
        relations = StringUtils.rightPad(relations, maxRelationLength);
        heads = StringUtils.rightPad(heads, 5);
        isTrigger = StringUtils.rightPad(isTrigger, 3);
        return String.format("%d\t%s\t%s\t%s\t%s\t%s", idx, token, relations, heads, label, isTrigger);
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
            List<Triple<String, String, String>> oriTriples = bidirectional ? getBidirectionalSPOTriples(allLinks) : getSPOTriples_2(allLinks);
            List<Triple<Span, String, Span>> triples = oriTriples.stream()
                    .map(o -> new ImmutableTriple<>(elementMap.get(o.getLeft()), o.getMiddle(), elementMap.get(o.getRight())))
                    .sorted(Comparator.comparing(o->o.getLeft().start)).collect(Collectors.toList());
            List<List<Span>> sentences = spaceEvalDoc.getSentences();

            if (shuffle) {
                Collections.shuffle(sentences);
            }

            for (List<Span> tokens: sentences) {
                String sentenceText = tokens.stream().map(o->o.text).collect(Collectors.joining(" "));
//                if (sentenceText.startsWith("05-Oct-2001"))
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


                if (relations.stream().noneMatch(r -> r.size() > 0)) {
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


                    if (relations.get(i).contains("trigger_Q")) {
                        for (int j=i; j < tokens.size();j++) {
                            tokens.get(j).label = tokens.get(j).label + "_Q";
                            if (j < tokens.size()-1 && !tokens.get(j+1).label.startsWith("I-")) break;
                        }
                    }
                    if (relations.get(i).contains("trigger_O")) {
                        for (int j=i; j < tokens.size();j++) {
                            tokens.get(j).label = tokens.get(j).label + "_O";
                            if (j < tokens.size()-1 && !tokens.get(j+1).label.startsWith("I-")) break;
                        }
                    }

                    maxTokenLength = Math.max(token.text.length(), maxTokenLength);
                    maxRelationLength = Math.max(relationsStr.length(), maxRelationLength);
                    String line = lineFormat(i, token.text, relationsStr, headStr, token.label, isTrigger);
                    if (includeXmlInfo) {
                        line += "\t" + token.id;
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
    private static void run(String srcDir, String targetFilePath,List<String> linkTypes, String mode,
                            boolean includeXmlInfo,  boolean shuffle) {

        FileUtil.createDir(targetFilePath + "/" + "AllLink-Head/");

        List<String> lines;
        lines = getMultiHeadFormatLinkLines(srcDir, linkTypes, false,true, includeXmlInfo,shuffle);
        FileUtil.writeFile(targetFilePath + "/" + "AllLink-Head/" + mode + ".txt", lines);

//        lines = getMultiHeadFormatLinkLines(srcDir, linkTypes, true, true, shuffle);
//        FileUtil.writeFile(targetFilePath + "/" + "Bi-AllLink-Head/" + mode + ".txt", lines);

//        lines = getMultiHeadFormatLinkLines(srcDir, linkTypes, false,false,includeXmlInfo, shuffle);
//        FileUtil.writeFile(targetFilePath + "/" + "AllLink-Tail/" + mode + ".txt", lines);

//        lines = getMultiHeadFormatLinkLines(srcDir, linkTypes, true, false, shuffle);
//        FileUtil.writeFile(targetFilePath + "/" + "Bi-AllLink-Tail/" + mode + ".txt", lines);

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


        List<String> linkTypes = Arrays.asList(MOVELINK, QSLINK, OLINK, MEASURELINK);

        GenerateMultiHeadCorpus.run("data/SpaceEval2015/raw_data/training++",
                "data/SpaceEval2015/processed_data/MHS", linkTypes,
                "train", false, false);

        GenerateMultiHeadCorpus.run("data/SpaceEval2015/raw_data/gold++",
                "data/SpaceEval2015/processed_data/MHS", linkTypes,
                "dev", false, false);

        GenerateMultiHeadCorpus.run("data/SpaceEval2015/raw_data/gold++",
                "data/SpaceEval2015/processed_data/MHS", linkTypes,
                "test", false, false);


        GenerateMultiHeadCorpus.run("data/SpaceEval2015/raw_data/training++",
                "data/SpaceEval2015/processed_data/MHS_xml", linkTypes,
                "train", true, false);

        GenerateMultiHeadCorpus.run("data/SpaceEval2015/raw_data/gold++",
                "data/SpaceEval2015/processed_data/MHS_xml", linkTypes,
                "dev", true, false);

        GenerateMultiHeadCorpus.run("data/SpaceEval2015/raw_data/gold++",
                "data/SpaceEval2015/processed_data/MHS_xml", linkTypes,
                "test", true, false);
    }
}
