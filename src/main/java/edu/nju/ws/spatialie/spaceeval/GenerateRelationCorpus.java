package edu.nju.ws.spatialie.spaceeval;

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

public class GenerateRelationCorpus {


    // 生成关系抽取格式（多元）的语料
    public static void main(String [] args) {
        GenerateRelationCorpus.run("data/SpaceEval2015/raw_data/training++",
                "data/SpaceEval2015/processed_data/relation", "train", false);

        GenerateRelationCorpus.run("data/SpaceEval2015/raw_data/gold++",
                "data/SpaceEval2015/processed_data/relation", "dev", false);

        GenerateRelationCorpus.run("data/SpaceEval2015/raw_data/gold++",
                "data/SpaceEval2015/processed_data/relation", "test", false);
    }
    //    public static void get
    private final static int moveLinkDistanceLimit = 10;
    private final static int nonMoveLinkDistanceLimit = 20;
    private final static int binaryNonMoveLinkDistanceLimit = 15;


    private static String getRelLine(String status, List<Span> tokens, Span ...elements) {
        StringBuilder line = new StringBuilder();
        line.append(status).append(" ");
        for (Span element: elements) {
            int startIdx = -1, endIdx = -1;
            for (int i = 0; i < tokens.size(); i++) {
                Span token = tokens.get(i);
                if (element.start == token.start) {
                    startIdx = i;
                }
                if (element.end == token.end) {
                    endIdx = i; break;
                }
            }
            line.append(startIdx).append(" ").append(endIdx).append(" ");
        }
        line.append(tokens.stream().map(t -> t.text).collect(Collectors.joining(" ")));
        return line.toString();
    }


    private static List<String> getTernaryLinks(String srcDir, String...linkTypes) {
        List<File> files = FileUtil.listFiles(srcDir);
        List<String> linkLines = new ArrayList<>();
        Set<String> linkTypeSet = new HashSet<>(Arrays.asList(linkTypes));
        for (File file: files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<List<Span>> sentences = spaceEvalDoc.getSentences();
            List<BratEvent> links = spaceEvalDoc.getAllLinks().stream().filter(o -> linkTypeSet.contains(o.getType()))
                    .collect(Collectors.toList());
            if (file.getName().startsWith("Bourbon")) {
                System.out.println();
            }
            Set<Triple<String, String, String>> goldTriples = links.stream().flatMap(x -> {
                String trigger = x.getType().equals(MEASURELINK) ? x.getRoleId(VAL): x.getRoleId(TRIGGER);
                List<Triple<String, String, String>> list = new ArrayList<>();
                Collection<String> trajectors = x.hasRole(TRAJECTOR) ? x.getRoleIds(TRAJECTOR): Arrays.asList("");;
                Collection<String> landmarks = x.hasRole(LANDMARK) ? x.getRoleIds(LANDMARK): Arrays.asList("");;
                trajectors.forEach(t -> landmarks.forEach(l -> list.add(new ImmutableTriple<>(t, trigger, l))));
                return list.stream();
            }).collect(Collectors.toSet());

            for (List<Span> tokensInSentence: sentences) {
                int start = tokensInSentence.get(0).start;
                int end = tokensInSentence.get(tokensInSentence.size()-1).end;
                List<Span> elementsInSentence = spaceEvalDoc.getElementsInSentence(start, end);

                if (tokensInSentence.stream().map(x->x.text).collect(Collectors.joining(" ")).startsWith("The financial district")) {
                        System.out.println();
                }
                List<Span> triggers = elementsInSentence.stream()
                        .filter(o -> {
                            boolean flag = false;
                            if (linkTypeSet.contains(SpaceEvalUtils.MEASURELINK)) {
                                flag = o.label.equals(SpaceEvalUtils.MEASURE);
                            }
                            if (linkTypeSet.contains(SpaceEvalUtils.QSLINK) || linkTypeSet.contains(SpaceEvalUtils.OLINK)) {
                                flag = flag || o.label.equals(SpaceEvalUtils.SPATIAL_SIGNAL);
                            }
                            return flag;
                        })
                        .collect(Collectors.toList());
                Span nullSpan = new Span("", "", "", -1, -1);
                elementsInSentence.add(nullSpan);
                triggers.add(nullSpan);
                for (Span trajector: elementsInSentence) {
                    for (Span landmark: elementsInSentence) {
                        if (trajector.equals(landmark))
                            continue;
                        for (Span trigger: triggers) {
                            if (trigger.equals(nullSpan) && (trajector.equals(nullSpan) || landmark.equals(nullSpan)))
                                continue;
                            int distance = calElementTokenLevelDistance(tokensInSentence, Arrays.asList(trajector, trigger, landmark));
                            if (goldTriples.contains(new ImmutableTriple<>(trajector.id, trigger.id, landmark.id))) {
                                linkLines.add(getRelLine("1", tokensInSentence, trajector, trigger, landmark));
                            } else if (trajectorTypes.contains(trajector.label) && landmarkTypes.contains(landmark.label)
                                    && distance < nonMoveLinkDistanceLimit) {
                                linkLines.add(getRelLine("0", tokensInSentence, trajector, trigger, landmark));
                            }
                        }
                    }
                }
            }
        }
        return linkLines.stream().distinct().collect(Collectors.toList());
    }


