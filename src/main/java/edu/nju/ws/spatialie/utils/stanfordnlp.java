package edu.nju.ws.spatialie.utils;

import edu.nju.ws.spatialie.data.Sentence;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ie.machinereading.structure.EntityMention;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.CoreAnnotations.NormalizedNamedEntityTagAnnotation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;


public class stanfordnlp {
    private static StanfordCoreNLP pipeline=null;
    private static CoreDocument document;

    public static void init(String mode) {
        Properties props = new Properties();
//        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote");
//        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
//        props.setProperty("coref.algorithm", "neural");
        if (mode.toLowerCase().equals("en")) {
            props.setProperty("annotators", "tokenize, ssplit");
        } else {
            try {
                props.load(IOUtils.readerFromString("StanfordCoreNLP-chinese.properties"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            props.setProperty("ssplit.boundaryTokenRegex", "[。\n\\s ]|[!?！？ ]+");
            props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        }
        pipeline = new StanfordCoreNLP(props);
    }

    public static void init() {
        init("en");
    }

    public static List<CoreLabel> getCoreLabel(String content) {
        CoreDocument doc = new CoreDocument(content);
        pipeline.annotate(doc);
        return doc.tokens();
    }

    public static List<CoreSentence> getCoreSentence(String content) {
        CoreDocument doc = new CoreDocument(content);
        pipeline.annotate(doc);
        return doc.sentences();
    }

    public static List<Sentence> getSentences(String text){
        if (pipeline == null) {
            init();
        }
        CoreDocument document=new CoreDocument(text);
        pipeline.annotate(document);
        List<Sentence> sentenceList = new ArrayList<>();
        List<CoreSentence> coreSentenceList = document.sentences();
        for (CoreSentence coreSentence: coreSentenceList) {
            List<String> tokens = coreSentence.tokens().stream().map(CoreLabel::value).collect(Collectors.toList());
            List<String> labels = coreSentence.nerTags();
            List<String> mentions = new ArrayList<>();
            List<String> nerTags = new ArrayList<>();
            List<String> norms = new ArrayList<>();
            int idx = 0;
            for (CoreEntityMention em: coreSentence.entityMentions()) {
                List<CoreLabel>  ts = em.tokens();
                int start = ts.get(0).index()-1;
                while (idx < start) {
                    mentions.add(tokens.get(idx));
                    nerTags.add("O");
                    norms.add("");
                    idx++;
                }
                mentions.add(String.join("",  ts.stream().map(CoreLabel::word).collect(Collectors.toList())));
                nerTags.add(em.entityType());
                if ((em.entityType().equals("DATE")||em.entityType().equals("TIME"))  &&
                        em.coreMap().containsKey(NormalizedNamedEntityTagAnnotation.class)) {
                    norms.add(em.coreMap().get(NormalizedNamedEntityTagAnnotation.class));
                }else {
                    norms.add("");
                }
                idx+=ts.size();
            }
            while (idx < tokens.size()) {
                mentions.add(tokens.get(idx));
                nerTags.add("O");
                norms.add("");
                idx++;
            }
            Sentence sentence = new Sentence(coreSentence.text(), tokens,  labels, mentions, nerTags, norms);
            sentenceList.add(sentence);
        }
        return sentenceList;
    }



    public static String text = "Joe Smith was born in California. " +
            "In 2017, he went to Paris, France in the summer. " +
            "His flight left at 3:00pm on July 10th, 2017. " +
            "After eating some escargot for the first time, Joe said, \"That was delicious!\" " +
            "He sent a postcard to his sister Jane Smith. " +
            "After hearing about Joe's trip, Jane decided she might go to France one day.";

    public static void main(String[] args) {
        List<Sentence> sentences = stanfordnlp.getSentences("2018-04-12 00:13:01 3点30发生了一起重要得交通事故");

        System.out.println(sentences.toString());
        sentences.forEach(x->System.out.println(x.getTokens().toString()));

        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote");
        // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
        props.setProperty("coref.algorithm", "neural");

        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create a document object
        CoreDocument document = new CoreDocument(text);
        // annnotate the document
        pipeline.annotate(document);
        // examples

        // 10th token of the document
        CoreLabel token = document.tokens().get(10);
        System.out.println("Example: token");
        System.out.println(token);
        System.out.println();

        // text of the first sentence
        String sentenceText = document.sentences().get(0).text();
        System.out.println("Example: sentence");
        System.out.println(sentenceText);
        System.out.println();

        // second sentence
        CoreSentence sentence = document.sentences().get(1);

        // list of the part-of-speech tags for the second sentence
        List<String> posTags = sentence.posTags();
        System.out.println("Example: pos tags");
        System.out.println(posTags);
        System.out.println();

        // list of the ner tags for the second sentence
        List<String> nerTags = sentence.nerTags();
        System.out.println("Example: ner tags");
        System.out.println(nerTags);
        System.out.println();

        // constituency parse for the second sentence
        Tree constituencyParse = sentence.constituencyParse();
        System.out.println("Example: constituency parse");
        System.out.println(constituencyParse);
        System.out.println();

        // dependency parse for the second sentence
        SemanticGraph dependencyParse = sentence.dependencyParse();
        System.out.println("Example: dependency parse");
        System.out.println(dependencyParse);
        System.out.println();

        // kbp relations found in fifth sentence
        List<RelationTriple> relations =
                document.sentences().get(4).relations();
        System.out.println("Example: relation");
        System.out.println(relations.get(0));
        System.out.println();

        // entity mentions in the second sentence
        List<CoreEntityMention> entityMentions = sentence.entityMentions();
        System.out.println("Example: entity mentions");
        System.out.println(entityMentions);
        System.out.println();

        // coreference between entity mentions
        CoreEntityMention originalEntityMention = document.sentences().get(3).entityMentions().get(1);
        System.out.println("Example: original entity mention");
        System.out.println(originalEntityMention);
        System.out.println("Example: canonical entity mention");
        System.out.println(originalEntityMention.canonicalEntityMention().get());
        System.out.println();

        // get document wide coref info
        Map<Integer, CorefChain> corefChains = document.corefChains();
        System.out.println("Example: coref chains for document");
        System.out.println(corefChains);
        System.out.println();

        // get quotes in document
        List<CoreQuote> quotes = document.quotes();
        CoreQuote quote = quotes.get(0);
        System.out.println("Example: quote");
        System.out.println(quote);
        System.out.println();

        // original speaker of quote
        // note that quote.speaker() returns an Optional
        System.out.println("Example: original speaker of quote");
        System.out.println(quote.speaker().get());
        System.out.println();

        // canonical speaker of quote
        System.out.println("Example: canonical speaker of quote");
        System.out.println(quote.canonicalSpeaker().get());
        System.out.println();

    }
}
