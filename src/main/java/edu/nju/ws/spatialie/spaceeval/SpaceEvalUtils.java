package edu.nju.ws.spatialie.spaceeval;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.utils.CollectionUtils;
import edu.nju.ws.spatialie.utils.FileUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.omg.CORBA.INTERNAL;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static edu.nju.ws.spatialie.utils.CollectionUtils.union;


public class SpaceEvalUtils {
    static final String PLACE="PLACE";
    static final String PATH="PATH";
    static final String SPATIAL_ENTITY="SPATIAL_ENTITY";
    static final String NONMOTION_EVENT="NONMOTION_EVENT";
    static final String MOTION ="MOTION";
    public static final String SPATIAL_SIGNAL="SPATIAL_SIGNAL";
    static final String MOTION_SIGNAL="MOTION_SIGNAL";
    static final String MEASURE="MEASURE";
    static final String QSLINK="QSLINK";
    static final String OLINK="OLINK";
    static final String OTLINK="OTLINK";
    static final String MOVELINK="MOVELINK";
    static final String MEASURELINK="MEASURELINK";
    static final String METALINK="METALINK";

    static final String MOVER="mover";
    static final String TRIGGER="trigger";
    public static final String TRAJECTOR="trajector";
    public static final String LANDMARK="landmark";
    static final String VAL="val";
    static final String SEMANTIC_TYPE ="semantic_type";
    static final String MOTION_TYPE = "motion_type";
    static final String MOTION_CLASS = "motion_class";
    static final String GOAL ="goal";
    static final String MID_POINT = "midPoint";
    static final String SOURCE="source";
    static final String GROUND = "ground";
    static final String PATH_ID="pathID";
    static final String MOTION_SIGNAL_ID="motion_signalID";

//    static final String LANDMARK_M="landmark";
//    static final String MOTION_SIGNAL_M="motion_signalID";


    static final String TOPOLOGICAL="TOPOLOGICAL";
    static final String DIRECTIONAL="DIRECTIONAL";
    static final String DIR_TOP="DIR_TOP";



    static final Set<String> allLinkTags = new HashSet<>(Arrays.asList(QSLINK, OLINK, MOVELINK, MEASURELINK));
    static final Set<String> allElementTags = new HashSet<>(Arrays.asList(PLACE, PATH, SPATIAL_ENTITY, NONMOTION_EVENT,
            MOTION, SPATIAL_SIGNAL, MOTION_SIGNAL, MEASURE));

    // 主要的元素类型，即元素识别任务需要识别的标签类型
    final static Set<String> mainElementTags =  new HashSet<>(Arrays.asList(PLACE, PATH, SPATIAL_ENTITY, NONMOTION_EVENT,
            MOTION));

    final static Set<String> moveLinkRoles = new HashSet<>(
            Arrays.asList("mover", "trigger", "source", "midPoint", "goal", "ground", "landmark", "motion_signalID", "pathID", "adjunctID"));
    final static Set<String> qsLinkRoles = new HashSet<>(Arrays.asList("trajector", "landmark", "trigger"));
    final static Set<String> oLinkRoles = new HashSet<>(Arrays.asList("trajector", "landmark", "trigger"));
    final static Set<String> mLinkRoles = new HashSet<>(Arrays.asList("trajector", "landmark", "val"));
    final static Set<String> allRoles = new HashSet<>(Arrays.asList("trajector", "landmark", "trigger", "val",
            "mover", "source", "midPoint", "goal", "ground", "motion_signalID", "pathID", "referencePt", "adjunctID"));

    final static Set<String> mLinkCoreRoles = new HashSet<>(Arrays.asList(MOVER, TRIGGER));
    final static Set<String> mLinkOptionalRoles = new HashSet<>(Arrays.asList(SOURCE, MID_POINT, GOAL, GROUND, MOTION_SIGNAL_ID, PATH_ID));

    final static Set<String> moverTypes = new HashSet<>(Arrays.asList(SPATIAL_ENTITY, PATH, PLACE));
    //    final static Set<String> pathTypes = new HashSet<>(Arrays.asList(SPATIAL_ENTITY, PATH, PLACE));
    final static Set<String>  trajectorTypes= new HashSet<>(Arrays.asList(SPATIAL_ENTITY, PATH, PLACE, MOTION, NONMOTION_EVENT));
    final static Set<String>  landmarkTypes= new HashSet<>(Arrays.asList(SPATIAL_ENTITY, PATH, PLACE));

    final static Set<String> attributeTypes = new HashSet<String>() {{add("relType");add("frame_type");}};

//    final static Set<String> goalsourceTypes = new HashSet<>(Arrays.asList(PATH));
//    final static Set<String> mothonsigTypes = new HashSet<>(Arrays.asList(MOTION_SIGNAL));

    final static Set<Pair<String ,String>> moveLinkPatterns = new HashSet<Pair<String ,String>>(){{
        add(new ImmutablePair<>(SPATIAL_ENTITY, MOTION)); // 648
        add(new ImmutablePair<>(PATH, MOTION));           // 52
        add(new ImmutablePair<>(PLACE, MOTION));          // 16
        add(new ImmutablePair<>("", MOTION));        // 45
        add(new ImmutablePair<>(NONMOTION_EVENT, MOTION));// 4
    }};

    final static Set<Triple<String ,String, String>> measureLinkPatterns = new HashSet<Triple<String ,String, String>>(){{
        add(new ImmutableTriple<>(MEASURE,MOTION,MOTION));          // 13
        add(new ImmutableTriple<>(MEASURE,PLACE,PLACE));            // 23
        add(new ImmutableTriple<>(MEASURE,"",""));    // 1
        add(new ImmutableTriple<>(MEASURE,PATH,PATH));              // 1
        add(new ImmutableTriple<>(MEASURE,"",""));    // 1
        add(new ImmutableTriple<>(MEASURE,"",""));    // 1
        add(new ImmutableTriple<>(MEASURE,"",""));    // 1
        add(new ImmutableTriple<>(MEASURE,"",""));    // 1
    }};

    static Map<String, String> invalidCharFixedMap = new HashMap<String, String>() {{
        put("â€“", "—");
        put("â€”", "–");
        put("â€˜", "‘");
        put("â€™", "’");
        put("â€œ", "“");
        put("â€\u009D", "”");
    }};

    //计算元素之间的最大距离
    static int calcElementDistance(Span...elements) {

        int start = 0x7ffffff, end = 0;
        for (Span element: elements) {
            if (element.start == -1)
                continue;
            start = Math.min(start, element.start);
            end = Math.max(end, element.end);
        }
        return end - start;
    }

    //计算元素之间的最大距离, 以单词个数为单位
    static int calElementTokenLevelDistance(List<Span> allTokens, List<Span> elements) {
        List<Span> sortedElements = elements.stream().filter(o -> o.start != -1).sorted().collect(Collectors.toList());
        if (elements.size() == 0) return -1;
        Span minElement = sortedElements.get(0), maxElement = sortedElements.get(sortedElements.size()-1);
        int startIndex = Collections.binarySearch(allTokens, minElement);
        int endIndex = Collections.binarySearch(allTokens, maxElement);

        if (startIndex < 0) {
            startIndex = - startIndex - 1;
        }

        if (endIndex < 0) {
            endIndex = - endIndex - 1;
        }

        if (endIndex - startIndex == -1) {
            System.out.println();
        }

        return endIndex - startIndex;
    }