    private static List<String> getBinaryLinks(String srcDir, String ...linkTypes) {
        List<File> files = FileUtil.listFiles(srcDir);
        List<String> linkLines = new ArrayList<>();
        Set<String> linkTypeSet = new HashSet<>(Arrays.asList(linkTypes));
        for (File file: files) {

            if (file.getName().startsWith("Lima")) {
                System.out.println();
            }

            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<List<Span>> sentences = spaceEvalDoc.getSentences();
            List<BratEvent> links = spaceEvalDoc.getAllLinks().stream().filter(o -> linkTypeSet.contains(o.getType()))
                    .collect(Collectors.toList());
            Set<Pair<String, String>> goldPairs = links.stream()
                    .filter(x -> !x.hasRole(TRIGGER) && !x.hasRole(VAL))
                    .flatMap(x -> {
                        List<Pair<String, String>> list = new ArrayList<>();
                        Collection<String> trajectors = x.hasRole(TRAJECTOR) ? x.getRoleIds(TRAJECTOR): Arrays.asList("");;
                        Collection<String> landmarks = x.hasRole(LANDMARK) ? x.getRoleIds(LANDMARK): Arrays.asList("");;
                        trajectors.forEach(t -> landmarks.forEach(l -> list.add(new ImmutablePair<>(t, l))));
                        return list.stream();
                    }).collect(Collectors.toSet());

            for (List<Span> tokensInSentence: sentences) {
                if (tokensInSentence.stream().map(x->x.text).collect(Collectors.joining(" ")).startsWith("In the next few decades")) {
                    System.out.println();
                }
                int start = tokensInSentence.get(0).start;
                int end = tokensInSentence.get(tokensInSentence.size()-1).end;
                List<Span> elementsInSentence = spaceEvalDoc.getElementsInSentence(start, end);
                Span nullSpan = new Span("", "", "", -1, -1);
                elementsInSentence.add(nullSpan);
                for (Span trajector: elementsInSentence) {
                    for (Span landmark: elementsInSentence) {
                        if (trajector.equals(landmark) || trajector.equals(nullSpan) || landmark.equals(nullSpan))
                            continue;
                        int distance = calElementTokenLevelDistance(tokensInSentence, Arrays.asList(trajector, landmark));
                        if (goldPairs.contains(new ImmutablePair<>(trajector.id, landmark.id))) {
                            linkLines.add(getRelLine("1", tokensInSentence, trajector, landmark));
                        } else if (trajectorTypes.contains(trajector.label) && landmarkTypes.contains(landmark.label)
                                && distance < binaryNonMoveLinkDistanceLimit) {
                            linkLines.add(getRelLine("0", tokensInSentence, trajector, landmark));
                        }
                    }
                }
            }
        }
        return linkLines;
    }

    private static List<String> getMoveLinks(String srcDir) {
        List<File> files = FileUtil.listFiles(srcDir);
        List<String> linkLines = new ArrayList<>();
        for (File file: files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<List<Span>> sentences = spaceEvalDoc.getSentences();
            List<BratEvent> links = spaceEvalDoc.getMoveLink();
            Set<Pair<String, String>> goldPairs = links.stream()
                    .flatMap(x -> {
                        List<Pair<String, String>> list = new ArrayList<>();
                        Collection<String> movers = x.hasRole(MOVER) ? x.getRoleIds(MOVER): Arrays.asList("");;
                        Collection<String> triggers = x.hasRole(TRIGGER) ? x.getRoleIds(TRIGGER): Arrays.asList("");
                        movers.forEach(m -> triggers.forEach(t -> list.add(new ImmutablePair<>(m,t))));
                        return list.stream();
                    }).collect(Collectors.toSet());

            for (List<Span> tokensInSentence: sentences) {
                int start = tokensInSentence.get(0).start;
                int end = tokensInSentence.get(tokensInSentence.size()-1).end;
                List<Span> elementsInSentence = spaceEvalDoc.getElementsInSentence(start, end);
                Span nullSpan = new Span("", "", "", -1, -1);
                elementsInSentence.add(nullSpan);
                List<Span> triggers = elementsInSentence.stream()
                        .filter(o -> o.label.equals(SpaceEvalUtils.MOTION))
                        .collect(Collectors.toList());
                for (Span mover: elementsInSentence) {
                    for (Span trigger: triggers) {
                        if (mover.equals(trigger) || trigger.equals(nullSpan) || mover.equals(nullSpan))
                            continue;
                        int distance = calElementTokenLevelDistance(tokensInSentence, Arrays.asList(mover, trigger));
                        if (goldPairs.contains(new ImmutablePair<>(mover.id, trigger.id))) {
                            linkLines.add(getRelLine("1", tokensInSentence, mover, trigger));
                        } else if (moverTypes.contains(mover.label) && distance < moveLinkDistanceLimit) {
                            linkLines.add(getRelLine("0", tokensInSentence, mover, trigger));
                        }
                    }
                }
            }
        }
        return linkLines;
    }


