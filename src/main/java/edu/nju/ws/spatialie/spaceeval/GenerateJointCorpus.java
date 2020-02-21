package edu.nju.ws.spatialie.spaceeval;

import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenerateJointCorpus {

    // 生成Joint NER语料
    private static void run(String srcDir, String targetFilePath) {
        List<File> files = FileUtil.listFiles(srcDir);
        int maxLength = 0;
        List<String> lines = new ArrayList<>();
        for (File file: files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<Span> elements = spaceEvalDoc.getElementsOfNERTask();
            List<List<Span>> sentences = spaceEvalDoc.getSentences();
            Map<String, Span> elementMap = spaceEvalDoc.getElementMap();
            List<BratEvent> allLinks = spaceEvalDoc.getAllLinks();

            List<Span> filteredTags = allLinks.stream().flatMap(e -> e.getRoleMap().values().stream())
                    .distinct().map(elementMap::get).sorted().collect(Collectors.toList());

            for (List<Span> sentence: sentences) {
                int flag = 0;
                List<String> tokens = new ArrayList<>(), labels = new ArrayList<>();
                for (Span token: sentence) {
                    Span tmpElement = new Span("", "", "", token.start,token.end);
                    int index = Collections.binarySearch(filteredTags, tmpElement);
                    if (index >= 0) {
                        flag = 1;
                    }
                    index = Collections.binarySearch(elements, tmpElement);
                    String label = "O";
                    if (index >= 0) {
                        label = "B-" + elements.get(index).label;
                    } else if((index = -index - 2) >= 0 && token.end <= elements.get(index).end) {
                        label = "I-" + elements.get(index).label;
                    }
                    tokens.add(token.text);
                    labels.add(label);
                }

                assert String.join(" ", tokens).split(" ").length == tokens.size();

                lines.add(String.format("%d\t%s\t%s", flag,
                        String.join(" ", tokens), String.join(" ", labels)));
                if (sentence.size() > maxLength) {
                    System.out.println(sentence.size());
                }
                maxLength = Math.max(maxLength, sentence.size());
            }
//            System.out.println(file.getPath() + "is processed");
        }
        FileUtil.writeFile(targetFilePath, lines);
        System.out.println(maxLength);
    }

    public static void main(String [] args) {
        GenerateJointCorpus.run("data/SpaceEval2015/raw_data/training++",
                "data/SpaceEval2015/processed_data/joint/train.txt");
        GenerateJointCorpus.run("data/SpaceEval2015/raw_data/gold++",
                "data/SpaceEval2015/processed_data/joint/dev.txt");
        GenerateJointCorpus.run("data/SpaceEval2015/raw_data/gold++",
                "data/SpaceEval2015/processed_data/joint/test.txt");
    }
}