    static int calElementNumBetweenElements(List<Span> allElements, List<Span> elements) {
        List<Span> sortedElements = elements.stream().filter(o -> o.start != -1).sorted().collect(Collectors.toList());
        if (elements.isEmpty())
            return -1;
        Span minElement = sortedElements.get(0), maxElement = sortedElements.get(sortedElements.size()-1);
        int startIndex = Collections.binarySearch(allElements, minElement);
        int endIndex = Collections.binarySearch(allElements, maxElement);

        if (startIndex < 0 || endIndex < 0) {
            return -1;
        }
        return endIndex - startIndex;
    }

    static boolean validCandidateRoleType(Span candidate, String roleType) {
        String elementType = candidate.label;
        switch (roleType) {
            case TRAJECTOR: return trajectorTypes.contains(elementType);
            case MOVER: return moverTypes.contains(elementType);
            case PATH_ID: return elementType.equals(PATH);
            case MOTION_SIGNAL_ID: return elementType.equals(MOTION_SIGNAL);
            default: return landmarkTypes.contains(elementType);
        }
    }



    // 检验是否有sentence不含任何关系
    private static void checkSentenceContainLink(String srcDir) {
        List<File> files = FileUtil.listFiles(srcDir, true);

        int totalNum = 0, hasNoLinkNum = 0;
        for (File file: files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<Span> tags = spaceEvalDoc.getElements();
            List<List<Span>> sentences = spaceEvalDoc.getSentences();
            Map<String, Span> tagMap = tags.stream().collect(Collectors.toMap(t -> t.id, t -> t, (x, y) -> x));
            List<BratEvent> allLinks = spaceEvalDoc.getAllLinks();

            List<Span> filteredTags = new ArrayList<>();
            for (BratEvent bratEvent: allLinks) {
                for (String id: bratEvent.getRoleMap().values()) {
                    if (!tagMap.containsKey(id)) {
                        System.out.println(1);
                    }
                    filteredTags.add(tagMap.get(id));
                }
            }
            Collections.sort(filteredTags);

            for (List<Span> sentence: sentences) {
                boolean flag = false;
                for (Span token: sentence) {
                    Span tmpTag = new Span("", "", "", token.start,token.end);
                    int index = Collections.binarySearch(filteredTags, tmpTag);
                    if (index >= 0) {
                        flag = true; break;
                    }
                }
                if (!flag) {
                    hasNoLinkNum++;
                    sentence.forEach(x->System.out.print(x.text + " "));
                    System.out.println();
                }
                totalNum++;
            }
        }
        System.out.println("total:"+totalNum+", noLink: " + hasNoLinkNum);
    }


    // 检查是否有跨句子的link
    private static void checkLinkAcrossSentence(String srcDir) {
        List<File> files = FileUtil.listFiles(srcDir);
        for (File file: files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<Span> tags = spaceEvalDoc.getElements();
            List<List<Span>> sentences = spaceEvalDoc.getSentences();
            List<BratEvent> allLinks = spaceEvalDoc.getAllLinks();
            Map<String, Span> tagMap = tags.stream().collect(Collectors.toMap(t -> t.id, t -> t, (x, y) -> x));

            List<Pair<Integer, Integer>> offset = new ArrayList<>();
            for(List<Span> sentence: sentences) {
                int start = sentence.get(0).start;
                int end = sentence.get(sentence.size()-1).end;
                offset.add(new ImmutablePair<>(start, end));
            }

            for (BratEvent bratEvent: allLinks) {
                Collection<String> roleIds = bratEvent.getRoleMap().values();
                Set<Integer> sentenceIds = new HashSet<>();
                for (String roleId: roleIds) {
                    Span tag = tagMap.get(roleId);
                    for (int i=0; i < offset.size();i++) {
                        Pair<Integer, Integer> pair = offset.get(i);
                        if (pair.getLeft() <= tag.start && tag.end <= pair.getRight()) {
                            sentenceIds.add(i);
                        }
                    }
                }
                if (sentenceIds.size() > 1) {
                    for (int i: sentenceIds) {
                        sentences.get(i).forEach(x->System.out.print(x.text + " "));
                        System.out.println();
                    }
                }

            }

        }
    }
    // 获取关系的各个Role的类型pattern
    public static void checkLinkPatterns(String srcDir) {
        List<File> files = FileUtil.listFiles(srcDir);
        Map<Pair<String, String>, Integer> moveLinkPattern = new HashMap<>();
        Map<Triple<String, String, String>, Integer> otLinkPattern = new HashMap<>();
        Map<Triple<String, String, String>, Integer> dLinkPattern = new HashMap<>();
        for (File file: files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<BratEvent> allLinks = spaceEvalDoc.getAllLinks();
            Map<String, Span> map = spaceEvalDoc.getElementMap();
            for (BratEvent link: allLinks) {
                Span span = new Span("", "", "", -1, -1);
                String mover = map.getOrDefault(link.getRoleId("mover"), span).label;
                String trigger = map.getOrDefault(link.getRoleId("trigger"), span).label;
                String trajector = map.getOrDefault(link.getRoleId("trajector"), span).label;
                String landmark = map.getOrDefault(link.getRoleId("landmark"), span).label;
                String val = map.getOrDefault(link.getRoleId("val"), span).label;
                if (link.getType().equals(SpaceEvalUtils.MOVELINK)) {
                    int count = moveLinkPattern.getOrDefault(new ImmutablePair<>(mover, trigger), 0);
                    moveLinkPattern.put(new ImmutablePair<>(mover, trigger), count+1);
                } else if(link.getType().equals(SpaceEvalUtils.MEASURELINK)) {
                    int count = dLinkPattern.getOrDefault(new ImmutableTriple<>(val, trajector, landmark), 0);
                    dLinkPattern.put(new ImmutableTriple<>(val, trajector, landmark), count+1);
                } else {
                    int count = otLinkPattern.getOrDefault(new ImmutableTriple<>(trigger, trajector, landmark), 0);
                    otLinkPattern.put(new ImmutableTriple<>(trigger, trajector, landmark), count+1);
                }
            }
        }
        System.out.println("moveLink:");
        System.out.println(moveLinkPattern.entrySet().stream()
                .collect(Collectors.toMap(o->o.getKey().getLeft(), Map.Entry::getValue, (o, v)-> o+v)));
        System.out.println(moveLinkPattern.entrySet().stream()
                .collect(Collectors.toMap(o->o.getKey().getRight(), Map.Entry::getValue, (o, v)-> o+v)));

        System.out.println("measureLink:");
        System.out.println(dLinkPattern.entrySet().stream()
                .collect(Collectors.toMap(o->o.getKey().getLeft(), Map.Entry::getValue, (o, v)-> o+v)));
        System.out.println(dLinkPattern.entrySet().stream()
                .collect(Collectors.toMap(o->o.getKey().getMiddle(), Map.Entry::getValue, (o, v)-> o+v)));
        System.out.println(dLinkPattern.entrySet().stream()
                .collect(Collectors.toMap(o->o.getKey().getRight(), Map.Entry::getValue, (o, v)-> o+v)));
//        dLinkPattern.forEach((x,y)-> System.out.println(x + ": " + y));

        System.out.println("QSLink&OLink");
        System.out.println(otLinkPattern.entrySet().stream()
                .collect(Collectors.toMap(o->o.getKey().getLeft(), Map.Entry::getValue, (o, v)-> o+v)));
        System.out.println(otLinkPattern.entrySet().stream()
                .collect(Collectors.toMap(o->o.getKey().getMiddle(), Map.Entry::getValue, (o, v)-> o+v)));
        System.out.println(otLinkPattern.entrySet().stream()
                .collect(Collectors.toMap(o->o.getKey().getRight(), Map.Entry::getValue, (o, v)-> o+v)));

//        otLinkPattern.forEach((x,y)-> System.out.println(x + ": " + y));

        System.out.println("****************");

    }


