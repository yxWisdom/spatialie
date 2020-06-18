package edu.nju.ws.spatialie.spaceeval;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.utils.FileUtil;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static edu.nju.ws.spatialie.spaceeval.SpaceEvalUtils.*;

public class GenerateSRLCorpus_new {
//    private static List<BratEvent> mergeLink(List<BratEvent> links) {
//        Map<String, List<BratEvent>> group = new HashMap<>();
//        List<BratEvent> mergedLinks = new ArrayList<>();
//        for (BratEvent link: links) {
//            String key = null;
//            if (!link.getRoleId("trigger").equals("")) {
//               key = link.getRoleId("trigger");
//            }
//            if (!link.getRoleId("val").equals("")) {
//                key = link.getRoleId("val");
//            }
//            if (key != null) {
//                group.putIfAbsent(key + link.getType(), new ArrayList<>());
//                group.get(key + link.getType()).add(link);
//            } else {
//                mergedLinks.add(link);
//            }
//        }
//        for (Map.Entry<String, List<BratEvent>> entry: group.entrySet()) {
//            Multimap<String, String> map = HashMultimap.create();
//            for (BratEvent bratEvent: entry.getValue()) {
//                map.putAll(bratEvent.getRoleMap());
//            }
//            BratEvent bratEvent = entry.getValue().get(0);
//            bratEvent.setRoleMap(map);
//            mergedLinks.add(bratEvent);
//        }
//        return mergedLinks;
//    }

