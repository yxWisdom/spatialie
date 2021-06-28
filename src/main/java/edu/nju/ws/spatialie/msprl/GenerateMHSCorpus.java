package edu.nju.ws.spatialie.msprl;

import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.spaceeval.Span;
import edu.nju.ws.spatialie.utils.CollectionUtils;
import edu.nju.ws.spatialie.utils.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.stream.Collectors;

import static edu.nju.ws.spatialie.msprl.MSpRLUtils.*;

/**
 * @author xyu
 * @version 1.0
 * @date 2021/3/26 20:56
 */
public class GenerateMHSCorpus {
    private static boolean coarseGrained = false;
    private static boolean fineGrained = false;
    private static boolean useSignalSuffix = false;

    static void processCorpus(String inPath, String outPath) {
//        List<Triple<String, String, String>> triples = new HashMap<>();
        List<String> lines = new ArrayList<>();
        List<String> roleTypes = Arrays.asList(TRAJECTOR, LANDMARK);

        MSpRLDoc doc = new MSpRLDoc(inPath);
        for (SpRLSentence sentence: doc.sentences) {
            Map<String, Span> elementMap = sentence.elementMap;
//            List<Span> elements = sentence.elements;
            List<Span> tokens = sentence.tokens;
//            List<Span> spatialSignals = elements.stream().filter(o->o.label.equals("SPATIAL_SIGNAL"))
//                    .sorted().collect(Collectors.toList());

            List<List<String>> relations = new ArrayList<>();
            List<List<Integer>> heads = new ArrayList<>();

            for (int i = 0; i < sentence.tokens.size(); i++) {
                relations.add(new ArrayList<>());
                heads.add(new ArrayList<>());
            }

            List<BratEvent> links = sentence.links;
            links.sort(Comparator.comparing(BratEvent::getType));

            Set<Triple<Integer, String, Integer>> triples = new HashSet<>();
            for (BratEvent link: links) {
                Span trigger = elementMap.get(link.getRoleId(TRIGGER));
                int headIdx = Collections.binarySearch(tokens, trigger);
                String linkType = link.getType();
                for (String roleType: roleTypes) {
                    if (link.hasRole(roleType)) {
                        Span entity = elementMap.get(link.getRoleId(roleType));
                        int tailIdx = Collections.binarySearch(tokens, entity);
//                        if (coarseGrained) {
//                            roleType += "_" + linkType.charAt(0);
//                        }
                        triples.add(new ImmutableTriple<>(headIdx, roleType, tailIdx));
                    }
                }
                if (fineGrained) {
                    String relType = link.getAttribute("relType");
                    triples.add(new ImmutableTriple<>(headIdx, relType, headIdx));
                }
                if (coarseGrained) {
                    triples.add(new ImmutableTriple<>(headIdx, linkType, headIdx));
                }
            }
            for (Triple<Integer, String, Integer> triple: triples) {
                int headIdx = triple.getLeft(), tailIdx = triple.getRight();
                relations.get(headIdx).add(triple.getMiddle());
                heads.get(headIdx).add(tailIdx);
            }

            for (int i = 0; i < tokens.size(); i++) {
                Span token = tokens.get(i);
                if (relations.get(i).size() == 0) {
                    relations.get(i).add("NA");
                    heads.get(i).add(i);
                }
//
//                if (useSignalSuffix) {
//                    if (relations.get(i).contains("trajector_Q") || relations.get(i).contains("landmark_Q")) {
//                        for (int j=i; j < tokens.size();j++) {
//                            tokens.get(j).label = tokens.get(j).label + "_Q";
//                            if (j < tokens.size()-1 && !tokens.get(j+1).label.startsWith("I-")) break;
//                        }
//                    }
//                    if (relations.get(i).contains("trajector_O") || relations.get(i).contains("landmark_O")) {
//                        for (int j=i; j < tokens.size();j++) {
//                            tokens.get(j).label = tokens.get(j).label + "_O";
//                            if (j < tokens.size()-1 && !tokens.get(j+1).label.startsWith("I-")) break;
//                        }
//                    }
//                }

                String word = StringUtils.rightPad(token.text, 15);
                String label  = StringUtils.rightPad(token.label, 21);
                String relationsStr = StringUtils.rightPad(relations.get(i).toString(), 30);
                String headStr = StringUtils.rightPad(heads.get(i).toString(), 20);
                String line = String.format("%d\t%s\t%s\t%s\t%s\t%s", i, word, label, token.id, headStr, relationsStr);
                lines.add(line);
            }
            lines.add("");
        }
        FileUtil.writeFile(outPath, lines);
    }
    static void run_config1() {
        coarseGrained = false;
        fineGrained = false;
//        useSignalSuffix = false;
        String trainPath = "data/mSpRL2017/clean_data/Sprl2017_train.xml";
        String goldPath = "data/mSpRL2017/clean_data/Sprl2017_gold.xml";
        String outputDir = "data/mSpRL2017/processed_data/config1/";
        FileUtil.createDir(outputDir);
        processCorpus(trainPath, outputDir + "train.txt");
        processCorpus(goldPath, outputDir + "dev.txt");
        processCorpus(goldPath, outputDir + "test.txt");
    }
    static void run_config2() {
        coarseGrained = true;
        fineGrained = false;
//        useSignalSuffix = true;
        String trainPath = "data/mSpRL2017/clean_data/Sprl2017_train.xml";
        String goldPath = "data/mSpRL2017/clean_data/Sprl2017_gold.xml";
        String outputDir = "data/mSpRL2017/processed_data/config2/";
        FileUtil.createDir(outputDir);
        processCorpus(trainPath, outputDir + "train.txt");
        processCorpus(goldPath, outputDir + "dev.txt");
        processCorpus(goldPath, outputDir + "test.txt");
    }