    private static void checkMaxElementNumMapBetweenElements(String srcDir) {
        Map<Integer, Integer> countMap = new TreeMap<>();
        Map<Integer, Double>  percentMap= new TreeMap<>();
        Map<Integer, Integer>  sumMap= new TreeMap<>();

        List<File> files = FileUtil.listFiles(srcDir);
        String [][] typePairs = {{TRAJECTOR, LANDMARK}, {TRAJECTOR, TRIGGER}, {LANDMARK, TRIGGER}, {MOVER, TRIGGER}};
        for (File file: files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<Span> allElements = spaceEvalDoc.getElements().stream().filter(o -> o.start >= 0).collect(Collectors.toList());
            Map<String, Span> elementMap = spaceEvalDoc.getElementMap();
            List<BratEvent> allLinks = spaceEvalDoc.getAllLinks();
            for (BratEvent link : allLinks) {
                for (String [] pair: typePairs) {
                    if (pair[0].equals(TRAJECTOR) && pair[1].equals(LANDMARK) && (link.hasRole(TRIGGER) || link.hasRole(VAL)))
                        continue;
                    List<Span> elementList1 = link.getRoleIds(pair[0]).stream().map(elementMap::get).collect(Collectors.toList());
                    List<Span> elementList2 = link.getRoleIds(pair[1]).stream().map(elementMap::get).collect(Collectors.toList());
                    for (Span element1: elementList1) {
                        for (Span element2: elementList2) {
                            int num = calElementNumBetweenElements(allElements, Arrays.asList(element1, element2));
                            int count = countMap.getOrDefault(num, 0);
                            countMap.put(num, count + 1);
                        }
                    }
                }
            }

        }
        int sum = 0;

        for (Integer key: countMap.keySet()) {
            sum += countMap.get(key);
            sumMap.put(key, sum);
        }
        for (Integer key: sumMap.keySet()) {
            percentMap.put(key, 1.0*sumMap.get(key)/sum);
        }
        System.out.println(countMap);
        System.out.println(sumMap);
        System.out.println(percentMap);

        System.out.println();
    }




    // 计算一个link的跨越长度
    private static Map<Integer, Integer> CalMaxDistanceOfLink(String srcDir, String linkType, String [] elementType) {
        return CalMaxDistanceOfLink(srcDir, linkType, elementType, "");
    }

    private static Map<Integer, Integer> CalMaxDistanceOfLink(String srcDir, String linkType, String [] elementType, String redundantRole) {

        Map<Integer, Integer> map = new TreeMap<>();

        List<File> files = FileUtil.listFiles(srcDir);
        Set<String> elementTypeSet = new HashSet<>(Arrays.asList(elementType));
        int maxDistance = 0;
        for (File file: files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<BratEvent> allLinks = spaceEvalDoc.getAllLinks().stream()
                    .filter(x -> x.getType().equals(linkType)).collect(Collectors.toList());
            Map<String, Span> elementMap = spaceEvalDoc.getElementMap();
            for (BratEvent link : allLinks) {
                List<Span> tokens = spaceEvalDoc.getAllTokenOfLink(link);
                if (tokens.size() == 0) {
                    continue;
                }
                if (spaceEvalDoc.getAllSentencesOfLink(link).size() != 1)
                    continue;
                int senStart = tokens.get(0).start, senEnd = tokens.get(tokens.size() - 1).end;
                List<Span> elements = new ArrayList<>();
                if (link.getRoleIds(redundantRole).size() > 0)
                    continue;
                for (Map.Entry<String, String> entry: link.getRoleMap().entries()) {
                    if (elementTypeSet.contains(entry.getKey())) {
                        Span role = elementMap.get(entry.getValue());
                        if (role.start == -1 || role.end < senStart || role.start > senEnd) continue;
                        elements.add(role);
                    }
                }


                int distance = calElementTokenLevelDistance(tokens, elements);

                if (distance < 0)
                    continue;

                int count = map.getOrDefault(distance, 0);
                map.put(distance, count + 1);
                maxDistance = Math.max(maxDistance, distance);
            }
        }
        System.out.println(linkType + " " + Arrays.toString(elementType) + ":" + maxDistance);
        System.out.println(map);
        return map;
    }


    private static void mergeCountMap(Map<Integer, Integer> map1, Map<Integer, Integer> map2) {
        for (Map.Entry<Integer, Integer> entry : map2.entrySet()) {
            int count = map1.getOrDefault(entry.getKey(), 0);
            map1.put(entry.getKey(), entry.getValue() + count);
        }
    }

    // 计算所有link的最大跨越长度
    private static void checkMaxDistanceOfLink(String srcDir) {

        Map<Integer, Integer> countMap = new TreeMap<>();
        Map<Integer, Double>  percentMap= new TreeMap<>();
        Map<Integer, Integer>  sumMap= new TreeMap<>();

        mergeCountMap(countMap, CalMaxDistanceOfLink(srcDir, MOVELINK, new String[] {"mover", "trigger"}));
        mergeCountMap(countMap, CalMaxDistanceOfLink(srcDir, QSLINK, new String[] {"trajector", "trigger"}));
        mergeCountMap(countMap, CalMaxDistanceOfLink(srcDir, QSLINK, new String[] {"landmark", "trigger"}));
        mergeCountMap(countMap, CalMaxDistanceOfLink(srcDir, QSLINK, new String[] {"trajector", "landmark"}));

        CalMaxDistanceOfLink(srcDir, QSLINK, new String[] {"trajector", "landmark"}, "trigger");
        CalMaxDistanceOfLink(srcDir, QSLINK, new String[] {"trajector", "landmark", "trigger"});

        mergeCountMap(countMap, CalMaxDistanceOfLink(srcDir, OLINK, new String[] {"trajector", "trigger"}));
        mergeCountMap(countMap, CalMaxDistanceOfLink(srcDir, OLINK, new String[] {"landmark", "trigger"}));
        mergeCountMap(countMap, CalMaxDistanceOfLink(srcDir, OLINK, new String[] {"trajector", "landmark"}));

        CalMaxDistanceOfLink(srcDir, OLINK, new String[] {"trajector", "landmark"}, "trigger");
        CalMaxDistanceOfLink(srcDir, OLINK, new String[] {"trajector", "landmark", "trigger"});

        mergeCountMap(countMap, CalMaxDistanceOfLink(srcDir, MEASURELINK, new String[] {"trajector", "val"}));
        mergeCountMap(countMap, CalMaxDistanceOfLink(srcDir, MEASURELINK, new String[] {"landmark", "val"}));
        mergeCountMap(countMap, CalMaxDistanceOfLink(srcDir, MEASURELINK, new String[] {"trajector", "landmark"}));

        CalMaxDistanceOfLink(srcDir, MEASURELINK, new String[] {"trajector", "landmark"}, "val");
        CalMaxDistanceOfLink(srcDir, MEASURELINK, new String[] {"trajector", "landmark", "val"});

        int sum = 0;

        for (Integer key: countMap.keySet()) {
            sum += countMap.get(key);
            sumMap.put(key, sum);
        }
        for (Integer key: sumMap.keySet()) {
            percentMap.put(key, 1.0*sumMap.get(key)/sum);
        }
        System.out.println(countMap);
        System.out.println(sumMap);
        System.out.println(percentMap);

        System.out.println();

    }


