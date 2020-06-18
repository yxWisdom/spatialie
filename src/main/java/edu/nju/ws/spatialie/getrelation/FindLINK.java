package edu.nju.ws.spatialie.getrelation;

import edu.nju.ws.spatialie.data.BratDocumentwithList;
import edu.nju.ws.spatialie.data.BratEntity;
import edu.nju.ws.spatialie.data.ParseTree;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;

import java.util.List;

public class FindLINK {
    static boolean isVerb(BratEntity trigger, BratDocumentwithList bratDocument) {
//        System.out.println(bratDocument.getContent());
//        System.out.println(trigger.getText());
        if (bratDocument.getParseTree().getPOS(trigger.getStart()).startsWith("V")) return true;
        String lema = bratDocument.getParseTree().getLemma(trigger.getStart());
        for (String v:WordData.getVerbList()){
            if (lema.equals(v)||lema.contains(v+" ")) return true;
        }
        return false;
    }

    static boolean isPrep(BratEntity trigger, BratDocumentwithList bratDocument) {
        //the snow covering the mountain is shining.
        if (trigger.getText().endsWith("ing")) return true;
        String nextWord = bratDocument.getNextWord(trigger.getEnd());
        for (String prep: WordData.getPrepList()){
            //full of
            if (trigger.getText().toLowerCase().endsWith(prep)) return true;
            if (nextWord.toLowerCase().equals(prep)) return true;
        }
        return false;
    }

    static boolean inSentence(BratDocumentwithList bratDocument, int idx1,int idx2){
        return true;
//        BratEntity e1 = bratDocument.getEntityList().get(idx1);
//        BratEntity e2 = bratDocument.getEntityList().get(idx2);
//        String s = bratDocument.getContent().substring(e1.getEnd(),e2.getStart());
//        String punctuation = ".?!";
//        for (char c:punctuation.toCharArray()){
//            if (s.indexOf(c)!=-1) return false;
//        }
//        return true;
    }

    static boolean inSegment(BratDocumentwithList bratDocument, int idx1,int idx2){
        return true;
//        BratEntity e1 = bratDocument.getEntityList().get(idx1);
//        BratEntity e2 = bratDocument.getEntityList().get(idx2);
//        String s = bratDocument.getContent().substring(e1.getEnd(),e2.getStart());
//        String punctuation = ".?!,()\"\'{}[]*&^%$#@~`";
//        for (char c:punctuation.toCharArray()){
//            if (s.indexOf(c)!=-1) return false;
//        }
//        return true;
    }

