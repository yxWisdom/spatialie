package edu.nju.ws.spatialie.getrelation;

import edu.nju.ws.spatialie.utils.FileUtil;
import edu.stanford.nlp.util.ArrayMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WordData {
    static List<String> prepList = FileUtil.readLines("resource/relation/prep.txt");
    static List<String> verb2objList = FileUtil.readLines("resource/relation/2Object_verb.txt");
    static List<String> verbList = FileUtil.readLines("resource/relation/TLINK_verb.txt");
    static List<String> pathList = FileUtil.readLines("resource/relation/path_canbeMover.txt");
    static List<String> not_notrigger_of = FileUtil.readLines("resource/relation/not_notrigger_of.txt");
    static List<String> changelt = FileUtil.readLines("resource/relation/changelt.txt");
    static List<String> movement_trajector = FileUtil.readLines("resource/relation/movement_be_trajector.txt");
    static Map<String,String> confidenceMap = readMap("resource/relation/confidence.txt");
    static List<String> notriggersortList = FileUtil.readLines("resource/relation/notrigger_rules_sort.txt");
    static List<String> flagverbList = FileUtil.readLines("resource/relation/flagwords_v.txt");
    static List<String> flagextraList = FileUtil.readLines("resource/relation/flagwords_extra.txt");

    public static List<String> getFlagextraList() {
        return flagextraList;
    }

    public static List<String> getFlagverbList() {
        return flagverbList;
    }

    public static List<String> getNotriggersortList() {
        return notriggersortList;
    }

    public static Integer getNoTriggerRuleWeight(String rule){
        for (int i = 0;i<notriggersortList.size();i++){
            if (rule.equals(notriggersortList.get(i))) return notriggersortList.size()-i;
        }
        return 0;
    }

    public static Map<String, String> getConfidenceMap() {
        return confidenceMap;
    }

    private static Map<String, String> readMap(String path) {
        List<String> lines = FileUtil.readLines(path);
        Map<String,String> cm = new ArrayMap<>();
        for (String line:lines){
            String[] linec = line.split("\t");
            cm.put(linec[0],linec[1]);
        }
        return cm;
    }

    public static List<String> getChangelt() {
        return changelt;
    }

    public static List<String> getNot_notrigger_of() {
        return not_notrigger_of;
    }

    public static List<String> getPathList() {
        return pathList;
    }

    public static List<String> getVerbList() {
        return verbList;
    }

    public static List<String> getPrepList() {
        return prepList;
    }

    public static List<String> getVerb2objList() {
        return verb2objList;
    }

    public static List<String> getMovement_trajector() {
        return movement_trajector;
    }

    public static List<String> getMovement_trajector_with_obj(){
        return Arrays.asList("give".split(" "));
    }
}