    // 检查是否有无trigger的link
    private static void checkNoTriggerLink(String srcDir) {
        List<File> files = FileUtil.listFiles(srcDir);
        for (File file: files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<BratEvent> allLinks = spaceEvalDoc.getAllLinks();
            for (BratEvent link : allLinks) {
                Multimap<String, String> roleMap = link.getRoleMap();
                if (link.getType().equals(QSLINK) && !roleMap.containsKey("trigger")
                        && roleMap.containsKey("trajector") && roleMap.containsKey("landmark")) {
                    List<Span> tokens = spaceEvalDoc.getAllTokenOfLink(link);
                    System.out.println(spaceEvalDoc.getPath());
                    tokens.forEach(x -> System.out.print(x.text + " "));
                    System.out.println();
                    System.out.println();
                }
            }
        }
    }

    // 获取QSlink和Olink的trigger类型
    private static Map<String, Map<String, Integer>> getQSAndOLinkTriggerType(String srcDir) {
        Map<String, Map<String, Integer>> map = new HashMap<>();
        List<File> files = FileUtil.listFiles(srcDir);
        for (File file: files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<BratEvent> allLinks = spaceEvalDoc.getAllLinks();
            Map<String, Span> elementMap = spaceEvalDoc.getElementMap();
            for (BratEvent link : allLinks) {
                if ((link.getType().equals(QSLINK) || link.getType().equals(OLINK)) &&
                        link.hasRole(TRIGGER)) {
                    String text = elementMap.get(link.getRoleId(TRIGGER)).text;
                    map.putIfAbsent(text, new HashMap<>());
                    int count = map.get(text).getOrDefault(link.getType(), 0);
                    map.get(text).put(link.getType(), count + 1);
                }
            }
        }
        return map;
    }

    // 检查相同的trigger的link的类型在训练集和测试中是否是一致的
    private static void checkQSAndOLinkTrigger(String trainDir, String testDir) {

        Map<String, Map<String, Integer>> train = getQSAndOLinkTriggerType(trainDir);
        Map<String, Map<String, Integer>> test = getQSAndOLinkTriggerType(testDir);

        System.out.println(test.size());
        System.out.println(train.size());
        test.forEach((k,v) -> {
            if (train.containsKey(k)) {
                Map<String, Integer> v2= train.get(k);
                if (v.size() !=  v2.size() || !v.keySet().containsAll(((Map) v2).keySet())) {
                    System.out.println(k + " " + v + " " + v2);
                }
            }
        });

//        List<String> qslink = new ArrayList<>();
//        List<String> olink = new ArrayList<>();
//        List<String> both = new ArrayList<>();
//        map.forEach((k,v) -> {
//            if (v.size() == 2) {
//                both.add(k);
//            } else if (v.contains(OLINK)) {
//                olink.add(k);
//            } else {
//                qslink.add(k);
//            }
//        });
//        System.out.println(qslink.size());
//        System.out.println(olink.size());
//        System.out.println(both.size());
//        System.out.println(qslink);
//        System.out.println(olink);
//        System.out.println(both);
    }

    // 检查token和element是否对齐
    private static void checkTokenSpanAlign(String srcDir) {
        List<File> files = FileUtil.listFiles(srcDir);
        for (File file: files) {
            System.out.println(file.getName());
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<Span> tokens = spaceEvalDoc.getTokens();
            Map<String, Span> tokenMap = tokens.stream().filter(x -> x.id!=null && !x.id.equals(""))
                    .collect(Collectors.toMap(x->x.id, x->x, (old, _new) -> {
                        old.text = old.text + " " + _new.text;
                        old.end = _new.end;
                        return old;
                    }));
            Map<String, Span> elementMap = spaceEvalDoc.getElements().stream().filter(x->x.start!=-1)
                    .collect(Collectors.toMap(x->x.id, x->x));

            elementMap.forEach((k,v) -> {
                if (!tokenMap.containsKey(k)) {
                    System.out.println("token中不存在element:" + v);
                } else if (v.start != tokenMap.get(k).start || v.end != tokenMap.get(k).end) {
                    Span token = tokenMap.get(k);
                    System.out.println("未对齐！ " + "token: " + token + " Element: " + v);
                }
            });
        }
    }

    // 检查是否有包含非法字符的Token
    private static void checkInvalidToken(String srcDir) {
        List<File> files = FileUtil.listFiles(srcDir);
        Set<String> set = new TreeSet<>();
        for (File file: files) {
            System.out.println(file.getName());
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            spaceEvalDoc.getTokens().forEach(x -> {
                String pattern = "^[\\s0-9a-zA-Z]+$";
                if (!Pattern.matches(pattern, x.text)) {
                    set.add(x.text);
                }
            });
        }
        set.forEach(System.out::println);
    }

    //检查相同trigger的link的role是否不一致，例如link A的trajector是a, link B的landmark是a
    private static void checkDifferencesOfSameTrigger(String srcDir) {
        List<File> files = FileUtil.listFiles(srcDir);
        for (File file: files) {
            System.out.println(file.getName());
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<BratEvent> allLinks = spaceEvalDoc.getAllLinks();
            Map<String, List<BratEvent>> map = new HashMap<>();
            allLinks.forEach(link -> {
                if (link.hasRole(TRIGGER) || link.hasRole(VAL)) {
                    String trigger = link.hasRole(TRIGGER) ? link.getRoleId(TRIGGER) : link.getRoleId(VAL);
                    map.putIfAbsent(trigger, new ArrayList<>());
                    map.get(trigger).add(link);
                }
            });

            map.forEach((trigger, links)->{
                for (int i = 0; i < links.size(); i++) {
                    for (int j = i + 1; j < links.size(); j++) {
                        BratEvent link1 = links.get(i), link2 = links.get(j);
                        if (link1.hasRole(TRAJECTOR) && link2.hasRole(LANDMARK)) {
//                            Collection<String> trajectors1 = ,landmarks1 = ;
                            Set<String> trajectors = new HashSet<>(link1.getRoleIds(TRAJECTOR)), landmarks = new HashSet<>(link2.getRoleIds(LANDMARK));
                            List<String> intersections = trajectors.stream().filter(landmarks::contains).collect(Collectors.toList());
                            if (!intersections.isEmpty()) {
                                System.out.println();
                                System.out.println(file.getName() + "\n" + spaceEvalDoc.getAllTokenOfLink(link1).stream().map(o -> o.text).collect(Collectors.joining(" ")));
                                System.out.println();
                            }
                        }

                        if (link1.hasRole(LANDMARK) && link2.hasRole(TRAJECTOR)) {
                            Set<String> trajectors = new HashSet<>(link2.getRoleIds(TRAJECTOR));
                            Set<String>  landmarks = new HashSet<>(link1.getRoleIds(LANDMARK));
                            List<String> intersections = trajectors.stream().filter(landmarks::contains).collect(Collectors.toList());
                            if (!intersections.isEmpty()) {
                                System.out.println();
                                System.out.println(file.getName() + "\n" + spaceEvalDoc.getAllTokenOfLink(link1).stream().map(o -> o.text).collect(Collectors.joining(" ")));
                                System.out.println();
                            }
                        }


                        if (link1.getType().equals(link2.getType()) && (link1.hasRole(TRAJECTOR) || link1.hasRole(LANDMARK))
                                && (link2.hasRole(TRAJECTOR) ||  link2.hasRole(LANDMARK))) {
                            Collection<String> trajectors1 = link1.getRoleIds(TRAJECTOR),landmarks1 = link1.getRoleIds(LANDMARK);
                            Collection<String> trajectors2 = link2.getRoleIds(TRAJECTOR),landmarks2 = link2.getRoleIds(LANDMARK);
                            if (trajectors1.size() == 0 || landmarks1.size() == 0 || trajectors2.size() == 0 || landmarks2.size() == 0) {
                                System.out.println();
                            }
                            if (union(trajectors1, trajectors2).size() == 0 && union(landmarks1, landmarks2).size() == 0) {
                                System.out.println();
                                System.out.println("***" + file.getName() + "\n" + spaceEvalDoc.getAllTokenOfLink(link1).stream().map(o -> o.text).collect(Collectors.joining(" ")));
                                System.out.println();
                            }
                        }
//
//                        if (link1.hasRole(TRAJECTOR) && link2.hasRole(TRAJECTOR) && link1.hasRole(LANDMARK) && link2.hasRole(LANDMARK)) {
//                            Collection<String> trajectors1 = link1.getRoleIds(TRAJECTOR),landmarks1 = link1.getRoleIds(LANDMARK);
//                            Collection<String> trajectors2 = link2.getRoleIds(TRAJECTOR),landmarks2 = link2.getRoleIds(LANDMARK);
//                            trajectors1.retainAll(landmarks2);
//                            trajectors2.retainAll(landmarks1);
//                            if (!trajectors1.isEmpty() || !trajectors2.isEmpty()) {
//                                System.out.println(file.getName() + " " + spaceEvalDoc.getAllTokenOfLink(link1).stream().map(o -> o.text).collect(Collectors.joining(" ")));
//                            }
//                        }
                    }
                }
            });

        }
    }


