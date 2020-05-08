package edu.nju.ws.spatialie.data;

import com.google.common.collect.Multimap;
import edu.nju.ws.spatialie.getrelation.FindLINK;
import edu.nju.ws.spatialie.getrelation.FindOTLINK;
import edu.nju.ws.spatialie.getrelation.JudgeEntity;
import edu.nju.ws.spatialie.getrelation.NLPUtil;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.pipeline.Annotation;
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
        for(CoreMap word_temp: sentences) {
            for (CoreLabel token: word_temp.get(CoreAnnotations.TokensAnnotation.class)) {
                String lema = token.get(CoreAnnotations.LemmaAnnotation.class);  // 获取对应上面word的词元信息，即我所需要的词形还原后的单词
                String POS = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                lemma.put(token.beginPosition(),lema);
                pos.put(token.beginPosition(),POS);
            }
        }

    }

    public List<IndexedWord> getPossibleSubj(int index,boolean allow_acl){
        IndexedWord node = graph.getNodeByIndex(index);
        return DFS_Graph(node,allow_acl);
    }

    public Multimap<String, String> getSemanticCompany(BratDocumentwithList bratDocument){
        int pos = 0;
        String[] words = bratDocument.getContent().split(" ");
        for (int i = 0;i<words.length;i++){
            try {
                for (SemanticGraphEdge edge : bratDocument.getParseTree().graph.outgoingEdgeList(bratDocument.getParseTree().graph.getNodeByIndex(i + 1))) {
                    IndexedWord target = edge.getTarget();
                    if (edge.getRelation().getShortName().equals("conj")) {
                        int b1 = pos;
                        int e1 = pos + words[i].length();
                        int b2 = target.beginPosition();
                        int e2 = target.endPosition();
                        int idx1 = 0;
                        for (BratEntity entity1 : bratDocument.getEntityList()) {
                            if (entity1.getStart() <= b1 && entity1.getEnd() >= e1) {
                                int idx = 0;
                                for (BratEntity entity2 : bratDocument.getEntityList()) {
                                    if (entity2.getStart() <= b2 && entity2.getEnd() >= e2) {
                                        if (JudgeEntity.canbeLandmark(entity1) && JudgeEntity.canbeLandmark(entity2)
                                                && !FindOTLINK.hasPOS("N", bratDocument, idx1, idx) && !FindOTLINK.hasPOS("V", bratDocument, idx1, idx) && !FindOTLINK.hasPOS("IN", bratDocument, idx1, idx)
                                                && (FindOTLINK.hasPOSinEntity("N", bratDocument, entity1) || FindOTLINK.hasPOSinEntity("P", bratDocument, entity1)) && (FindOTLINK.hasPOSinEntity("N", bratDocument, entity2) || FindOTLINK.hasPOSinEntity("P", bratDocument, entity2))) {
                                            bratDocument.noCandidate(idx);
                                            bratDocument.companyMap.put(entity1.getId(), entity2.getId());
                                            entity1.setEnd(entity2.getEnd());
                                        }
                                    }
                                    idx++;
                                }
                            }
                            idx1++;
                        }
                    }
                }
            } catch (Exception e){

            }
            pos+=words[i].length()+1;
        }
        return bratDocument.companyMap;
    }

    private List<IndexedWord> DFS_Graph(IndexedWord node, boolean allow_acl) {
        List<IndexedWord> res=new ArrayList<>();

        for (SemanticGraphEdge edge:graph.getOutEdgesSorted(node)){
            IndexedWord target = edge.getTarget();
            String relation = edge.getRelation().getShortName();
            if (relation.equals("nsubj")||relation.equals("nsubjpass")){
                res.add(target);
            }
        }

        for (SemanticGraphEdge edge:graph.getIncomingEdgesSorted(node)){
            IndexedWord next = edge.getSource();
            String relation = edge.getRelation().getShortName();
            if (allow_acl&&relation.equals("acl:relcl"))
                res.add(next);
        }

        if (res.size()>0) return res;

        for (SemanticGraphEdge edge:graph.getIncomingEdgesSorted(node)){
            IndexedWord next = edge.getSource();
            String relation = edge.getRelation().getShortName();
//            if (allow_acl&&relation.equals("acl:relcl"))
//                res.add(next);
//            else
                res.addAll(DFS_Graph(next, allow_acl));
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
