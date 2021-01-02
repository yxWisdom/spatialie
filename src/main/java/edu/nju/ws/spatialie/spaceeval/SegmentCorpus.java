package edu.nju.ws.spatialie.spaceeval;

import edu.nju.ws.spatialie.utils.XmlUtil;
import edu.nju.ws.spatialie.utils.StandfordNLPUtil;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreSentence;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class SegmentCorpus {
    public static void process(String inPath, String outPath) throws IOException {
        StandfordNLPUtil.init();
        Element root = XmlUtil.getRootElement(inPath);
        Element tokens = root.addElement("TOKENS");
        String text = root.elementText("TEXT");
        List<CoreSentence> sentences = StandfordNLPUtil.getCoreSentence(text);
        for (CoreSentence sentence: sentences) {
            Element sen = tokens.addElement("s");
            for (CoreLabel coreLabel: sentence.tokens()) {
                Element  token = sen.addElement("lex");
                int begin = coreLabel.beginPosition();
                int end = coreLabel.endPosition();
                token.addAttribute("begin", String.valueOf(begin));
                token.addAttribute("end", String.valueOf(end));
                token.addText(coreLabel.value());
            }
        }
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer = new XMLWriter(new FileOutputStream(new File(outPath)), format);
        writer.write(root);
    }
    public static void  main(String [] args) throws IOException {
        String inPath = "data/SpaceEval2015/raw_data/training/CP/47_N_22_E.xml";
        String outPath = "data/SpaceEval2015/raw_data/training++/CP/47_N_22_E.xml";
        SegmentCorpus.process(inPath, outPath);

        inPath = "data/SpaceEval2015/raw_data/training/CP/47_N_27_E.xml";
        outPath = "data/SpaceEval2015/raw_data/training++/CP/47_N_27_E.xml";
        SegmentCorpus.process(inPath, outPath);
        SegmentCorpus.process(inPath, outPath);
    }
}
