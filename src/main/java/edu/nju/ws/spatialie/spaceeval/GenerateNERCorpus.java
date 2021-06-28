package edu.nju.ws.spatialie.spaceeval;

import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.utils.FileUtil;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.reflect.Array;
import java.util.*;

import static edu.nju.ws.spatialie.spaceeval.SpaceEvalUtils.*;

public class GenerateNERCorpus {
    private static final Set<Integer> lengths = new TreeSet<>();


    private static String getTokenLabel(List<Span> elements, Span target) {
        int index = Collections.binarySearch(elements, target);
        String label = "O";
        if (index >= 0) {
            label = "B-" + elements.get(index).label;
        } else if(index < -1) {
            index = -index - 2;
            if (target.end <= elements.get(index).end) {
                label = "I-" + elements.get(index).label;
            }
        }
        return label;
    }

    // 生成NER格式语料
    private static void run_config1(String srcDir, String targetFilePath) {
        Map<Integer, Integer> map = new TreeMap<>();
        List<File> files = FileUtil.listFiles(srcDir);
        int maxLength = 0;
        List<String> lines = new ArrayList<>();

        for (File file: files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<Span> elements = spaceEvalDoc.getElements();
            List<List<Span>> sentences = spaceEvalDoc.getSentences();
            for (List<Span> sentence: sentences) {
                lines.add(file.getName());
                for (Span token: sentence) {
                    String label = getTokenLabel(elements, token);
                    lines.add(token.text + " " + label);
                }
                lines.add("");
                if (sentence.size() > maxLength) {
                    System.out.println(sentence.size());
                }
                maxLength = Math.max(maxLength, sentence.size());

                int cnt = map.getOrDefault(sentence.size(), 0);
                map.put(sentence.size(), cnt + 1);

                if (sentence.size() == 79) {
                    System.out.println(sentence);
                }

            }
        }
        String dir = targetFilePath.substring(0, targetFilePath.lastIndexOf("/"));
        FileUtil.createDir(dir);
        FileUtil.writeFile(targetFilePath, lines);
        System.out.println(map);
    }


    private static void run_config1b(String srcDir, String targetFilePath) {
        Map<Integer, Integer> map = new TreeMap<>();
        List<File> files = FileUtil.listFiles(srcDir);
        int maxLength = 0;
        List<String> lines = new ArrayList<>();

        for (File file: files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<Span> elements = spaceEvalDoc.getElements();
            List<List<Span>> sentences = spaceEvalDoc.getSentences();

            List<BratEvent> links = spaceEvalDoc.getAllLinks();
            Set<String> qsTriggers = new HashSet<>();
            Set<String> oTriggers = new HashSet<>();
            links.forEach(link -> {
                if (link.hasRole(TRIGGER)) {
                    if (link.getType().equals(QSLINK)) {
                        qsTriggers.addAll(link.getRoleIds(TRIGGER));
                    }
                    if (link.getType().equals(OLINK)) {
                        oTriggers.addAll(link.getRoleIds(TRIGGER));
                    }
                }
            });

            for (List<Span> sentence: sentences) {
                for (Span token: sentence) {
                    String label = getTokenLabel(elements, token);
                    if (qsTriggers.contains(token.id)) {
                        label += "_Q";
                    }
                    if (oTriggers.contains(token.id)) {
                        label += "_O";
                    }
                    lines.add(token.text + " " + label);
                }
                lines.add("");
                if (sentence.size() > maxLength) {
                    System.out.println(sentence.size());
                }
                maxLength = Math.max(maxLength, sentence.size());

                int cnt = map.getOrDefault(sentence.size(), 0);
                map.put(sentence.size(), cnt + 1);

                if (sentence.size() == 79) {
                    System.out.println(sentence);
                }

            }
        }
        String dir = targetFilePath.substring(0, targetFilePath.lastIndexOf("/"));
        FileUtil.createDir(dir);
        FileUtil.writeFile(targetFilePath, lines);
        System.out.println(map);
    }

    private static void run_config2(String srcDir, String targetFilePath) {
        List<File> files = FileUtil.listFiles(srcDir);
        List<String> lines = new ArrayList<>();

        Set<String> spatialElements = new HashSet<>(Arrays.asList(SPATIAL_ENTITY, PATH, PLACE, NONMOTION_EVENT));
        for (File file: files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<Span> elements = spaceEvalDoc.getElements();
            List<List<Span>> sentences = spaceEvalDoc.getSentences();
            for (List<Span> sentence: sentences) {
                for (Span token: sentence) {
                    String label = getTokenLabel(elements, token);
                    if (!label.equals("O") && spatialElements.contains(label.substring(2))) {
                        label = label.substring(0,2) + "BE";
                    }
//                    if (label.equals(SPATIAL_SIGNAL) || label.equals(MOTION) || label.equals(MEASURE))
                    lines.add(token.text + " " + label);
                }
                lines.add("");
                lengths.add(sentence.size());
            }
//            System.out.println(file.getPath() + "is processed");
        }

        String dir = targetFilePath.substring(0, targetFilePath.lastIndexOf("/"));
        FileUtil.createDir(dir);

        FileUtil.writeFile(targetFilePath, lines);
    }


