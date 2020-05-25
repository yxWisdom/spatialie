package edu.nju.ws.spatialie.spaceeval;

import com.google.common.collect.Multimap;
import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.utils.FileUtil;
import edu.stanford.nlp.util.ArrayHeap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

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
    static final String SPATIAL_SIGNAL="SPATIAL_SIGNAL";
    static final String MOTION_SIGNAL="MOTION_SIGNAL";
    static final String MEASURE="MEASURE";
    static final String QSLINK="QSLINK";
    static final String OLINK="OLINK";
    static final String MOVELINK="MOVELINK";
    static final String MEASURELINK="MEASURELINK";
    static final String METALINK="METALINK";

    static final String MOVER="mover";
    static final String TRIGGER="trigger";
    static final String TRAJECTOR="trajector";
    static final String LANDMARK="landmark";
    static final String VAL="val";

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
            "mover", "source", "midPoint", "goal", "ground", "motion_signalID", "pathID", "referencePt"));


    final static Set<String> moverTypes = new HashSet<>(Arrays.asList(SPATIAL_ENTITY, PATH, PLACE));
    final static Set<String>  trajectorTypes= new HashSet<>(Arrays.asList(SPATIAL_ENTITY, PATH, PLACE, MOTION, NONMOTION_EVENT));
    final static Set<String>  landmarkTypes= new HashSet<>(Arrays.asList(SPATIAL_ENTITY, PATH, PLACE));

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


    public static void main(String [] args) {
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

        SpaceEvalUtils.checkDifferencesOfSameTrigger("data/SpaceEval2015/raw_data/training++");
        SpaceEvalUtils.checkDifferencesOfSameTrigger("data/SpaceEval2015/raw_data/gold++");

//        SpaceEvalUtils.checkMaxElementNumMapBetweenElements("data/SpaceEval2015/raw_data/training++");
//        SpaceEvalUtils.checkMaxElementNumMapBetweenElements("data/SpaceEval2015/raw_data/gold++");

    }
}