    static boolean inSegment_true(BratDocumentwithList bratDocument, int idx1,int idx2){
        try {
            BratEntity e1 = bratDocument.getEntityList().get(idx1);
            BratEntity e2 = bratDocument.getEntityList().get(idx2);
            String s = bratDocument.getContent().substring(e1.getEnd(), e2.getStart());
            String punctuation = "—––--.?!,()\"{}[]*&^%$#@~`-:：";
            for (char c : punctuation.toCharArray()) {
                if (c == '-') {
                    if (s.contains("- ") || s.contains(" -")) return false;
                    else continue;
                }
                if (s.contains(" " + c + " ")) return false;
            }
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public static String getSegment(BratDocumentwithList bratDocument, int idx1, int idx2) {
        try{
            int p1 = bratDocument.getEntityList().get(idx1).getEnd();
            int p2 = bratDocument.getEntityList().get(idx2).getStart();
            return bratDocument.getContent().substring(p1, p2);
        } catch (Exception e){
            return "";
        }
    }

    static String getSegmentOrigin(BratDocumentwithList bratDocument, int idx1, int idx2) {
        int p1 = bratDocument.getEntityList().get(idx1).getEnd();
        int p2 = bratDocument.getEntityList().get(idx2).getStart();
        ParseTree t = bratDocument.getParseTree();
        String res = "";
        for (int p=p1;p<p2;p++){
            String lema = t.getLemma(p);
            if (lema!=null){
                res = res+lema+" ";
            }
        }
        return res;
    }

    public static boolean hasPOS(String pos,BratDocumentwithList bratDocument,int idx1,int idx2){
        int p1 = bratDocument.getEntityList().get(idx1).getEnd();
        int p2 = bratDocument.getEntityList().get(idx2).getStart();
        ParseTree t = bratDocument.getParseTree();
        for (int p=p1;p<p2;p++){
            String POS = t.getPOS(p);
            if (POS!=null&&POS.startsWith(pos)) return true;
        }
        return false;
    }

    public static boolean hasPOSinEntity(String pos,BratDocumentwithList bratDocument,BratEntity e){
        int p2 = e.getEnd();
        int p1 = e.getStart();
        ParseTree t = bratDocument.getParseTree();
        for (int p=p1;p<p2;p++){
            String POS = t.getPOS(p);
            if (POS!=null&&POS.startsWith(pos)) return true;
        }
        return false;
    }

    public static int countVerb(BratDocumentwithList bratDocument,int idx1,int idx2){
        int p1 = bratDocument.getEntityList().get(idx1).getEnd();
        int p2 = bratDocument.getEntityList().get(idx2).getStart();
        ParseTree t = bratDocument.getParseTree();
        int count = 0;
        for (int p=p1;p<p2;p++){
            String POS = t.getPOS(p);
            if (POS!=null&&POS.startsWith("V")&&!t.getLemma(p).equals("be")&&!POS.equals("VBN")&&!POS.equals("VBG")) count++;
        }
        return count;
    }

    public static int countNoun(BratDocumentwithList bratDocument,int idx1,int idx2){
        int p1 = bratDocument.getEntityList().get(idx1).getEnd();
        int p2 = bratDocument.getEntityList().get(idx2).getStart();
        ParseTree t = bratDocument.getParseTree();
        int count = 0;
        boolean the_last_is_noun = false;
        for (int p=p1;p<p2;p++){
            String POS = t.getPOS(p);
            if (POS!=null&&(POS.startsWith("N")||POS.startsWith("PRP"))) {
                // the Chistmas day
                if (!the_last_is_noun) count++;
                the_last_is_noun = true;
            } else {
                if (POS!=null) {
                    if (!POS.startsWith("TO")&&!POS.startsWith("PREP")&&!POS.startsWith("DT")&&!POS.startsWith("IN"))
                        the_last_is_noun = false;
                }
            }
        }
//        if (getSegment(bratDocument,idx1,idx2).contains(" and")) count++;
        return count;
    }

    public static int countNoun_position(BratDocumentwithList bratDocument,int p1,int p2){
//        int p1 = bratDocument.getEntityList().get(idx1).getEnd();
//        int p2 = bratDocument.getEntityList().get(idx2).getStart();
        ParseTree t = bratDocument.getParseTree();
        int count = 0;
        boolean the_last_is_noun = false;
        for (int p=p1;p<p2;p++){
            String POS = t.getPOS(p);
            if (POS!=null&&(POS.startsWith("N")||POS.startsWith("PRP"))) {
                // the Chistmas day
                if (!the_last_is_noun) count++;
                the_last_is_noun = true;
            } else {
                if (POS!=null) {
                    if (!POS.startsWith("TO")&&!POS.startsWith("PREP")&&!POS.startsWith("DT")&&!POS.startsWith("IN")
                            &&!POS.startsWith("VBG")&&!POS.startsWith("VBN")&&!(POS.startsWith("VBD")&&bratDocument.getContent().substring(p,p+6).equals("capped")))
                        the_last_is_noun = false;
                }
            }
        }
        return count;
    }

    static int countWord(String word,BratDocumentwithList bratDocument,int idx1,int idx2) {
        int p1 = bratDocument.getEntityList().get(idx1).getEnd();
        int p2 = bratDocument.getEntityList().get(idx2).getStart();
        ParseTree t = bratDocument.getParseTree();
        int count = 0;
        for (int p=p1;p<p2;p++){
            if (t.getLemma(p)!=null&&t.getLemma(p).equals(word)) count++;
        }
        return count;
    }

    static boolean checkOrderVN(BratDocumentwithList bratDocument,int idx1,int idx2) {
        int p1 = bratDocument.getEntityList().get(idx1).getEnd();
        int p2 = bratDocument.getEntityList().get(idx2).getStart();
        ParseTree t = bratDocument.getParseTree();
        boolean f = false;
        for (int p=p1;p<p2;p++){
            String POS = t.getPOS(p);
            if (POS!=null&&POS.startsWith("V")) f = true;
            if (POS!=null&&(POS.startsWith("N")||POS.startsWith("PRP")))
                if (!f) return false; else f = false;
        }
        return true;
    }

    static BratEntity getEntity(int idx, List<BratEntity> entityList) {
        if (idx==-1)
            return null;
        else
            return entityList.get(idx);
    }

   public static int getNext(int index, BratDocumentwithList bratDocument) {
        if (index==-1) return -1;
        int i = index+1;
        while (i<bratDocument.getEntityList().size()){
            if (bratDocument.getIsCandidate(i)) break;
            i++;
        }
        if (i>=bratDocument.getEntityList().size()) return -1;
        else return i;
    }

    public static int getLast(int index, BratDocumentwithList bratDocument) {
        if (index==-1) return -1;
        int i = index-1;
        while (i>=0){
            if (bratDocument.getIsCandidate(i)) break;
            i--;
        }
        if (i<0) return -1;
        else return i;
    }

    static String getLemma(BratDocumentwithList bratDocument, BratEntity trigger) {
        int p = trigger.getStart();
        String lema = bratDocument.getParseTree().getLemma(p);
        return lema;
    }

    static boolean is2ObjVerb(String lemma) {
        return WordData.getVerb2objList().contains(lemma);
    }

    static String getPOS(String v, BratEntity trigger, BratDocumentwithList bratDocument) {
        int p = trigger.getStart();
        String[] words = trigger.getText().split(" ");
        for (String word:words){
            if (bratDocument.getParseTree().getPOS(p).startsWith(v)) return word;
            p+=word.length()+1;
        }
        return null;
    }

    public static boolean hasNoNV(BratDocumentwithList bratDocument, int idx1, int idx2) {
        int p1 = bratDocument.getEntityList().get(idx1).getEnd();
        int p2 = bratDocument.getEntityList().get(idx2).getStart();
        //TODO:特别加入
        try {
            if (bratDocument.getContent().substring(p1, p2).contains(" and ")) return false;
        } catch (Exception e){
            return false;
        }
        ParseTree t = bratDocument.getParseTree();
        for (int p=p1;p<p2;p++){
            String POS = t.getPOS(p);
            if (POS!=null){
                //TODO:是否保留
                if (POS.startsWith("N")&&!bratDocument.getContent().substring(p,p+3).equals("ft ")) return false;
                if (POS.startsWith("V")&&!POS.equals("VBG")&&!POS.equals("VBN")) return false;
            }
        }
        return true;
    }

    static boolean hasNoVPinEntity(BratDocumentwithList bratDocument, int idx) {
        int p2 = bratDocument.getEntityList().get(idx).getEnd();
        int p1 = bratDocument.getEntityList().get(idx).getStart();
        ParseTree t = bratDocument.getParseTree();
        for (int p=p1;p<p2;p++){
            String POS = t.getPOS(p);
            if (POS!=null){
//                if (POS.startsWith("P")) return false;
                if (POS.startsWith("V")) return false;
            }
        }
        return true;
    }

    static boolean endwithPrep(String temp_s) {
        for (String s:WordData.getPrepList()){
            if (temp_s.contains(s)) {
                int i = temp_s.lastIndexOf(s)+s.length()+1;
                if (i>=temp_s.length()) return true;
                Annotation annotation = new Annotation(temp_s.substring(i));
                if (!temp_s.substring(i).equals(" ")) {
                    NLPUtil.pipeline.annotate(annotation);
                    if (annotation.get(CoreAnnotations.SentencesAnnotation.class) != null && annotation.get(CoreAnnotations.SentencesAnnotation.class).get(0) != null) {
                        for (CoreLabel token : annotation.get(CoreAnnotations.SentencesAnnotation.class).get(0).get(CoreAnnotations.TokensAnnotation.class)) {
                            String POS = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                            if ((POS.startsWith("N")||POS.startsWith("PRP"))) return false;
                            if (POS.startsWith("V")) return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }
}
