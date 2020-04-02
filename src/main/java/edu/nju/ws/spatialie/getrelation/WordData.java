package edu.nju.ws.spatialie.getrelation;

import edu.nju.ws.spatialie.utils.FileUtil;

import java.util.ArrayList;
import java.util.List;

public class WordData {
    static List<String> prepList = FileUtil.readLines("data/relation/prep.txt");
    static List<String> verb2objList = FileUtil.readLines("data/relation/2Object_verb.txt");
    static List<String> verbList = FileUtil.readLines("data/relation/TLINK_verb.txt");
    static List<String> pathList = FileUtil.readLines("data/relation/path_canbeMover.txt");
    static List<String> not_notrigger_of = FileUtil.readLines("data/relation/not_notrigger_of.txt");
    static List<String> changelt = FileUtil.readLines("data/relation/changelt.txt");

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
}
