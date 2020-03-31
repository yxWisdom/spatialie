package edu.nju.ws.spatialie.data;

import edu.nju.ws.spatialie.getrelation.NLPUtil;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

public class ParseTree {
    Tree tree;
    Map<String,Tree> nodeMap;
    String text;
    Map<Integer,String> lemma = new HashMap<>();
    Map<Integer,String> pos = new HashMap<>();
    SemanticGraph graph;

    public ParseTree(String text) {
        Annotation annotation = new Annotation(text);
        this.text = text;
        NLPUtil.pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        tree = sentences.get(0).get(TreeCoreAnnotations.TreeAnnotation.class);
        graph = sentences.get(0).get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
        nodeMap = new HashMap<>();
        DFS(tree);
//        graph.getNodeByIndex()
//        for (SemanticGraphEdge edge:graph.edgeListSorted()){
//            IndexedWord target = edge.getTarget();
//            IndexedWord source = edge.getSource();
//            String relation = edge.getRelation().getShortName();
//        }
        for(CoreMap word_temp: sentences) {
            for (CoreLabel token: word_temp.get(CoreAnnotations.TokensAnnotation.class)) {
                String lema = token.get(CoreAnnotations.LemmaAnnotation.class);  // 获取对应上面word的词元信息，即我所需要的词形还原后的单词
                String POS = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                lemma.put(token.beginPosition(),lema);
                pos.put(token.beginPosition(),POS);
            }
        }

    }

    public List<IndexedWord> getPossibleSubj(int index){
        IndexedWord node = graph.getNodeByIndex(index);
        return DFS_Graph(node);
    }

    private List<IndexedWord> DFS_Graph(IndexedWord node) {
        List<IndexedWord> res=new ArrayList<>();

        for (SemanticGraphEdge edge:graph.getOutEdgesSorted(node)){
            IndexedWord target = edge.getTarget();
            String relation = edge.getRelation().getShortName();
            if (relation.equals("nsubj")||relation.equals("nsubjpass")){
                res.add(target);
            }
        }
        if (res.size()>0) return res;

        for (SemanticGraphEdge edge:graph.getIncomingEdgesSorted(node)){
            IndexedWord next = edge.getSource();
            String relation = edge.getRelation().getShortName();
//            if (relation.equals("acl:relcl"))
//                res.add(next);
//            else
                res.addAll(DFS_Graph(next));
        }
        return res;
    }

    public String getLemma(int pos){
        return lemma.get(pos);
    }

    public String getPOS(int position){
        return pos.get(position);
    }

    private void DFS(Tree tree) {
        String text = trimString(tree.yieldWords());
        nodeMap.put(text,tree);
        for (Tree subtree:tree.getChildrenAsList()){
            DFS(subtree);
        }
    }

    private String trimString(ArrayList<Word> wordArrayList) {
        int p1 = wordArrayList.get(0).beginPosition();
        int p2 = wordArrayList.get(wordArrayList.size()-1).endPosition();
        return text.substring(p1,p2);
    }

    public Tree getNode(String text){
        return nodeMap.get(text);
    }

    public Tree getRoot(){
        return tree;
    }
}