    private static List<String> getSRLFormatLinkLines(String srcDir, Collection<String> linkTypes, boolean includeTrigger, boolean shuffle) {
        return getSRLFormatLinkLines(srcDir, linkTypes, includeTrigger, shuffle, 1);
    }


//    static String parseToSample(List<String> tokens, List<String> labels, List<String> roles) {
//        int predicateStartIdx = roles.indexOf("B-trigger");
//        int predicateEndIndex = Math.max(roles.lastIndexOf("I-trigger"), roles.lastIndexOf("B-trigger"));
//        String line = String.format("%d %d\t%s\t%s\t%s", predicateStartIdx, predicateEndIndex, String.join(" ", tokens)
//                , String.join(" ", labels), String.join(" ", roles));
//        return line;
//    }


//    private static List<String> getSRLFormatLinkLinesNew(String srcDir, Collection<String> linkTypes, boolean includeTrigger,
//    boolean shuffle) {
//        List<File> files = FileUtil.listFiles(srcDir);
//        List<String> lines = new ArrayList<>();
//        Set<String> acceptRoles = new HashSet<>(Arrays.asList("trajector", "landmark", "trigger", "mover"));
//        for (File file: files) {
//            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
//            List<Span> elements = spaceEvalDoc.getElements();
//            Map<String, Span> elementMap = spaceEvalDoc.getElementMap();
//            List<BratEvent> allMergedLinks = spaceEvalDoc.getMergedLinks().stream()
//                    .filter(o -> linkTypes.contains(o.getType())).collect(Collectors.toList());
//
//            Set<String> triggerInLinks = allMergedLinks.stream().flatMap(link -> link.getRoleMap().values().stream())
//                    .collect(Collectors.toSet());
//
//            List<Span> spatialSignals = elements.stream()
//                    .filter(e -> e.label.equals(SPATIAL_SIGNAL) && !triggerInLinks.contains(e.id))
//                    .collect(Collectors.toList());
//
//            spatialSignals.forEach(ss -> {
//                if (ss.semantic_type.equals(DIR_TOP) || ss.semantic_type.equals(TOPOLOGICAL)) {
//                    BratEvent link = new BratEvent();
//                    link.setType(QSLINK);
//                    link.getRoleMap().put(TRIGGER, ss.id);
//                    link.setStartEnd(ss.start, ss.end);
//                    allMergedLinks.add(link);
//                }
//                if (ss.semantic_type.equals(DIR_TOP) || ss.semantic_type.equals(DIRECTIONAL)) {
//                    BratEvent link = new BratEvent();
//                    link.setType(OLINK);
//                    link.getRoleMap().put(TRIGGER, ss.id);
//                    link.setStartEnd(ss.start, ss.end);
//                    allMergedLinks.add(link);
//                }
//            });
//
//
//            for (BratEvent link : allMergedLinks) {
//                List<String> tokens = new ArrayList<>();
//                List<String> labels = new ArrayList<>();
//                List<String> roles = new ArrayList<>();
//
//                Multimap<String, String> usedRoleMap = HashMultimap.create();
//                List<Span> roleTags = new ArrayList<>();
//                link.getRoleMap().forEach((role, eid) -> {
//                    if (role.equals("val"))
//                        role = "trigger";
//                    if (acceptRoles.contains(role)) {
//                        Span element = elementMap.get(eid);
//                        usedRoleMap.put(role, eid);
//                        // TODO: 每种关系trigger
//                        role = link.getType().charAt(0) + "_" + role;
//                        roleTags.add(new Span(element.id, element.text, role, element.start, element.end));
//                    }
//                });
////                if (!usedRoleMap.containsKey("trigger"))
////                    continue;
//                // 只保留有用的role
//                link.setRoleMap(usedRoleMap);
//                Collections.sort(roleTags);
//                List<Span> tokensOfLink;
//                if (spaceEvalDoc.getAllSentencesOfLink(link).size() > 1) {
//                    List<Span> trigger = usedRoleMap.get("trigger").stream().map(elementMap::get).collect(Collectors.toList());
//                    if (trigger.size() == 0)
//                        continue;
//                    tokensOfLink = spaceEvalDoc.getAllTokenOfElements(trigger);
//                } else {
//                    tokensOfLink =  spaceEvalDoc.getAllTokenOfLink(link);
//                }
//
//
//                for (Span token : tokensOfLink) {
//                    Span tmpTag = new Span("", "", "", token.start, token.end);
//                    String label = "O", role = "O";
//                    int index = Collections.binarySearch(elements, tmpTag);
//                    if (index >= 0) {
//                        label = "B-" + elements.get(index).label;
//                    } else if ((index = -index - 2) >= 0 && token.end <= elements.get(index).end) {
//                        label = "I-" + elements.get(index).label;
//                    }
//
//                    index = Collections.binarySearch(roleTags, tmpTag);
//                    if (index >= 0) {
//                        role = "B-" + roleTags.get(index).label;
//                    } else if ((index = -index - 2) >= 0 && token.end <= roleTags.get(index).end) {
//                        role = "I-" + roleTags.get(index).label;
//                    }
//                    tokens.add(token.text);
//                    labels.add(label);
//                    roles.add(role);
//                }
//
//                if (!includeTrigger && tokensOfLink.stream().map(x->x.text).collect(Collectors.joining(" ")).startsWith("Calle de Alcalá merges with ")) {
//                    System.out.println();
//                }
//
//                int predicateStartIdx = roles.indexOf("B-trigger");
//                int predicateEndIndex = Math.max(roles.lastIndexOf("I-trigger"), roles.lastIndexOf("B-trigger"));
//                String line = String.format("%d %d\t%s\t%s\t%s", predicateStartIdx, predicateEndIndex, String.join(" ", tokens)
//                        , String.join(" ", labels), String.join(" ", roles));
//                if (includeTrigger && predicateStartIdx >= 0) {
//                    lines.add(line);
//                }
//                if (!includeTrigger && line.contains("trajector") && line.contains("landmark") && !line.contains("trigger")) {
//                    lines.add(line);
//                }
//
//            }
//        }
//
//        lines = lines.subList(0, (int)(Math.ceil(lines.size()*proportion)));
//
//        if (shuffle) {
//            Collections.shuffle(lines);
//        }
//        return lines.stream().distinct().collect(Collectors.toList());
//    }




