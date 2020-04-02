package edu.nju.ws.spatialie.getrelation;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.Properties;

public class NLPUtil {

    public static StanfordCoreNLP pipeline;
    public static Properties props = new Properties();

    public NLPUtil() {
    }

    public static void init(){
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse");
        // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
//        props.setProperty("coref.algorithm", "neural");
        pipeline = new StanfordCoreNLP(props);
    }
}