    public static void statisticsLinks(String srcDir) {
        List<File> files = FileUtil.listFiles(srcDir);
        int qs_num=0, o_num=0, move_num=0;
        int qs_no_trigger = 0, o_no_trigger = 0, qs_no_trigger_1 = 0, o_no_trigger_1 = 0, m_no_trigger=0;
        for (File file: files) {
            System.out.println(file.getName());
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<BratEvent> allLinks = spaceEvalDoc.getAllLinks();
            for (BratEvent link: allLinks) {
                if (link.getType().equals(QSLINK)) {
                    qs_num++;
                    if (link.getRoleIds(TRIGGER).size() == 0) {
                        qs_no_trigger++;
//                        String trajector  = link.getRoleId(TRAJECTOR);
//                        String landmark  = link.getRoleId(LANDMARK);
//                        System.out.println("QSLINK: " + elementMap.get(trajector).text + "  :  "
//                                + elementMap.get(landmark).text);

                        if (link.getRoleIds(TRAJECTOR).isEmpty() || link.getRoleIds(LANDMARK).isEmpty()) {
                            qs_no_trigger_1 ++;
                        }
                    }
                }
                if (link.getType().equals(OLINK)) {
                    o_num++;
                    if (link.getRoleIds(TRIGGER).size() == 0) {
                        o_no_trigger ++;
                        if (link.getRoleIds(TRAJECTOR).isEmpty() || link.getRoleIds(LANDMARK).isEmpty()) {
                            o_no_trigger_1 ++;
                        }
                    }
                }
                if (link.getType().equals(MOVELINK)) {
                    move_num++;
                    if (link.getRoleIds(TRIGGER).isEmpty()) {
                        m_no_trigger ++;
                    }
                }
            }
        }

        System.out.format("qs_num: %d\n", qs_num);
        System.out.format("o_num: %d\n", o_num);
        System.out.format("move_num: %d\n", move_num);
        System.out.format("qs_no_trigger_num: %d\n", qs_no_trigger);
        System.out.format("o_no_trigger_num: %d\n", o_no_trigger);
        System.out.format("m_no_trigger_num: %d\n", m_no_trigger);
        System.out.format("qs_no_trigger_1_num: %d\n", qs_no_trigger_1);
        System.out.format("o_no_trigger_1_num: %d\n", o_no_trigger_1);

        System.out.println(qs_num - qs_no_trigger);
        System.out.println(o_num - o_no_trigger);
    }

    public static void statisticsLink(String srcDir) {
        List<File> files = FileUtil.listFiles(srcDir);
        Map<String, Integer> map1 = new HashMap<>();
        for (File file: files) {
            System.out.println(file.getName());
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            spaceEvalDoc.getAllLinks().forEach(link -> {
                String type = link.getType();
                int cnt1 = map1.getOrDefault(type, 0);
                map1.put(type, cnt1 + 1);
            });
        }
        int total1 = 0;
        for (Map.Entry<String, Integer> entry : map1.entrySet()) {
            String k = entry.getKey();
            Integer v = entry.getValue();
            total1 += v;
            System.out.println(k + " " + v);
        }
        System.out.println("total " + total1);
    }

    public static void statisticsElement(String srcDir) {
        List<File> files = FileUtil.listFiles(srcDir);
        Map<String, Integer> map1 = new HashMap<>();
        Map<String, Integer> map2 = new HashMap<>();
        for (File file: files) {

            System.out.println(file.getName());
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<Span> elements = spaceEvalDoc.getElements();
            elements.forEach(element -> {
                String type = element.label;
                int cnt2 = map2.getOrDefault(type, 0);
                map2.put(type, cnt2 + 1);
                if (element.start != -1) {
                    int cnt1 = map1.getOrDefault(type, 0);
                    map1.put(type, cnt1 + 1);
                }
            });
        }

        int total1 = 0;
        for (Map.Entry<String, Integer> entry : map1.entrySet()) {
            String k = entry.getKey();
            Integer v = entry.getValue();
            total1 += v;
            System.out.println(k + " " + v);
        }
        System.out.println("total " + total1);
        System.out.println();
        total1 = 0;
        for (Map.Entry<String, Integer> entry : map2.entrySet()) {
            String k = entry.getKey();
            Integer v = entry.getValue();
            total1 += v;
            System.out.println(k + " " + v);
        }
        System.out.println("total " + total1);
    }




    private static String linkWithSentence(BratEvent link, List<Span> tokensOfLink, Map<String, Span> elementMap) {
        String relType = link.getAttribute("relType");
        String frame_type = link.getAttribute("frame_type");
        String linkType = link.getType();
        String info = String.format("[%s/%s/%s] ", linkType, relType, frame_type);

        Multimap<String, Span> roleMap = HashMultimap.create();
        link.getRoleMap().forEach((k,v) -> roleMap.put(k, elementMap.get(v)));
        StringBuilder sb = new StringBuilder(info);
        tokensOfLink.forEach(token -> {
            roleMap.forEach((roleType, role) -> {
                if (role.start == token.start) {
                    sb.append("【");
                }
            });
            sb.append(token.text).append(" ");
            roleMap.forEach((roleType, role) -> {
                if (role.end == token.end) {
                    sb.deleteCharAt(sb.length() - 1).append("】(").append(roleType).append("/").append(role.label).append(") ");
                }
            });
        });
        return sb.toString();
    }

