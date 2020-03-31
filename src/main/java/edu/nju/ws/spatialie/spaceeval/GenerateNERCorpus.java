package edu.nju.ws.spatialie.spaceeval;

import edu.nju.ws.spatialie.utils.FileUtil;

import java.io.File;
import java.util.*;

public class GenerateNERCorpus {
    private static Set<Integer> lengths = new TreeSet<>();


    // 生成NER格式语料
    private static void run(String srcDir, String targetFilePath) {
        List<File> files = FileUtil.listFiles(srcDir);
        int maxLength = 0;
        List<String> lines = new ArrayList<>();

        for (File file: files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<Span> elements = spaceEvalDoc.getElementsOfNERTask();
            List<List<Span>> sentences = spaceEvalDoc.getSentences();
            for (List<Span> sentence: sentences) {
                for (Span token: sentence) {
                    Span tmpElement = new Span("", "", "", token.start,token.end);
                    int index = Collections.binarySearch(elements, tmpElement);
                    String label = "O";
                    if (index >= 0) {
                        label = "B-" + elements.get(index).label;
                    } else if(index < -1) {
                        index = -index - 2;
                        if (token.end <= elements.get(index).end) {
                            label = "I-" + elements.get(index).label;
                        }
                    }
                    lines.add(token.text + " " + label);
                }
                lines.add("");
                lengths.add(sentence.size());
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
        GenerateNERCorpus.run("data/SpaceEval2015/raw_data/training++",
                "data/SpaceEval2015/processed_data/ner/train.txt");
        GenerateNERCorpus.run("data/SpaceEval2015/raw_data/gold++",
                "data/SpaceEval2015/processed_data/ner/dev_all.txt");
        GenerateNERCorpus.run("data/SpaceEval2015/raw_data/gold++",
                "data/SpaceEval2015/processed_data/ner/test.txt");
        System.out.println(GenerateNERCorpus.lengths);
    }
}
