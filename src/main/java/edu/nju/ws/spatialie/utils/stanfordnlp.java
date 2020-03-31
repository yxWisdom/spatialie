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
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.CoreAnnotations.NormalizedNamedEntityTagAnnotation;

import java.io.File;
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

//    public static void init() {
//        init("en");
//    }

    public static List<CoreLabel> getCoreLabel(String content) {
        CoreDocument doc = new CoreDocument(content);
        pipeline.annotate(doc);
        return doc.tokens();
    }

    public static List<Sentence> getSentences(String text){
        if (pipeline == null) {
            init("ch");
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



    public static String text = "Joe Smith was born in California. " ;
//            +
//            "In 2017, he went to Paris, France in the summer. " +
//            "His flight left at 3:00pm on July 10th, 2017. " +
//            "After eating some escargot for the first time, Joe said, \"That was delicious!\" " +
//            "He sent a postcard to his sister Jane Smith. " +
//            "After hearing about Joe's trip, Jane decided she might go to France one day.";



    static void generateNERCorpusChinese(String path){
        String content = FileUtil.readFile(path);
        String token = "";
        for (int i = 0;i<content.length();i++){
            char c = content.charAt(i);
            if (c==' ') continue;
            if (c>='a'&&c<='z'||c>='A'&&c<='Z'){
                while(c>='a'&&c<='z'||c>='A'&&c<='Z'||c>='0'&&c<='9'){
                    token = token+c;
                    i ++;
                    if (i==content.length()) break;
                    c = content.charAt(i);
                }
                i--;
            } else {
                token = token+c;
            }
            token = token+" O\n";
            if ("。！？".contains(c+"")) token = token+"\n";
        }
        FileUtil.writeFile("res.txt",token);
    }

    public static void main(String[] args) {

//        generateNERCorpusChinese("test.txt");

//        String s = "2 0 1 9 年 3 月 4 日 至 2 4 日 在 菲 律 宾 马 尼 拉 北 部 的 帕 拉 延 市 麦 格 塞 塞 堡 基 地 和 新 埃 西 哈 省 部 分 地 区 举 行";
//        System.out.println(s.split(" ").length);
//        String ttext = "美菲“萨马萨马”海上训练活动(Maritime raining Activity Sama Sama)是美菲年度海上联合训练演习， 2019年日海上自卫队首次参加该演 习。这是该演习第3次举行，时间从10月14日持续至21日，演习地点主要集中在菲律宾 普林塞萨港及周边海域。美军参演兵力包括美海军濒海战斗舰蒙哥马利号(USS Montgomery， LCS-8)、两栖船坞登陆舰杰曼敦号、远征快速运输船米利诺克特号(USNS Millinocket ，T-EPF3)、打捞船打捞者号(USNS Salvor，ARS-52)、海警船斯特拉顿号(USCG Stratton，VMSL-752)、 1架P-8A 反潜巡逻机。演习分为岸基和海上演习两个阶 段。岸基阶段主要进行主题专家交流会，议题涵盖海域态势感知、部队防护、医疗、人道主义援助和减灾、爆炸物处置、潜水和打捞行动、工程、航空和登临搜捕行动等。海上阶段包括登临、搜捕演练、分离战术、搜救演练、直升机甲板着陆资格、防空和水面作战追踪等。";
//        String res = "";
//        for (char c:ttext.toCharArray()){
//            res = res+c+" O\n";
//            if (c=='。') res = res +"\n";
//        }
//        FileUtil.writeFile("temp.txt",res,true);



        List<Sentence> sentences = stanfordnlp.getSentences("从3月18日开始进行为期10天的演习。");
        System.out.println(sentences.toString());
        sentences.forEach(x->System.out.println(x.getTokens().toString()));

//        // set up pipeline properties
//        Properties props = new Properties();
//        // set the list of annotators to run
//        props.setProperty("annotators", "tokenize,ssplit,pos,parse");
//        // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
//        props.setProperty("coref.algorithm", "neural");
//
//        // build pipeline
//        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
//        // create a document object
//        Annotation document = new Annotation(text);
//        // annnotate the document
//        pipeline.annotate(document);
//        // examples
//
//        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
//        Tree tree = sentences.get(0).get(TreeCoreAnnotations.TreeAnnotation.class);
//        System.out.println(tree.getChildrenAsList().get(0).getChildrenAsList().get(0));
//        for (Tree subt:tree.getChildrenAsList().get(0).getChildrenAsList()){
//            System.out.println(subt);
//        }
//        Tree tree = document.sentences(CoreAnnotations.SentencesAnnotation.class).get(0).get(TreeCoreAnnotations.TreeAnnotation.class);
//        // 10th token of the document
//        CoreLabel token = document.tokens().get(10);
//        System.out.println("Example: token");
//        System.out.println(token);
//        System.out.println();
//
//        // text of the first sentence
//        String sentenceText = document.sentences().get(0).text();
//        System.out.println("Example: sentence");
//        System.out.println(sentenceText);
//        System.out.println();
//
//        // second sentence
//        CoreSentence sentence = document.sentences().get(1);
//
//        // list of the part-of-speech tags for the second sentence
//        List<String> posTags = sentence.posTags();
//        System.out.println("Example: pos tags");
//        System.out.println(posTags);
//        System.out.println();
//
//        // list of the ner tags for the second sentence
//        List<String> nerTags = sentence.nerTags();
//        System.out.println("Example: ner tags");
//        System.out.println(nerTags);
//        System.out.println();
//
//        // constituency parse for the second sentence
//        Tree constituencyParse = sentence.constituencyParse();
//        System.out.println("Example: constituency parse");
//        System.out.println(constituencyParse);
//        System.out.println();
//
//        // dependency parse for the second sentence
//        SemanticGraph dependencyParse = sentence.dependencyParse();
//        System.out.println("Example: dependency parse");
//        System.out.println(dependencyParse);
//        System.out.println();
//
//        // kbp relations found in fifth sentence
//        List<RelationTriple> relations =
//                document.sentences().get(4).relations();
//        System.out.println("Example: relation");
//        System.out.println(relations.get(0));
//        System.out.println();
//
//        // entity mentions in the second sentence
//        List<CoreEntityMention> entityMentions = sentence.entityMentions();
//        System.out.println("Example: entity mentions");
//        System.out.println(entityMentions);
//        System.out.println();
//
//        // coreference between entity mentions
//        CoreEntityMention originalEntityMention = document.sentences().get(3).entityMentions().get(1);
//        System.out.println("Example: original entity mention");
//        System.out.println(originalEntityMention);
//        System.out.println("Example: canonical entity mention");
//        System.out.println(originalEntityMention.canonicalEntityMention().get());
//        System.out.println();
//
//        // get document wide coref info
//        Map<Integer, CorefChain> corefChains = document.corefChains();
//        System.out.println("Example: coref chains for document");
//        System.out.println(corefChains);
//        System.out.println();
//
//        // get quotes in document
//        List<CoreQuote> quotes = document.quotes();
//        CoreQuote quote = quotes.get(0);
//        System.out.println("Example: quote");
//        System.out.println(quote);
//        System.out.println();
//
//        // original speaker of quote
//        // note that quote.speaker() returns an Optional
//        System.out.println("Example: original speaker of quote");
//        System.out.println(quote.speaker().get());
//        System.out.println();
//
//        // canonical speaker of quote
//        System.out.println("Example: canonical speaker of quote");
//        System.out.println(quote.canonicalSpeaker().get());
//        System.out.println();

    }
}
