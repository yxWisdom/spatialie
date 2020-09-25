package edu.nju.ws.spatialie.spaceeval;

import edu.nju.ws.spatialie.data.Sentence;
import edu.nju.ws.spatialie.utils.XmlUtil;
import edu.nju.ws.spatialie.utils.stanfordnlp;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreSentence;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class SegmentCorpus {
    public static void process(String in_path, String out_path) throws IOException {
        stanfordnlp.init();
        Element root = XmlUtil.getRootElement(in_path);

        Element tokens = root.addElement("TOKENS");
        String text = root.elementText("TEXT");
        List<CoreSentence> sentences = stanfordnlp.getCoreSentence(text);
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
        XMLWriter writer = new XMLWriter(new FileOutputStream(new File(out_path)), format);
        writer.write(root);
    }
    public static void  main(String [] args) throws IOException {
        String in_path = "data/SpaceEval2015/raw_data/training/CP/47_N_22_E.xml";
        String out_path = "data/SpaceEval2015/raw_data/training++/CP/47_N_22_E.xml";
        SegmentCorpus.process(in_path, out_path);

        in_path = "data/SpaceEval2015/raw_data/training/CP/47_N_27_E.xml";
        out_path = "data/SpaceEval2015/raw_data/training++/CP/47_N_27_E.xml";
        SegmentCorpus.process(in_path, out_path);
    }
}