    private static void run(String srcDir, String targetFilePath, String mode, boolean shuffle) {
        List<String> moveLinkLines = getMoveLinks(srcDir);
        List<String> qsLinkLines = getTernaryLinks(srcDir, QSLINK);
        List<String> oLinkLines = getTernaryLinks(srcDir, OLINK);
        List<String> measureLinkLines = getTernaryLinks(srcDir, MEASURELINK);
        List<String> nonMoveLinkLines = getTernaryLinks(srcDir, QSLINK, OLINK);
        List<String> binaryNonMoveLinkLines = getBinaryLinks(srcDir, QSLINK, OLINK, MEASURELINK);

        if (shuffle) {
            Collections.shuffle(moveLinkLines);
            Collections.shuffle(qsLinkLines);
            Collections.shuffle(oLinkLines);
            Collections.shuffle(measureLinkLines);
            Collections.shuffle(nonMoveLinkLines);
            Collections.shuffle(binaryNonMoveLinkLines);
        }

        FileUtil.writeFile(targetFilePath + "/" + "MoveLink/" + mode + ".txt", moveLinkLines);
        FileUtil.writeFile(targetFilePath + "/" + "QSLink/" + mode + ".txt", qsLinkLines);
        FileUtil.writeFile(targetFilePath + "/" + "OLink/" + mode + ".txt", oLinkLines);
        FileUtil.writeFile(targetFilePath + "/" + "DLink/" + mode + ".txt", measureLinkLines);
        FileUtil.writeFile(targetFilePath + "/" + "non-MoveLink/" + mode + ".txt", nonMoveLinkLines);
        FileUtil.writeFile(targetFilePath + "/" + "no-trigger-non-MoveLink/" + mode + ".txt", binaryNonMoveLinkLines);
    }

//    public static void run(String srcDir, String targetFilePath, String mode) {
//        List<File> files = FileUtil.getAllFiles(srcDir);
////        int maxLength = 0;
////        List<String> lines = new ArrayList<>();
//
////        List<String> allLinkLines = new ArrayList<>();
////        List<String> allMoveLin kLines = new ArrayList<>();
//        List<String> moveLinkLines = getMoveLinks(srcDir);
//        List<String> qsLinkLines = getTernaryLinks(srcDir, QSLINK);
//        List<String> oLinkLines = getTernaryLinks(srcDir, OLINK);
//        List<String> measureLinkLines = getTernaryLinks(srcDir, MEASURELINK);
//        List<String> nonMoveLinkLines = getTernaryLinks(srcDir, QSLINK, OLINK, MEASURELINK);
//        List<String> binaryNonMoveLinkLines = getBinaryLinks(srcDir, QSLINK, OLINK, MEASURELINK);
//
//
//        for (File file: files) {
//            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
////            List<Span> elements = spaceEvalDoc.getElements();
////            List<List<Span>> sentences = spaceEvalDoc.getSentences();
////            Map<String, Span> elementMap = spaceEvalDoc.getElementMap();
//            List<BratEvent> allLinks = spaceEvalDoc.getAllLinks();
//            List<BratEvent> qsLinks = spaceEvalDoc.getQSLink();
//            List<BratEvent> oLinks = spaceEvalDoc.getOLink();
//            List<BratEvent> moveLinks = spaceEvalDoc.getMoveLink();
//            List<BratEvent> mLinks = spaceEvalDoc.getMeasureLinks();
//
//            Set<Triple<String, String, String>> qsTriples = new HashSet<>();
//            Set<Triple<String, String, String>> oTriples = new HashSet<>();
//            Set<Triple<String, String, String>> mTriples = new HashSet<>();
//            Set<Pair<String, String>> moveTriples = new HashSet<>();
//
//
//            Set<Triple<String, String, String>> usedTriples = new HashSet<>();
//
//            qsLinks.forEach(o -> qsTriples.add(new ImmutableTriple<>(o.getRoleId("trajector"),
//                    o.getRoleId("trigger"), o.getRoleId("landmark"))));
//            oLinks.forEach(o -> oTriples.add(new ImmutableTriple<>(o.getRoleId("trajector"),
//                    o.getRoleId("trigger"), o.getRoleId("landmark"))));
//            mLinks.forEach(o -> mTriples.add(new ImmutableTriple<>(o.getRoleId("trajector"),
//                    o.getRoleId("val"), o.getRoleId("landmark"))));
//            moveLinks.forEach(o -> moveTriples.add(new ImmutablePair<>(o.getRoleId("mover"),
//                    o.getRoleId("trigger"))));
//
//            for (BratEvent link: allLinks) {
//                List<Span> tokens = spaceEvalDoc.getAllTokenOfLink(link);
//                if (tokens.size() == 0) {
//                    System.out.print(1);
//                }
//                int start = tokens.get(0).start, end = tokens.get(tokens.size()-1).end;
//                List<Span> elementInSentence = spaceEvalDoc.getElementsInSentence(start, end);
//                List<Span> spatialSignals = new ArrayList<>();
//                List<Span> motions =new ArrayList<>();
//                List<Span> spatialEntities = new ArrayList<>();
//                List<Span> trajectors = new ArrayList<>();
//                List<Span> landmarks = new ArrayList<>();
//                List<Span> movers = new ArrayList<>();
//                Set<Triple<String, String, String>> triples = new HashSet<>();
//                for (Span element: elementInSentence) {
//                    if (element.label.equals("SPATIAL_SIGNAL"))
//                        spatialSignals.add(element);
//                    if (element.label.equals("MOTION"))
//                        motions.add(element);
//                    if (element.label.equals("SPATIAL_ENTITY")){
//                        spatialEntities.add(element);
//                    }
//                    if (!element.label.equals("SPATIAL_SIGNAL"))
//                        trajectors.add(element);
//                    if (element.label.equals("PATH") || element.label.equals("PLACE") ||
//                            element.label.equals("SPATIAL_ENTITY")) {
//                        landmarks.add(element);
//                    }
//                }
//                spatialSignals.add(new Span("", "", "", -1, -1));
//                if (!link.getType().equals("MOVELINK")) {
//                    for (Span trajector: trajectors) {
//                        for (Span landmark: landmarks) {
//                            if (trajector.id.equals(landmark.id)) continue;
//                            for (Span trigger: spatialSignals) {
//                                Triple<String, String, String> triple =  new ImmutableTriple<>(trajector.id,trigger.id,landmark.id);
//                                boolean isTLink = qsTriples.contains(triple);
//                                boolean isOLink = oTriples.contains(triple);
//                                boolean isDLink = mTriples.contains(triple);
//
//                                if (usedTriples.contains(triple))
//                                    continue;
//                                usedTriples.add(triple);
//                                if (isTLink || isOLink || isDLink) {
//                                    nonMoveLinkLines.add(getRelLine("1", tokens,trajector,trigger,landmark));
//                                } else {
//                                    nonMoveLinkLines_neg.add(getRelLine("0", tokens,trajector,trigger,landmark));
//                                }
//                            }
//                        }
//                    }
//                } else {
//                    for (Span mover: spatialEntities) {
//                        for (Span trigger: motions) {
//                            if (moveTriples.contains(new ImmutablePair<>(mover.id, trigger.id))) {
//                                moveLinkLines.add(getRelLine("1", tokens, mover, trigger));
//                            } else {
//                                moveLinkLines_neg.add(getRelLine("0", tokens, mover, trigger));
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        moveLinkLines = moveLinkLines.stream().distinct().collect(Collectors.toList());
//        moveLinkLines_neg = moveLinkLines_neg.stream().distinct().collect(Collectors.toList());
//
//
//        nonMoveLinkLines = nonMoveLinkLines.stream().distinct().collect(Collectors.toList());
//        nonMoveLinkLines_neg = nonMoveLinkLines_neg.stream().distinct().collect(Collectors.toList());
//        Collections.shuffle(moveLinkLines_neg);
//        Collections.shuffle(nonMoveLinkLines_neg);
//
//        int negMoveLength =  Math.min(moveLinkLines_neg.size(), moveLinkLines.size() * 6);
//        moveLinkLines.addAll(moveLinkLines_neg.subList(0,negMoveLength));
//        int negNonMoveLength = Math.min(nonMoveLinkLines_neg.size(), nonMoveLinkLines.size() * 6);
//        nonMoveLinkLines.addAll(nonMoveLinkLines_neg.subList(0, negNonMoveLength));
//
//        Collections.shuffle(moveLinkLines);
//        Collections.shuffle(nonMoveLinkLines);
//        FileUtil.writeFile(targetFilePath + "/" + "NonMoveLink/" + mode + ".txt", nonMoveLinkLines);
//        FileUtil.writeFile(targetFilePath + "/" + "MoveLink/" + mode + ".txt", moveLinkLines);
//    }
//

}
