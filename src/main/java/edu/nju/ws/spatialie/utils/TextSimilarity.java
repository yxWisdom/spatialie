package edu.nju.ws.spatialie.utils;

import org.apache.commons.text.similarity.*;

import java.util.Locale;

public class TextSimilarity {
    private static JaccardSimilarity jaccardSimilarity = null;
    private static LongestCommonSubsequence longestCommonSubsequence=null;
    private static LongestCommonSubsequenceDistance longestCommonSubsequenceDistance=null;
    private static LevenshteinDistance levenshteinDistance=null;
    private static FuzzyScore fuzzyScore= null;


    private static void init(){
        jaccardSimilarity = new JaccardSimilarity();
        longestCommonSubsequence = new LongestCommonSubsequence();
        fuzzyScore = new FuzzyScore(Locale.CHINESE);
        longestCommonSubsequenceDistance = new LongestCommonSubsequenceDistance();
        levenshteinDistance = new LevenshteinDistance();
    }

    public static double getJaccardSimilarity(String source, String target) {
        if (jaccardSimilarity == null) {
            init();
        }
        return jaccardSimilarity.apply(source, target);
    }
    public static int getLongestCommonSubsequence(String source, String target) {
        if (longestCommonSubsequence == null) {
            init();
        }
        return longestCommonSubsequence.apply(source, target);
    }

    private static int getCommonStrLength(String str1, String str2) {
        int len1 = str1.length();
        int len2 = str2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];
        for (int i = 0; i <= len1; i++) {
            for (int j = 0; j <= len2; j++) {
                dp[i][j] = 0;
            }
        }
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = 0;
                }
            }
        }
        int max = 0;
        for (int i = 0; i <= len1; i++) {
            for (int j = 0; j <= len2; j++) {
                if (dp[i][j] > max) {
                    max = dp[i][j];
                }
            }
        }
        return max;
    }



    public static  void main(String [] args) {
        SimilarityScore similarityScore = null;
        String a = "俄国防部网站遭到密集黑客攻击 锁定三大来源";
        String b = "俄国防部网站遭到密集黑客攻击";
        String c = "俄防部网遭到密黑客击";
        String d = "随后5名自杀式袭击者引爆了自己，并与领事馆守卫交火。至少两名武装分子在交火中被打死，领事馆两名阿富汗守卫和一名平民遇害、约20名平民受伤。据美国驻喀布尔大使馆消息，在领事馆工作的外国人没有出现伤亡情况。";
        String e = "新华社快讯：据外电报道，伊拉克首都巴格达市中心15日发生两起自杀式爆炸袭击，已造成至少16人死亡。";
        System.out.println(getJaccardSimilarity(a,b));
        System.out.println(getJaccardSimilarity(b,b));
        System.out.println(getLongestCommonSubsequence(a, b));
        System.out.println(getLongestCommonSubsequence(a, c));
        System.out.println(fuzzyScore.fuzzyScore(a,b));
        System.out.println(fuzzyScore.fuzzyScore(b,b));
        System.out.println(fuzzyScore.fuzzyScore(a,c));
        System.out.println(longestCommonSubsequenceDistance.apply(a, b));
        System.out.println(longestCommonSubsequenceDistance.apply(b, b));
        System.out.println(LevenshteinDistance.getDefaultInstance().apply(a,b));
        System.out.println(getLongestCommonSubsequence(d,e));
        System.out.println(longestCommonSubsequenceDistance.apply(d,e));
        System.out.println(getCommonStrLength(d,e));
    }

}