    private static void run_config3b(String srcDir, String targetFilePath) {
        List<File> files = FileUtil.listFiles(srcDir);
        int maxLength = 0;
        List<String> linkTypes = Arrays.asList(QSLINK, OLINK, MOVELINK);

        for (String linkType: linkTypes) {
            List<String> lines = new ArrayList<>();
            for (File file: files) {
                SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
                Map<String, Span> elementMap = spaceEvalDoc.getElementMap();
                List<BratEvent> links = spaceEvalDoc.getAllLinks();
                List<List<Span>> sentences = spaceEvalDoc.getSentences();

                List<Span> triggers = new ArrayList<>();
                for (BratEvent link: links) {
                    if (link.getType().equals(linkType) && link.hasRole(TRIGGER)) {
                        Span trigger = elementMap.get(link.getRoleId(TRIGGER));
                        if (linkType.equals(QSLINK) || linkType.equals(OLINK)) {
                            trigger.label = link.getAttribute("relType");
                        } else {
                            trigger.label = trigger.getAttribute(MOTION_CLASS);
                        }
                        triggers.add(trigger);
                    }
                }
                Collections.sort(triggers);
                for (List<Span> sentence: sentences) {
                    for (Span token: sentence) {
                        String label = getTokenLabel(triggers, token);
                        lines.add(token.text + " " + label);
                    }
                    lines.add("");
                    if (sentence.size() > maxLength) {
                        System.out.println(sentence.size());
                    }
                    maxLength = Math.max(maxLength, sentence.size());
                }
//            System.out.println(file.getPath() + "is processed");
            }

            String dir = targetFilePath.substring(0, targetFilePath.lastIndexOf("/") + 1)  + linkType;
            FileUtil.createDir(dir);
            String path = dir + targetFilePath.substring(targetFilePath.lastIndexOf("/"));
            FileUtil.writeFile(path, lines);
        }
    }


    public static void main(String [] args) {
        GenerateNERCorpus.run_config1("data/SpaceEval2015/raw_data/training++",
                "data/SpaceEval2015/processed_data/NER/config1/train.txt");
        GenerateNERCorpus.run_config1("data/SpaceEval2015/raw_data/gold++",
                "data/SpaceEval2015/processed_data/NER/config1/dev.txt");
        GenerateNERCorpus.run_config1("data/SpaceEval2015/raw_data/gold++",
                "data/SpaceEval2015/processed_data/NER/config1/test.txt");
        System.out.println(GenerateNERCorpus.lengths);

//        GenerateNERCorpus.run_config1b("data/SpaceEval2015/raw_data/training++",
//                "data/SpaceEval2015/processed_data/NER/config1b/train.txt");
//        GenerateNERCorpus.run_config1b("data/SpaceEval2015/raw_data/gold++",
//                "data/SpaceEval2015/processed_data/NER/config1b/dev.txt");
//        GenerateNERCorpus.run_config1b("data/SpaceEval2015/raw_data/gold++",
//                "data/SpaceEval2015/processed_data/NER/config1b/test.txt");
////
//
//
//        GenerateNERCorpus.run_config2("data/SpaceEval2015/raw_data/training++",
//                "data/SpaceEval2015/processed_data/NER/config2/train.txt");
//        GenerateNERCorpus.run_config2("data/SpaceEval2015/raw_data/gold++",
//                "data/SpaceEval2015/processed_data/NER/config2/dev.txt");
//        GenerateNERCorpus.run_config2("data/SpaceEval2015/raw_data/gold++",
//                "data/SpaceEval2015/processed_data/NER/config2/test.txt");
//        System.out.println(GenerateNERCorpus.lengths);
//
//        GenerateNERCorpus.run_config3b("data/SpaceEval2015/raw_data/training++",
//                "data/SpaceEval2015/processed_data/NER/config3b/train.txt");
//        GenerateNERCorpus.run_config3b("data/SpaceEval2015/raw_data/gold++",
//                "data/SpaceEval2015/processed_data/NER/config3b/dev.txt");
//        GenerateNERCorpus.run_config3b("data/SpaceEval2015/raw_data/gold++",
//                "data/SpaceEval2015/processed_data/NER/config3b/test.txt");
    }
}
