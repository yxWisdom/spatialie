package edu.nju.ws.spatialie.msprl;

import edu.nju.ws.spatialie.spaceeval.Span;
import edu.nju.ws.spatialie.utils.FileUtil;

import java.util.*;

/**
 * @author xyu
 * @version 1.0
 * @date 2021/5/1 4:47
 */
public class GenerateNERCorpus {
    static void run(String inPath, String outPath) {
        List<String> lines = new ArrayList<>();

        MSpRLDoc doc = new MSpRLDoc(inPath);
        for (SpRLSentence sentence: doc.sentences) {
            List<Span> tokens = sentence.tokens;
            for (Span token : tokens) {
                lines.add(token.text + " " + token.label);
            }
            lines.add("");
        }
        FileUtil.writeFile(outPath, lines);
    }

    public static void main(String [] args) {
        String trainPath = "data/mSpRL2017/clean_data/Sprl2017_train.xml";
        String goldPath = "data/mSpRL2017/clean_data/Sprl2017_gold.xml";
        String outputDir = "data/mSpRL2017/processed_data/NER/config1/";
        FileUtil.createDir(outputDir);
        run(trainPath, outputDir + "train.txt");
        run(goldPath, outputDir + "dev.txt");
        run(goldPath, outputDir + "test.txt");
    }

}