    public static void getLinksWithoutTrigger(String srcDir, String targetPath) {
        List<String> examples = new ArrayList<>();
        List<File> files = FileUtil.listFiles(srcDir);
        for (File file: files) {
            examples.add(file.getName());
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            Map<String, Span> elementMap = spaceEvalDoc.getElementMap();
            List<BratEvent> allLinks = spaceEvalDoc.getAllLinks();

            for(BratEvent link: allLinks) {
                String linkType = link.getType();
                if ((linkType.equals(QSLINK) || linkType.equals(OLINK))) {
//                    List<Span> roles = new ArrayList<>();
//                    oLinkRoles.forEach(role -> {
//                        if (link.hasRole(role)) {
//                            roles.add(elementMap.get(link.getRoleId(role)));
//                        }
//                    });
                    if (link.hasRole(TRIGGER)) {
                        List<Span> tokensOfLink = spaceEvalDoc.getAllTokenOfLink(link);

                        Span trigger = elementMap.get(link.getRoleId(TRIGGER));
                        String triggerText = trigger.text.toLowerCase().trim();
                        if (triggerText.equals("from") || triggerText.equals("of")) {
                            System.out.println(trigger.text);
                            System.out.println(tokensOfLink.stream().map(x -> x.text).collect(Collectors.joining(" ")));
                            System.out.println(linkWithSentence(link, tokensOfLink, elementMap));
                            System.out.println();
                        }
                    }

                    if (!link.hasRole(TRIGGER) && link.hasRole(TRAJECTOR) && link.hasRole(LANDMARK)) {
                        List<Span> tokensOfLink = spaceEvalDoc.getAllTokenOfLink(link);
//                        StringBuilder sb = new StringBuilder();
//                        Span trajector = elementMap.get(link.getRoleId(TRAJECTOR));
//                        Span landmark = elementMap.get(link.getRoleId(LANDMARK));
//                        tokensOfLink.forEach(token -> {
//                            if (token.start == trajector.start || token.start == landmark.start) {
//                                sb.append("【");
//                            }
//                            sb.append(token.text).append(" ");
//                            if (token.end == trajector.end || token.end == landmark.end) {
//                                String role = token.end == trajector.end ? "T" : "L";
//                                sb.append("】").append(role).append(" ");
//                            }
//
//                        });
//                        String relType = link.getAttribute("relType");
//                        String otherInfo = String.format("[%s/%s]", linkType, relType);
//                        sb.append(otherInfo);
                        examples.add(tokensOfLink.stream().map(x -> x.text).collect(Collectors.joining(" ")));
                        examples.add(linkWithSentence(link, tokensOfLink, elementMap));
                        examples.add("");
                    }
                }
            }
        }
        FileUtil.writeFile(targetPath, examples);
    }
    public static void analyseSpatialSignalTypes(String srcDir, String targetPath) {
        List<File> files = FileUtil.listFiles(srcDir);
        Map<String, Integer> word2cnt = new HashMap<>();
        for (File file: files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            Map<String, Span> elementMap = spaceEvalDoc.getElementMap();
            elementMap.forEach((id, ele) -> {
                if (ele.label.equals(SPATIAL_SIGNAL)) {
                    String word = ele.text.toLowerCase();
                    int cnt = word2cnt.getOrDefault(word, 0);
                    word2cnt.put(word, cnt+1);
                }
            });
        }
        List<String> lines = new ArrayList<>();
        word2cnt.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(e -> lines.add(e.getKey() + "\t" + e.getValue()));
        FileUtil.writeFile(targetPath, lines);
    }

    public static void analyseNoTriggerRelType(String srcDir, String targetPath) {
        List<File> files = FileUtil.listFiles(srcDir);
        Map<String, Integer> oLinkMap = new TreeMap<>();
        Map<String, Integer> qsLinkMap = new TreeMap<>();

        int qsNoTriggerNum = 0;
        int oNoTriggerNum = 0;

        int validQSNoTriggerNum = 0;
        int validONoTriggerNum = 0;


        for (File file: files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            Map<String, Span> elementMap = spaceEvalDoc.getElementMap();
            List<BratEvent> links = spaceEvalDoc.getAllLinks();
            for (BratEvent link: links) {
                if (!link.hasRole(TRIGGER)) {
                    if (link.getType().equals(QSLINK)) {
                        qsNoTriggerNum++;
                    } else {
                        oNoTriggerNum++;
                    }
                }
                if (!link.hasRole(TRIGGER) && link.hasRole(TRAJECTOR) && link.hasRole(LANDMARK)) {
                    String relType = link.getAttribute("relType");
                    if (link.getType().equals(QSLINK)) {
                        int count = qsLinkMap.getOrDefault(relType, 0);
                        qsLinkMap.put(relType, count+1);
                        validQSNoTriggerNum++;
                    } else if (link.getType().equals(OLINK)) {
                        String frame_type =  link.getAttribute("frame_type");
                        String key = relType + "\t" + frame_type;
                        int count = oLinkMap.getOrDefault(key, 0);
                        oLinkMap.put(key, count+1);
                        validONoTriggerNum++;
                    }
                }
            }

        }

        System.out.println("no-trigger QSLINk:" + qsNoTriggerNum);
        System.out.println("no-trigger OLINk:" + oNoTriggerNum);
        System.out.println("valid no-trigger QSLINk:" + validQSNoTriggerNum);
        System.out.println("valid no-trigger OLINk:" + validONoTriggerNum);
        List<String> lines = new ArrayList<>();
        qsLinkMap.forEach((k,v) -> lines.add(k + "\t" + v));
        oLinkMap.forEach((k,v) -> lines.add(k + "\t" + v));
        FileUtil.writeFile(targetPath, lines);
    }