    private static List<String> getSRLFormatLinkLines(String srcDir, Collection<String> linkTypes, boolean includeTrigger,
                                                      boolean shuffle, double proportion) {
        List<File> files = FileUtil.listFiles(srcDir);
        List<String> lines = new ArrayList<>();
        for (File file: files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<Span> elements = spaceEvalDoc.getElements();
            Map<String, Span> elementMap = spaceEvalDoc.getElementMap();
            List<BratEvent> allMergedLinks = spaceEvalDoc.getMergedLinks().stream()
                    .filter(o -> linkTypes.contains(o.getType())).collect(Collectors.toList());

            Set<String> triggerInLinks = new HashSet<>();
            for (BratEvent link: allMergedLinks) {
                triggerInLinks.addAll(link.getRoleIds(TRIGGER));
                triggerInLinks.addAll(link.getRoleIds(VAL));
            }

            List<Span> spatialSignals = elements.stream()
                    .filter(e -> e.label.equals(SPATIAL_SIGNAL) && !triggerInLinks.contains(e.id))
                    .collect(Collectors.toList());

            spatialSignals.forEach(ss -> {
                if (ss.semantic_type.equals(DIR_TOP) || ss.semantic_type.equals(TOPOLOGICAL)) {
                    BratEvent link = new BratEvent();
                    link.setType(QSLINK);
                    link.getRoleMap().put(TRIGGER, ss.id);
                    link.setStartEnd(ss.start, ss.end);
                    allMergedLinks.add(link);
                }
                if (ss.semantic_type.equals(DIR_TOP) || ss.semantic_type.equals(DIRECTIONAL)) {
                    BratEvent link = new BratEvent();
                    link.setType(OLINK);
                    link.getRoleMap().put(TRIGGER, ss.id);
                    link.setStartEnd(ss.start, ss.end);
                    allMergedLinks.add(link);
                }
            });


            Set<String> acceptRoles = new HashSet<>(Arrays.asList("trajector", "landmark", "trigger", "mover"));
            for (BratEvent link : allMergedLinks) {
                List<String> tokens = new ArrayList<>();
                List<String> labels = new ArrayList<>();
                List<String> roles = new ArrayList<>();

                Multimap<String, String> usedRoleMap = HashMultimap.create();
                List<Span> roleTags = new ArrayList<>();
                link.getRoleMap().forEach((role, eid) -> {
                    if (role.equals("val"))
                        role = "trigger";
                    if (acceptRoles.contains(role)) {
                        Span element = elementMap.get(eid);
                        usedRoleMap.put(role, eid);
                        // TODO: 每种关系trigger
                        if (role.equals(TRIGGER))
                            role = link.getType().charAt(0) + "_" + role;
                        roleTags.add(new Span(element.id, element.text, role, element.start, element.end));
                    }
                });
//                if (!usedRoleMap.containsKey("trigger"))
//                    continue;
                // 只保留有用的role
                link.setRoleMap(usedRoleMap);
                Collections.sort(roleTags);
                List<Span> tokensOfLink;
                if (spaceEvalDoc.getAllSentencesOfLink(link).size() > 1) {
                    List<Span> trigger = usedRoleMap.get("trigger").stream().map(elementMap::get).collect(Collectors.toList());
                    if (trigger.size() == 0)
                        continue;
                    tokensOfLink = spaceEvalDoc.getAllTokenOfElements(trigger);
                } else {
                    tokensOfLink =  spaceEvalDoc.getAllTokenOfLink(link);
                }

                for (Span token : tokensOfLink) {
                    Span tmpTag = new Span("", "", "", token.start, token.end);
                    String label = "O", role = "O";
                    int index = Collections.binarySearch(elements, tmpTag);
                    if (index >= 0) {
                        label = "B-" + elements.get(index).label;
                    } else if ((index = -index - 2) >= 0 && token.end <= elements.get(index).end) {
                        label = "I-" + elements.get(index).label;
                    }

                    index = Collections.binarySearch(roleTags, tmpTag);
                    if (index >= 0) {
                        role = "B-" + roleTags.get(index).label;
                    } else if ((index = -index - 2) >= 0 && token.end <= roleTags.get(index).end) {
                        role = "I-" + roleTags.get(index).label;
                    }
                    tokens.add(token.text);
                    labels.add(label);
                    roles.add(role);
                }
                if (!includeTrigger && tokensOfLink.stream().map(x->x.text).collect(Collectors.joining(" ")).startsWith("Calle de Alcalá merges with ")) {
                    System.out.println();
                }
                String triggerLabel = link.getType().charAt(0) + "_trigger";
                String linkType = link.getType();

                int predicateStartIdx = roles.indexOf("B-" + triggerLabel);
                int predicateEndIndex = Math.max(roles.lastIndexOf("I-" + triggerLabel), predicateStartIdx);
                String line = String.format("%d %d %s\t%s\t%s\t%s", predicateStartIdx, predicateEndIndex, linkType, String.join(" ", tokens)
                        , String.join(" ", labels), String.join(" ", roles));
//                int predicateStartIdx = roles.indexOf("B-trigger");
//                int predicateEndIndex = Math.max(roles.lastIndexOf("I-trigger"), predicateStartIdx);
//                String line = String.format("%d %d\t%s\t%s\t%s", predicateStartIdx, predicateEndIndex, String.join(" ", tokens)
//                        , String.join(" ", labels), String.join(" ", roles));
                if (includeTrigger && predicateStartIdx >= 0) {
                    lines.add(line);
                }
                if (!includeTrigger && line.contains("trajector") && line.contains("landmark") && !line.contains("trigger")) {
                    lines.add(line);
                }
            }
         }
        lines = lines.subList(0, (int)(Math.ceil(lines.size()*proportion)));
        if (shuffle) {
            Collections.shuffle(lines);
        }
        return lines;
    }