    static void run_config3() {
        coarseGrained = false;
        fineGrained = true;
//        useSignalSuffix = true;
        String trainPath = "data/mSpRL2017/clean_data/Sprl2017_train.xml";
        String goldPath = "data/mSpRL2017/clean_data/Sprl2017_gold.xml";
        String outputDir = "data/mSpRL2017/processed_data/config3/";
        FileUtil.createDir(outputDir);
        processCorpus(trainPath, outputDir + "train.txt");
        processCorpus(goldPath, outputDir + "dev.txt");
        processCorpus(goldPath, outputDir + "test.txt");
    }



    private static void run_pipeline(String srcFilePath, String targetFilePath, String elementPredPath) {
        List<String> predElementLines = FileUtil.readLines(elementPredPath);
        List<String> oriGoldLines = FileUtil.readLines(srcFilePath);
        List<String> newGoldLines = new ArrayList<>();

        List<List<String>> predGroups = CollectionUtils.split(predElementLines, "");
        List<List<String>> goldGroups = CollectionUtils.split(oriGoldLines, "");
        int idx = 0;
        for (List<String> goldGroup : goldGroups) {
            String goldSen = goldGroup.stream().map(x -> x.split("\\s+")[1]).collect(Collectors.joining(" "));
            do {
                List<String> predGroup = predGroups.get(idx++);
                if (goldGroup.size() != predGroup.size()) continue;
                String predSen = predGroup.stream().map(x -> x.split("\t")[0]).collect(Collectors.joining(" "));
                if (goldSen.equals(predSen)) {
                    for (int j = 0; j < goldGroup.size(); j++) {
                        String[] goldArr = goldGroup.get(j).split("\t");
                        String[] predArr = predGroup.get(j).split("\t");
                        String oriLabel = goldArr[2];
                        String newLabel = predArr[2];
                        goldArr[2] = StringUtils.rightPad(newLabel, oriLabel.length());
                        goldGroup.set(j, String.join("\t", goldArr));
                    }
                    break;
                }
            } while (true);
            newGoldLines.addAll(goldGroup);
            newGoldLines.add("");
        }

        FileUtil.writeFile(targetFilePath, newGoldLines);
//        lines = get_config1_lines(srcDir, elementPredPath, linkTypes, includeXmlInfo, shuffle, true);
//        FileUtil.createDir(targetFilePath + "/" + "Bi-AllLink-Head/");
//        FileUtil.writeFile(targetFilePath + "/" + "Bi-AllLink-Head/" + mode + ".txt", lines);
    }
    public static void main(String [] args) {
//        run_config1();
//        run_config2();
//        run_config3();

        String elementPredPath = "data/mSpRL2017/processed_data/NER/config1/predict.txt";
        run_pipeline("data/mSpRL2017/processed_data/joint/config2/dev.txt",
                "data/mSpRL2017/processed_data/pipeline/config2/dev.txt",
                elementPredPath);
        run_pipeline("data/mSpRL2017/processed_data/joint/config2/test.txt",
                "data/mSpRL2017/processed_data/pipeline/config2/test.txt",
                elementPredPath);

        run_pipeline("data/mSpRL2017/processed_data/joint/config3/dev.txt",
                "data/mSpRL2017/processed_data/pipeline/config3/dev.txt",
                elementPredPath);
        run_pipeline("data/mSpRL2017/processed_data/joint/config3/test.txt",
                "data/mSpRL2017/processed_data/pipeline/config3/tst.txt",
                elementPredPath);
    }
}