    public static void analyseRelType(String srcDir, String targetPath) {
        List<File> files = FileUtil.listFiles(srcDir);
        Map<String, Map<String, Integer>> oLinkMap = new HashMap<>();
        Map<String, Map<String, Integer>> qsLinkMap = new HashMap<>();
        Map<String, Map<String, Integer>> qsAndOLinkMap = new HashMap<>();
        for (File file: files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            Map<String, Span> elementMap = spaceEvalDoc.getElementMap();
            List<BratEvent> links = spaceEvalDoc.getAllLinks();

            Map<String, String> qsID2RelType = new HashMap<>();
            Map<String, String> oID2RelType = new HashMap<>();

            links.forEach(link -> {
                if (link.hasRole(TRIGGER)) {
                    String triggerId = link.getRoleId(TRIGGER);
                    String trigger = elementMap.get(triggerId).text.toLowerCase().trim();
                    String relType = link.getAttribute("relType");
                    if (link.getType().equals(QSLINK)) {
                        qsLinkMap.putIfAbsent(trigger, new TreeMap<>());
                        int count = qsLinkMap.get(trigger).getOrDefault(relType, 0);
                        qsLinkMap.get(trigger).put(relType, count+1);
                        qsID2RelType.put(triggerId, relType);
                    } else if (link.getType().equals(OLINK)) {
                        String frame_type =  link.getAttribute("frame_type");
//                        String key = relType + "\t" + frame_type;
                        oLinkMap.putIfAbsent(trigger, new TreeMap<>());
                        int count = oLinkMap.get(trigger).getOrDefault(relType, 0);
                        oLinkMap.get(trigger).put(relType, count+1);
                        oID2RelType.put(triggerId, relType);
                    }
                }
            });

            Collection<String> triggerIds = CollectionUtils.intersect(qsID2RelType.keySet(), oID2RelType.keySet());
            triggerIds.forEach(triggerId -> {
                String trigger = elementMap.get(triggerId).text.toLowerCase().trim();
                String relType = qsID2RelType.get(triggerId) + " " + oID2RelType.get(triggerId);
                qsAndOLinkMap.putIfAbsent(trigger, new TreeMap<>());
                int count = qsAndOLinkMap.get(trigger).getOrDefault(relType, 0);
                qsAndOLinkMap.get(trigger).put(relType, count+1);
            });

        }
        List<String> lines = new ArrayList<>();

        Set<String> qsSignals = new TreeSet<>();
        Set<String> oSignals = new TreeSet<>();

        qsLinkMap.forEach((k,v) -> qsSignals.addAll(v.keySet()));
        lines.addAll(qsSignals);

        oLinkMap.forEach((k,v) -> oSignals.addAll(v.keySet()));
        lines.addAll(oSignals);
        lines.add("");

        Set<String> words = new TreeSet<>(CollectionUtils.union(qsLinkMap.keySet(), oLinkMap.keySet()));

        words.forEach(word -> {
            lines.add(word);
//            lines.add(QSLINK);
            qsLinkMap.getOrDefault(word, new HashMap<>()).forEach((k,v) -> {
                lines.add("\t" + k + "\t" + v);
            });
//            lines.add(OLINK);
            oLinkMap.getOrDefault(word, new HashMap<>()).forEach((k,v) -> {
                lines.add("\t" + k + "\t" + v);
            });
            lines.add("");
            qsAndOLinkMap.getOrDefault(word, new HashMap<>()).forEach((k,v) -> {
                lines.add("\t" + k + "\t" + v);
            });
            lines.add("");
        });
        FileUtil.writeFile(targetPath, lines);
    }

    public static void analyseMotionClass(String srcDir, String targetPath) {

        Map<String, String> class2focusMap = new HashMap<String ,String>() {{
            put("MOVE", "");
            put("MOVE_EXTERNAL", "ground");
            put("MOVE_INTERNAL", "ground");
            put("LEAVE", "source");
            put("REACH", "goal");
            put("DETACH", "source");
            put("HIT","goal");
            put("CROSS", "midPoint");
            put("FOLLOW", "pathID");
            put("DEVIATE", "pathID");
        }};


        List<File> files = FileUtil.listFiles(srcDir);
        Map<String, Map<String, Integer>> word2MotionMap = new HashMap<>();
        for (File file: files) {
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            Map<String, Span> elementMap = spaceEvalDoc.getElementMap();
            List<BratEvent> links = spaceEvalDoc.getAllLinks();
            links.forEach(link -> {
                if (link.getType().equals(MOVELINK) && link.hasRole(TRIGGER)) {
                    String triggerId = link.getRoleId(TRIGGER);
                    String trigger = elementMap.get(triggerId).text.toLowerCase().trim();
                    String motionClass = elementMap.get(triggerId).getAttribute(MOTION_CLASS);
                    word2MotionMap.putIfAbsent(trigger, new TreeMap<>());
                    int count = word2MotionMap.get(trigger).getOrDefault(motionClass, 0);
                    word2MotionMap.get(trigger).put(motionClass, count+1);

                    Set<String> roles = new HashSet<>(link.getRoleMap().keys());
                    roles.removeAll(Arrays.asList(MOVER, TRIGGER, MOTION_SIGNAL_ID,class2focusMap.get(motionClass)));

                    if (!motionClass.equals("MOVE") && !roles.isEmpty()) {
                        System.out.println(trigger + " " + motionClass + roles.toString());
                    }

                }
            });
        }
        System.out.println();

        Set<String> motions = new TreeSet<>();
        word2MotionMap.forEach((k,v) -> motions.addAll(v.keySet()));
        List<String> lines = new ArrayList<>(motions);
        lines.add("");

        word2MotionMap.entrySet().stream()
                .sorted(Comparator.comparing(o -> o.getValue().values().stream().reduce(Integer::sum).orElse(0),
                        Comparator.reverseOrder()))
                .forEach(entry -> {
                    lines.add(entry.getKey());
                    entry.getValue().forEach((k,v) -> {
                        lines.add("\t" + k + "\t" + v);
                    });
                });
        FileUtil.writeFile(targetPath, lines);

    }

    public static void analyseReferencePt(String srcDir, String targetPath) {
        List<String> examples = new ArrayList<>();
        List<File> files = FileUtil.listFiles(srcDir);
        for (File file: files) {
            examples.add(file.getName());
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            Map<String, Span> elementMap = spaceEvalDoc.getElementMap();
            List<BratEvent> allLinks = spaceEvalDoc.getAllLinks();

            for(BratEvent link: allLinks) {
                String linkType = link.getType();
                if (linkType.equals(OLINK)) {
                    List<Span> tokensOfLink = spaceEvalDoc.getAllTokenOfLink(link);
                    String referencePtID = link.getRoleId("referencePt");
                    String landmarkID = link.getRoleId(LANDMARK);
                    if (!referencePtID.equals(landmarkID) && elementMap.containsKey(referencePtID) && elementMap.get(referencePtID).start != -1) {
                        examples.add(tokensOfLink.stream().map(x -> x.text).collect(Collectors.joining(" ")));
                        examples.add(linkWithSentence(link, tokensOfLink, elementMap));
                        examples.add("");
                    }
                }
            }
            System.out.println(file.getName());
        }
        FileUtil.writeFile(targetPath, examples);
    }

    // 检测一个trigger是否有多个QSLINK或OLINK的RelType
    public static void checkRelTypesBetweenLinksWithTheSameTrigger(String srcDir) {
        List<File> files = FileUtil.listFiles(srcDir);
        for (File file: files) {
            System.out.println(file.getName());
//            Map<String, Set<String>> trigger2QSRelTypes = new HashMap<>();
//            Map<String, Set<String>> trigger2ORelTypes = new HashMap<>();

            Map<String, Set<String>> trigger2RelTypes = new HashMap<>();

            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<BratEvent> allLinks = spaceEvalDoc.getAllLinks();
            Map<String, List<BratEvent>> map = new HashMap<>();
            Map<String, Span> elementMap = spaceEvalDoc.getElementMap();
            allLinks.forEach(link -> {
                if (link.hasRole(TRIGGER)) {
                    String linkType = link.getType();
                    String relType = link.getAttribute("relType");
                    String triggerID = link.getRoleId(TRIGGER);
                    String triggerText = elementMap.get(triggerID).text;
                    String key = triggerID + "\t" + triggerText + "\t" + linkType;
                    trigger2RelTypes.putIfAbsent(key, new HashSet<>());
                    trigger2RelTypes.get(key).add(relType);
                }
            });

            trigger2RelTypes.forEach((key, set) -> {
                if (set.size() > 1) {
                    System.out.println(key + set);
                }
            });
        }
    }
    public static void analysisMoveLinkRoleNumber(String srcDir) {
        List<File> files = FileUtil.listFiles(srcDir);
        Map<String, List<String>> role2set1 = new HashMap<>();
        Map<String, List<String>> role2set2 = new HashMap<>();
        for (File file: files) {
            System.out.println(file.getName());
            SpaceEvalDoc spaceEvalDoc = new SpaceEvalDoc(file.getPath());
            List<BratEvent> links = spaceEvalDoc.getAllLinks()
                    .stream().filter(x -> x.getType().equals(MOVELINK))
                    .collect(Collectors.toList());
            for (BratEvent link : links) {
                Multimap<String, String> map = link.getRoleMap();
                map.keySet().forEach(key -> {
                    if (!key.equals(MOTION_SIGNAL_ID) && map.get(key).size() > 1) {
                        role2set1.putIfAbsent(key, new ArrayList<>());
                        role2set1.get(key).add(map.get(key).toString());
                    }
                });
            }
            links = spaceEvalDoc.getMergedLinks()
                    .stream().filter(x -> x.getType().equals(MOVELINK))
                    .collect(Collectors.toList());
            for (BratEvent link : links) {
                Multimap<String, String> map = link.getRoleMap();
                map.keySet().forEach(key -> {
                    if (!key.equals(MOTION_SIGNAL_ID) && map.get(key).size() > 1) {
                        role2set2.putIfAbsent(key, new ArrayList<>());
                        role2set2.get(key).add(map.get(key).toString());
                    }
                });
            }
        }
        role2set1.forEach((x, y) -> {
            System.out.println(x + " " + y.size());
            System.out.println(x + " " + role2set2.get(x).size());
        });
    }


//    public static void checkMotionClassConstraint(String srcDir) {
//        List<File> files = FileUtil.listFiles(srcDir);
//        Map<String, String> map = new HashMap<String, String>() {{
//            put("MOVE", null);
//            put("MOVE_")
//        }}
//        Map<String, Set<String>> map = new HashMap<String, Set<String>>(){{
//
//        }}
//    }

