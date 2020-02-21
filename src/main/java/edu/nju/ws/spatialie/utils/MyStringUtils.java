package edu.nju.ws.spatialie.utils;

import java.util.ArrayList;
import java.util.List;

public class MyStringUtils {
//    private static int kmp(String str, String dest,int[] next){//str文本串  dest 模式串
//        for(int i = 0, j = 0; i < str.length(); i++){
//            while(j > 0 && str.charAt(i) != dest.charAt(j)){
//                j = next[j - 1];
//            }
//            if(str.charAt(i) == dest.charAt(j)){
//                j++;
//            }
//            if(j == dest.length()){
//                return i-j+1;
//            }
//        }
//        return -1;
//    }

    //str文本串  dest 模式串
    private static int kmp(String str, String dest,int[] next, int startIndex){
        for(int i = startIndex, j = 0; i < str.length(); i++){
            while(j > 0 && str.charAt(i) != dest.charAt(j)){
                j = next[j - 1];
            }
            if(str.charAt(i) == dest.charAt(j)){
                j++;
            }
            if(j == dest.length()){
                return i-j+1;
            }
        }
        return -1;
    }

    private static int[] kmpNext(String dest){
        int[] next = new int[dest.length()];
        next[0] = 0;
        for(int i = 1,j = 0; i < dest.length(); i++){
            while(j > 0 && dest.charAt(j) != dest.charAt(i)){
                j = next[j - 1];
            }
            if(dest.charAt(i) == dest.charAt(j)){
                j++;
            }
            next[i] = j;
        }
        return next;
    }


    public static int indexOf(String seq, String searchSeq) {
        return indexOf(seq, searchSeq, 0);
    }

    public static int indexOf(String seq, String searchSeq, int startIndex) {
        int[] next = kmpNext(searchSeq);
        return indexOf(seq, searchSeq, startIndex, next);
    }


    private static int indexOf(String seq, String searchSeq, int startIndex, int [] next) {
        if (startIndex >= seq.length())
            return (searchSeq.length() == 0 ? seq.length() : -1);
        if (startIndex < 0)
            startIndex = 0;
        if (seq.length()-startIndex < searchSeq.length())
            return -1;
        if (searchSeq.length() == 0)
            return startIndex;
        return kmp(seq, searchSeq, next, startIndex);
    }



    private static List<Integer> indexOfAll(String seq, String searchSeq, int interval) {
        List<Integer> indexList = new ArrayList<>();
        int[] next = kmpNext(searchSeq);
        int index = indexOf(seq, searchSeq, 0, next);
        if (searchSeq.length() == 0) {
            if (index >= 0)
                indexList.add(index);
        } else {
            while(index >= 0) {
                indexList.add(index);
                index = indexOf(seq, searchSeq, index + interval, next);
            }
        }
        return indexList;
    }

    public static List<Integer> indexOfAllNoOverlap(String seq, String searchSeq) {
        return indexOfAll(seq, searchSeq, searchSeq.length());
    }

    public static List<Integer> indexOfAll(String seq, String searchSeq) {
        return indexOfAll(seq, searchSeq, 1);
    }

    //    public static int lcs(String str, String dest) {
//        int[] next = kmpNext(dest);
//        return kmp(str,dest, next);
//    }
    public static void main(String[] args){
        String a = "abababb";
        String b = "ssdfgasdbababa";
        String c = "ababababa";
//        System.out.println(LCS.lcs(c,a));
        System.out.println(TextSimilarity.getLongestCommonSubsequence(a,b));
        System.out.println(indexOfAllNoOverlap(c,a));
        System.out.println(indexOfAll(c,a));
//        List<BratEntity> list = new ArrayList<>();
//        BratEntity bratEntity = new BratEntity("1","2","3",4,5);
//        list.add(bratEntity);
//        bratEntity.setTag("5");
//        list.add(bratEntity);
//        System.out.println(list);

//        BratAttribute bratAttribute = new BratAttribute("0","1","2","3");
//        bratEntity.addAttribute(bratAttribute);
//        BratEntity bratEntity1 = new BratEntity("11","22","33",4,5);
//        BratAttribute bratAttribute1 = bratEntity.getBratAttributes().get(0);
//        bratAttribute1.setId("111");
//        bratEntity1.addAttribute(bratAttribute1);
//        System.out.println(bratEntity.getBratAttributes());
//        System.out.println(bratEntity1.getBratAttributes());

    }
}
