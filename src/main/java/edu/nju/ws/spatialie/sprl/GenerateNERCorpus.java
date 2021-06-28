package edu.nju.ws.spatialie.sprl;

import edu.nju.ws.spatialie.sprl.*;
import edu.nju.ws.spatialie.spaceeval.Span;
import edu.nju.ws.spatialie.utils.FileUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xyu
 * @version 1.0
 * @date 2021/5/1 4:47
 */
public class GenerateNERCorpus {
    static void run(String inPath, String outPath) {
        List<String> lines = new ArrayList<>();

        SpRLDocument doc = new SpRLDocument(inPath);
        for (Sentence sentence: doc.sentences) {
            List<Span> tokens = sentence.tokens;
            for (Span token : tokens) {
                lines.add(token.text + " " + token.label);
            }
            lines.add("");
        }
        FileUtil.writeFile(outPath, lines);
    }

    public static void main(String [] args) {
        String trainPath = "data/SpRL2012/modified_data/SpRL-2012-Train.xml";
        String goldPath = "data/SpRL2012/modified_data/SpRL-2012-Gold.xml";
        String outputDir = "data/SpRL2012/processed_data/NER/config1/";
        FileUtil.createDir(outputDir);
        run(trainPath, outputDir + "train.txt");
        run(goldPath, outputDir + "dev.txt");
        run(goldPath, outputDir + "test.txt");
    }
}