    public static void main(String [] args) {
        String allPath = "data/SpaceEval2015/raw";
        String trainPath = "data/SpaceEval2015/raw_data/training++";
        String goldPath = "data/SpaceEval2015/raw_data/gold++";


//        SpaceEvalUtils.checkSentenceContainLink("data/SpaceEval2015/raw_data/training++");
//        SpaceEvalUtils.checkSentenceContainLink("data/SpaceEval2015/raw_data/gold++");

//        SpaceEvalUtils.checkLinkAcrossSentence("data/SpaceEval2015/raw_data/training++");
//        SpaceEvalUtils.checkLinkAcrossSentence("data/SpaceEval2015/raw_data/gold++");

//        SpaceEvalUtils.checkLinkPattern("data/SpaceEval2015/raw_data/training++");
//        SpaceEvalUtils.checkLinkPattern("data/SpaceEval2015/raw_data/gold++");

//        SpaceEvalUtils.checkMaxDistanceOfLink("data/SpaceEval2015/raw_data/training++");
//        SpaceEvalUtils.checkMaxDistanceOfLink("data/SpaceEval2015/raw_data/gold++");
//        SpaceEvalUtils.checkQSAndOLinkTrigger("data/SpaceEval2015/raw_data/training++",
//                "data/SpaceEval2015/raw_data/gold++");

//        SpaceEvalUtils.checkNoTriggerLink("data/SpaceEval2015/raw_data/training++");
//        SpaceEvalUtils.checkNoTriggerLink("data/SpaceEval2015/raw_data/gold++");

//        SpaceEvalUtils.checkTokenSpanAlign("data/SpaceEval2015/raw_data/training++");
//        SpaceEvalUtils.checkTokenSpanAlign("data/SpaceEval2015/raw_data/gold++");

//        SpaceEvalUtils.checkInvalidToken("data/SpaceEval2015/raw_data/training++");
//        SpaceEvalUtils.checkInvalidToken("data/SpaceEval2015/raw_data/gold++");

//        SpaceEvalUtils.checkDifferencesOfSameTrigger("data/SpaceEval2015/raw_data/training++");
//        SpaceEvalUtils.checkDifferencesOfSameTrigger("data/SpaceEval2015/raw_data/gold++");

//        SpaceEvalUtils.checkMaxElementNumMapBetweenElements("data/SpaceEval2015/raw_data/training++");
//        SpaceEvalUtils.checkMaxElementNumMapBetweenElements("data/SpaceEval2015/raw_data/gold++");

//        SpaceEvalUtils.statisticsLinks("data/SpaceEval2015/raw_data/gold++");
//        SpaceEvalUtils.statisticsLinks("data/SpaceEval2015/raw_data/training++");

//        SpaceEvalUtils.getLinksWithoutTrigger("data/SpaceEval2015/raw_data/training++",
//                "data/SpaceEval2015/analysis/train_noTrigger.txt");

//        SpaceEvalUtils.getLinksWithoutTrigger("data/SpaceEval2015/raw_data/gold++",
//                "data/SpaceEval2015/analysis/test_noTrigger.txt");

//        SpaceEvalUtils.analyseSpatialSignalTypes("data/SpaceEval2015/raw_data/training++",
//                "data/SpaceEval2015/analysis/train_SS.txt");
//
//        SpaceEvalUtils.analyseSpatialSignalTypes("data/SpaceEval2015/raw_data/gold++",
//                "data/SpaceEval2015/analysis/test_SS.txt");

//        SpaceEvalUtils.analyseReferencePt("data/SpaceEval2015/raw_data/training++",
//                "data/SpaceEval2015/analysis/train_referencePt.txt");
//
//        SpaceEvalUtils.analyseReferencePt("data/SpaceEval2015/raw_data/gold++",
//                "data/SpaceEval2015/analysis/test_referencePt.txt");

//        SpaceEvalUtils.analyseRelType("data/SpaceEval2015/raw_data/training++",
//                "data/SpaceEval2015/analysis/train_relTypes.txt");
//
//        SpaceEvalUtils.analyseRelType("data/SpaceEval2015/raw_data/gold++",
//                "data/SpaceEval2015/analysis/test_relTypes.txt");
//
//        SpaceEvalUtils.analyseRelType("data/SpaceEval2015/raw",
//                "data/SpaceEval2015/analysis/all_relTypes.txt");

//        SpaceEvalUtils.analyseMotionClass("data/SpaceEval2015/raw_data/training++",
//                "data/SpaceEval2015/analysis/train_motion_class.txt");
//
//        SpaceEvalUtils.analyseMotionClass("data/SpaceEval2015/raw_data/gold++",
//                "data/SpaceEval2015/analysis/test_motion_class.txt");

//        SpaceEvalUtils.analyseNoTriggerRelType(trainPath,
//                "data/SpaceEval2015/analysis/train_relType_noTrigger.txt");

//        SpaceEvalUtils.analyseNoTriggerRelType(goldPath,
//                "data/SpaceEval2015/analysis/test_relType_noTrigger.txt");

//        SpaceEvalUtils.checkRelTypesBetweenLinksWithTheSameTrigger("data/SpaceEval2015/raw_data/training++");
//        SpaceEvalUtils.checkRelTypesBetweenLinksWithTheSameTrigger("data/SpaceEval2015/raw_data/gold++");

//        SpaceEvalUtils.analysisMoveLinkRoleNumber(trainPath);
//        SpaceEvalUtils.analysisMoveLinkRoleNumber(goldPath);

//        SpaceEvalUtils.statisticsElement(trainPath);
//        SpaceEvalUtils.statisticsElement(goldPath);

        SpaceEvalUtils.statisticsLink(trainPath);
        SpaceEvalUtils.statisticsLink(goldPath);
    }
}
