package edu.nju.ws.spatialie.getrelation;

import edu.nju.ws.spatialie.Link.MLINK;
import edu.nju.ws.spatialie.data.BratDocumentwithList;
import edu.nju.ws.spatialie.data.BratEntity;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.trees.Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

public class FindMLINK extends FindLINK {

    static Map<BratEntity, MLINK> entityEventMap = new HashMap<>();

    public static MLINK findMLINK(BratDocumentwithList bratDocument, List<MLINK> mlinklist, int level) {
        if (level != 1) return null;
        for (MLINK mlink : mlinklist) {
            if (isMLINK(mlink, bratDocument, level) != null) {
                return mlink;
            }
        }
        return null;
    }

    private static MLINK isMLINK(MLINK mlink, BratDocumentwithList bratDocument, int level) {
        if (mlink.isIscompleted()) return null;
        List<BratEntity> entityList = bratDocument.getEntityList();
        int index = mlink.getTrigger();
        BratEntity trigger = entityList.get(index);
        int idx_next1 = getNext(index, bratDocument);
        BratEntity next1 = getEntity(idx_next1, entityList);


//        if (trigger.getText().contains("drive")) {
//            System.out.println(233);
//        }
        String coreword = "";
//        if (getPOS("V", trigger, bratDocument)!=null){
            String lemma = getLemma(bratDocument, trigger);
            if (!is2ObjVerb(lemma)) {
                int p = trigger.getStart();
                String[] words = trigger.getText().split(" ");
                for (String word:words){
                    if (bratDocument.getParseTree().getPOS(p).startsWith("V")){break;}
                    p+=word.length()+1;
                }
                int idx = 0;
                for (int tp = 0;tp<p;tp=bratDocument.getContent().indexOf(' ',tp+1)+1){
                    idx++;
                }
                idx++;
                List<IndexedWord> subj = bratDocument.getParseTree().getPossibleSubj(idx);
                if (subj.size()>0){
                    for (IndexedWord word :subj){
                        for (int t = 1;t<=50;t++){
                            int i = index;
                            //保证顺序是index-1 index+1 index-2 index+2...
                            if (t%2!=0){
                                i = max(0,i-t/2-1);
                            } else {
                                i = min(entityList.size()-1,i+t/2);
                            }
                            BratEntity e = entityList.get(i);
                            if (bratDocument.getIsCandidate(i) && JudgeEntity.canbeMover_NotStrict(e)) {
                                if (e.getStart()<=word.beginPosition()&&e.getEnd()>=word.endPosition()){
                                    mlink.setMover(i);
                                    mlink.Complete();
                                    mlink.setRule_id("MT1");
                                    return mlink;
                                }
                                if (word.toString().contains("that")||word.toString().contains("who")||word.toString().contains("which")){
                                    if (e.getEnd()+1==word.beginPosition()){
                                        mlink.setMover(i);
                                        mlink.Complete();
                                        mlink.setRule_id("MT1");
                                        return mlink;
                                    }
                                }
                            }
                            if (max(0,i-t/2-1)==0&&min(entityList.size()-1,i+t/2)==entityList.size()-1) break;
                        }
                    }
                }
            }
//        }


        if (getPOS("N", trigger, bratDocument) != null) {
            // take a walk
            //our trip TODO:存疑
            coreword = getPOS("N", trigger, bratDocument);
        } else {
            coreword = getPOS("V", trigger, bratDocument);
        }
        if (coreword != null) {
            List<Tree> mover_node = new ArrayList<>();
            mover_node.add(bratDocument.getParseTree().getNode(coreword));
            int idx =0;
            while(true) {
                mover_node = getMover(mover_node.get(0), bratDocument.getParseTree().getRoot());
                if (idx==3) break;
                if (mover_node .size()>0) {
                    for (int k = 0;k<mover_node.size();k++) {
                        Tree mover_node_itr = mover_node.get(k);
                        String mover = concatString(mover_node_itr.yieldWords());
                        int p1 = mover_node_itr.yieldWords().get(0).beginPosition();
                        int p2 = mover_node_itr.yieldWords().get(mover_node_itr.yieldWords().size()-1).endPosition();
                        for (int t = 1;t<=20;t++){
                            int i = index;
                            //保证顺序是index-1 index+1 index-2 index+2...
                            if (t%2!=0){
                                i = max(0,i-t/2-1);
                            } else {
                                i = min(entityList.size()-1,i+t/2);
                            }
                            BratEntity e = entityList.get(i);
                            if (bratDocument.getIsCandidate(i) && JudgeEntity.canbeMover(e)) {
                                if (e.getText().contains(mover) || mover.contains(e.getText())) {
                                    if (!(e.getStart()>p2||e.getEnd()<p1)) {
                                        mlink.setMover(i);
                                        mlink.Complete();
//                                    bratDocument.noCandidate(index);
//                                    bratDocument.noCandidate(i);
                                        mlink.setRule_id("MT2");
                                        return mlink;
                                    }
                                }
                            }
                            if (max(0,i-t/2-1)==0&&min(entityList.size()-1,i+t/2)==entityList.size()-1) break;
                        }
                    }
                } else {
                    break;
                }
                idx++;
            }
        }

//        if (getPOS("N", trigger, bratDocument) != null) {
//            // take a walk
//            //our trip TODO:存疑
//            String verb = getPOS("N", trigger, bratDocument);
//            if (verb != null) {
//                Tree node = bratDocument.getParseTree().getNode(verb);
//                Tree mover_node = getMover(node, bratDocument.getParseTree().getRoot());
//                if (mover_node != null) {
//                    String mover = concatString(mover_node.yieldWords());
//                    for (int i = max(0, index - 5); i < min(entityList.size(), index + 6); i++) {
//                        BratEntity e = entityList.get(i);
//                        if (bratDocument.getIsCandidate(i) && JudgeEntity.canbeMover(e)) {
//                            if (e.getText().contains(mover) || mover.contains(e.getText())) {
//                                mlink.setMover(i);
//                                mlink.Complete();
////                                bratDocument.noCandidate(index);
////                                bratDocument.noCandidate(i);
//                                return mlink;
//                            }
//                        }
//                    }
//                }
//            }
//        } else {
//            String lemma = getLemma(bratDocument, trigger);
//            if (is2ObjVerb(lemma)) {
//                //bring us to [PLACE]
//                if (next1 != null) {
//                    if (inSegment(bratDocument, index, idx_next1)) {
//                        if (JudgeEntity.canbeMover(next1)) {
//                            if (!hasPOS("N", bratDocument, index, idx_next1) && !hasPOS("V", bratDocument, index, idx_next1)) {
//                                mlink.setMover(idx_next1);
//                                mlink.Complete();
////                                bratDocument.noCandidate(index);
////                                bratDocument.noCandidate(idx_next1);
//                                return mlink;
//                            }
//                        }
//                    }
//                }
//            }
//            String verb = getPOS("V", trigger, bratDocument);
//            if (verb != null) {
//                Tree node = bratDocument.getParseTree().getNode(verb);
//                Tree mover_node = getMover(node, bratDocument.getParseTree().getRoot());
//                if (mover_node != null) {
//                    String mover = concatString(mover_node.yieldWords());
//                    for (int i = max(0, index - 5); i < min(entityList.size(), index + 6); i++) {
//                        BratEntity e = entityList.get(i);
//                        if (bratDocument.getIsCandidate(i) && JudgeEntity.canbeMover(e)) {
//                            if (e.getText().contains(mover) || mover.contains(e.getText())) {
//                                mlink.setMover(i);
//                                mlink.Complete();
////                                    bratDocument.noCandidate(index);
////                                    bratDocument.noCandidate(i);
//                                return mlink;
//                            }
//                        }
//                    }
//                }
//            }
//
//        }
        return null;
    }

    private static String concatString(ArrayList<Word> yieldWords) {
        String res = "";
        for (Word w : yieldWords) {
            res = res+" " + w.word();
        }
        return res.substring(1);
    }

    private static List<Tree> getMover(Tree node, Tree root) {
        node = node.ancestor(1,root);
        List<Tree> res = new ArrayList<>();
        while (!node.equals(root)) {
            List<Tree> siblings = node.siblings(root);
            for (Tree t : siblings) {
                String label = t.label().value();
                if (label.startsWith("PRP")||label.startsWith("N")){
//                if (label.startsWith("N") && hasPRP(t)) {
                    res.add(t);
                }
            }
            if (res.size()>0) return res;
            node = node.ancestor(1, root);
        }
        return res;
    }

    private static boolean hasPRP(Tree t) {
        boolean has = t.label().value().equals("PRP");
        for (Tree subtree : t.getChildrenAsList()) {
            has = has | hasPRP(subtree);
        }
        return has;
    }


}