    // 生成SRL格式的语料
    private static void run(String srcDir, String targetFilePath, String mode, boolean shuffle) {
        List<String> allLinkLines = getSRLFormatLinkLines(srcDir, Arrays.asList(MOVELINK, QSLINK, OLINK), true,shuffle);
        List<String> moveLinkLines = getSRLFormatLinkLines(srcDir, Arrays.asList(MOVELINK), true,shuffle);
        List<String> nonMoveLinkLines = getSRLFormatLinkLines(srcDir, Arrays.asList(QSLINK, OLINK), true,shuffle);
        List<String> qsLinkLines = getSRLFormatLinkLines(srcDir, Arrays.asList(QSLINK), true,shuffle);
        List<String> oLinkLines = getSRLFormatLinkLines(srcDir, Arrays.asList(OLINK), true,shuffle);
        List<String> measureLinkLines =  getSRLFormatLinkLines(srcDir, Arrays.asList(MEASURELINK), true,shuffle);
        List<String> qsNoTriggerLink = getSRLFormatLinkLines(srcDir, Arrays.asList(QSLINK), false,shuffle);
        List<String> oNoTriggerLink = getSRLFormatLinkLines(srcDir, Arrays.asList(OLINK), false,shuffle);
        List<String> noTriggerLink = getSRLFormatLinkLines(srcDir, Arrays.asList(OLINK, QSLINK), false,shuffle);



        String [] dirNames = {"NonMoveLink", "MoveLink", "AllLink","QSLink", "OLink", "DLink", "QSNoTrigger",
                "ONoTrigger", "NoTriggerLink"};

        for (String dirName: dirNames) {
            FileUtil.createDir(targetFilePath + "/" + dirName);
        }

        FileUtil.writeFile(targetFilePath + "/" + "NonMoveLink/" + mode + ".txt", nonMoveLinkLines);
        FileUtil.writeFile(targetFilePath + "/" + "MoveLink/" + mode + ".txt", moveLinkLines);
        FileUtil.writeFile(targetFilePath + "/" + "AllLink/" + mode + ".txt", allLinkLines);
        FileUtil.writeFile(targetFilePath + "/" + "QSLink/" + mode + ".txt", qsLinkLines);
        FileUtil.writeFile(targetFilePath + "/" + "OLink/" + mode + ".txt", oLinkLines);
        FileUtil.writeFile(targetFilePath + "/" + "DLink/" + mode + ".txt", measureLinkLines);
        FileUtil.writeFile(targetFilePath + "/" + "QSNoTrigger/" + mode + ".txt", qsNoTriggerLink);
        FileUtil.writeFile(targetFilePath + "/" + "ONoTrigger/" + mode + ".txt", oNoTriggerLink);
        FileUtil.writeFile(targetFilePath + "/" + "NoTriggerLink/" + mode + ".txt", noTriggerLink);
    }


