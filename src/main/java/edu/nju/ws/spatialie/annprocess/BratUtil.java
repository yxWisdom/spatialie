package edu.nju.ws.spatialie.annprocess;

import java.lang.reflect.Array;
import java.util.*;

public class BratUtil {
    public static final String DATE = "Date";
    public static final String TIME = "Time";
    public static final String DURATION = "Duration";
    public static final String TIME_SET = "TimeSet";
    public static final String PLACE = "Place";
    public static final String MILITARY_PLACE = "MilitaryPlace";
    public static final String MILITARY_BASE = "MilitaryBase";
    public static final String COUNTRY = "Country";
    public static final String ADMIN_DIV = "AdministrativeDivision";
    public static final String PATH = "Path";
    public static final String P_MILITARY_PLACE = "P_MilitaryPlace";
    public static final String ORGANIZATION = "Organization";
    public static final String ARMY = "Army";
    public static final String PERSON = "Person";
    public static final String COMMANDER = "Commander";
    public static final String WEAPON = "Weapon";
    public static final String EVENT = "Event";
    public static final String MILITARY_EXERCISE = "MilitaryExercise";
    public static final String CONFERENCE = "Conference";
    public static final String SPATIAL_ENTITY = "SpatialEntity";
    public  static final String MEASURE = "Measure";
    public static final String SPATIAL_SIGNAL = "SpatialSignal";
    public static final String MOTION = "Motion";
    public static final String MOTION_SIGNAL = "MotionSignal";
    public static final String LITERAL = "Literal";

    public static final String TLINK = "TLINK";
    public static final String OLINK = "OLINK";
    public static final String MLINK = "MLINK";
    public static final String DLINK = "DLINK";

    public static final String COREFERENCE = "coreference";

    static final Map<String, String> nerTagMap= new HashMap<String, String>(){{
        put("PERSON", PERSON);
        put("ORGANIZATION", ORGANIZATION);
        put("GPE", ADMIN_DIV);
        put("CITY", ADMIN_DIV);
        put("STATE_OR_PROVINCE", ADMIN_DIV);
        put("COUNTRY", COUNTRY);
        put("FACILITY", PLACE);
        put("LOCATION", PLACE);
        put("TIME", TIME);
        put("DATE", DATE);
        put("NATIONALITY", null);
        put("DEMONYM", null);
    }};

    public static final Map<String,String> subtypeMap = new HashMap<String, String>() {{
        put(PLACE,PLACE);
        put(MILITARY_PLACE,PLACE);
        put(MILITARY_BASE,PLACE);
        put(COUNTRY,PLACE);
        put(ADMIN_DIV,PLACE);
        put(PATH,PLACE);
        put(P_MILITARY_PLACE,PATH);
        put(MILITARY_EXERCISE,EVENT);
        put(EVENT, EVENT);
        put(CONFERENCE, EVENT);
    }};

    public static final Set<String> availableLabels = new HashSet<>(Arrays.asList("Place", "MilitaryPlace", "MilitaryBase",
            "Country","AdministrativeDivision", "Path", "P_MilitaryPlace", "SpatialEntity", "SpatialSignal", "Motion",
            "Event", "MilitaryExercise", "Conference", "Measure", "MotionSignal"));

    static boolean isTimeTag(String tag) {
        return tag.equals(DATE) || tag.equals(TIME) || tag.equals(DURATION) || tag.equals(TIME_SET);
    }

    public static void main(String [] args) {
        System.out.println(nerTagMap.get("O"));
    }
}
