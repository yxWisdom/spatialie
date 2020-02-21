package edu.nju.ws.spatialie.spaceeval;

import com.google.common.collect.Multimap;
import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.utils.FileUtil;
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

    private static String lineFormat(int idx, String token, String label, String relations, String heads) {
        int maxTokenLength = 15, maxRelationLength = 20;
        token = StringUtils.rightPad(token, maxTokenLength);
        label  = StringUtils.rightPad(label, 16);
        relations = StringUtils.rightPad(relations, maxRelationLength);
        return String.format("%d\t%s\t%s\t%s\t%s", idx, token, label, relations, heads);
    }


    private static List<String> getMultiHeadFormatLinkLines(String srcDir, Collection<String> linkTypes,
                                                            boolean bidirectional,boolean use_head, boolean shuffle) {
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
                int sentenceStart = tokens.get(0).start, sentenceEnd = tokens.get(tokens.size()-1).end;
                List<Triple<Span, String, Span>> triplesInSentence = triples.stream().filter(o -> {
                    int start = Math.min(o.getLeft().start, o.getRight().start);
                    int end = Math.max(o.getLeft().end, o.getRight().end);
                    return sentenceStart <= start && end <= sentenceEnd;
                }).collect(Collectors.toList());
                List<List<String>> relations = tokens.stream().map(o->new ArrayList<String>()).collect(Collectors.toList());
                List<List<Integer>> heads = tokens.stream().map(o->new ArrayList<Integer>()).collect(Collectors.toList());
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
                    }
                }
                for (int i=0; i < tokens.size();i++) {
                    Span token = tokens.get(i);
                    if (relations.get(i).size() == 0) {
                        relations.get(i).add("NA");
                        heads.get(i).add(i);
                    }
//                    String relationsStr = "[" + StringUtils.join(relations.get(i), ",") + "]";
//                    String headStr = "[" + StringUtils.join(heads.get(i), ",") + "]";

                    String relationsStr = relations.get(i).toString();
                    String headStr = heads.get(i).toString();

                    maxTokenLength = Math.max(token.text.length(), maxTokenLength);
                    maxRelationLength = Math.max(relationsStr.length(), maxRelationLength);
                    lines.add(lineFormat(i, token.text, token.label, relationsStr, headStr));
                }
                lines.add("\n");
            }
        }
        System.out.println(maxTokenLength + " " + maxRelationLength);
        return lines;
    }


    // 生成Multi-head selection格式的语料
    private static void run(String srcDir, String targetFilePath,List<String> linkTypes, String mode, boolean shuffle) {
        List<String> lines;
        lines = getMultiHeadFormatLinkLines(srcDir, linkTypes, false,true,shuffle);
        FileUtil.writeFile(targetFilePath + "/" + "AllLink-Head/" + mode + ".txt", lines);

        lines = getMultiHeadFormatLinkLines(srcDir, linkTypes, true, true, shuffle);
        FileUtil.writeFile(targetFilePath + "/" + "Bi-AllLink-Head/" + mode + ".txt", lines);

        lines = getMultiHeadFormatLinkLines(srcDir, linkTypes, false,false,shuffle);
        FileUtil.writeFile(targetFilePath + "/" + "AllLink-Tail/" + mode + ".txt", lines);

        lines = getMultiHeadFormatLinkLines(srcDir, linkTypes, true, false, shuffle);
        FileUtil.writeFile(targetFilePath + "/" + "Bi-AllLink-Tail/" + mode + ".txt", lines);

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
                "data/SpaceEval2015/processed_data/multi-head", linkTypes, "train", true);

        GenerateMultiHeadCorpus.run("data/SpaceEval2015/raw_data/gold++",
                "data/SpaceEval2015/processed_data/multi-head", linkTypes, "dev", false);

        GenerateMultiHeadCorpus.run("data/SpaceEval2015/raw_data/gold++",
                "data/SpaceEval2015/processed_data/multi-head", linkTypes, "test", false);
    }
}