    private static void generateSubCorpus(String srcDir, String targetDir, String mode, boolean shuffle, float proportion) {
        List<String> allLinkLines = getSRLFormatLinkLines(srcDir, Arrays.asList(MOVELINK, QSLINK, OLINK), true,shuffle, proportion);
        List<String> moveLinkLines = getSRLFormatLinkLines(srcDir, Arrays.asList(MOVELINK), true,shuffle, proportion);
        List<String> nonMoveLinkLines = getSRLFormatLinkLines(srcDir, Arrays.asList(QSLINK, OLINK), true,shuffle, proportion);
//        List<String> qsLinkLines = getSRLFormatLinkLines(srcDir, Arrays.asList(QSLINK), true,shuffle, proportion);
//        List<String> oLinkLines = getSRLFormatLinkLines(srcDir, Arrays.asList(OLINK), true,shuffle, proportion);
//        List<String> measureLinkLines =  getSRLFormatLinkLines(srcDir, Arrays.asList(MEASURELINK), true,shuffle, proportion);
//        List<String> qsNoTriggerLink = getSRLFormatLinkLines(srcDir, Arrays.asList(QSLINK), false,shuffle, proportion);
//        List<String> oNoTriggerLink = getSRLFormatLinkLines(srcDir, Arrays.asList(OLINK), false,shuffle, proportion);
//        List<String> noTriggerLink = getSRLFormatLinkLines(srcDir, Arrays.asList(OLINK, QSLINK), false,shuffle, proportion);


        FileUtil.createDir(targetDir + "/" + "NonMoveLink/");
        FileUtil.createDir(targetDir + "/" + "MoveLink/");
        FileUtil.createDir(targetDir + "/" + "AllLink/");

        FileUtil.writeFile(targetDir + "/" + "NonMoveLink/" + mode + ".txt", nonMoveLinkLines);
        FileUtil.writeFile(targetDir + "/" + "MoveLink/" + mode + ".txt", moveLinkLines);
        FileUtil.writeFile(targetDir + "/" + "AllLink/" + mode + ".txt", allLinkLines);
//        FileUtil.writeFile(targetFilePath + "/" + "QSLink/" + mode + ".txt", qsLinkLines);
//        FileUtil.writeFile(targetFilePath + "/" + "OLink/" + mode + ".txt", oLinkLines);
//        FileUtil.writeFile(targetFilePath + "/" + "DLink/" + mode + ".txt", measureLinkLines);
//        FileUtil.writeFile(targetFilePath + "/" + "QSNoTrigger/" + mode + ".txt", qsNoTriggerLink);
//        FileUtil.writeFile(targetFilePath + "/" + "ONoTrigger/" + mode + ".txt", oNoTriggerLink);
//        FileUtil.writeFile(targetFilePath + "/" + "NoTriggerLink/" + mode + ".txt", noTriggerLink);
    }

    public static void main(String [] args) {

        String trainDir = "data/SpaceEval2015/raw_data/training++";
        String devDir = "data/SpaceEval2015/raw_data/gold++";
//        String testDir = "data/SpaceEval2015/raw_data/gold++";
        String targetDir = "data/SpaceEval2015/processed_data/SRL_new";

        GenerateSRLCorpus_new.run(trainDir, targetDir, "train", false);
        GenerateSRLCorpus_new.run(devDir, targetDir, "dev", false);

//        GenerateSRLCorpus_new.run(testDir,targetDir, "test", false);

//        for (float proportion: new float[] {0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f}) {
//            targetDir = String.format("%s/proportion_%.1f", "data/SpaceEval2015/processed_data/SRL_subset", proportion);
//            GenerateSRLCorpus_new.generateSubCorpus(trainDir, targetDir, "train", false, proportion);
//            GenerateSRLCorpus_new.generateSubCorpus(devDir, targetDir, "dev", false, 1);
////            GenerateSRLCorpus_new.generateSubCorpus(testDir,targetDir, "test", false, 1);
//
//        }
    }
}
