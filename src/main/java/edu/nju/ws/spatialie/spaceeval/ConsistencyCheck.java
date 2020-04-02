package edu.nju.ws.spatialie.spaceeval;

import com.alibaba.fastjson.JSONObject;
import edu.nju.ws.spatialie.utils.CollectionUtils;
import edu.nju.ws.spatialie.utils.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


// 校验SRL Relation Multi-head Selection等生成的语料的一致性
public class ConsistencyCheck {
    private static void  checkSRLAndRelation(String[] srlDir, String[] relDir, boolean binary) {
        Map<String, Integer>  srlMap = Arrays.stream(srlDir).map(FileUtil::readLines)
                .flatMap(o -> o.stream().map(x -> {
                    String [] arr = x.split("\t");
                    String sentence = arr[1];
                    String [] labels = arr[3].split(" ");
                    Map<String, Integer> map = new HashMap<>();
                    for (String label: labels) {
                        if (label.startsWith("B-")) {
                            int num = map.getOrDefault(label, 0);
                            map.put(label, num+1);
                        }
                    }
                    AtomicInteger relNum = new AtomicInteger(1);
                    map.forEach((k,v)-> relNum.set(relNum.get()*v));
//                    System.out.println(relNum.get());
                    return new ImmutablePair<>(sentence, relNum.get());
                })).collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight, (n, o) -> n + o));


        int idx = binary ? 5: 7;
        Map<String, Integer>  relMap = Arrays.stream(relDir).map(FileUtil::readLines)
                .flatMap(o -> o.stream()
                        .map(x -> x.split(" "))
                        .filter(x -> x[0].equals("1"))
                        .map(x -> String.join(" ", Arrays.copyOfRange(x, idx, x.length))))
                .collect(Collectors.toMap(o->o, o->1, (n, o) -> n + o));

        int srlNum = srlMap.values().stream().reduce((o1, o2) -> o1+o2).orElse(0);
        int relNum = relMap.values().stream().reduce((o1, o2) -> o1+o2).orElse(0);
        List<String> a = new ArrayList<>(srlMap.keySet());
        a.removeAll(relMap.keySet());
        List<String> b = new ArrayList<>(relMap.keySet());
        b.removeAll(srlMap.keySet());
        System.out.println("srl lines");
        a.forEach(System.out::println);
        System.out.println("rel lines");
        b.forEach(System.out::println);

        System.out.println("rel num:" + relNum);
        System.out.println("srl num:" + srlNum);

        List<String> intersection = new ArrayList<>(relMap.keySet());
        intersection.retainAll(srlMap.keySet());
        for (String key: intersection) {
            if (!relMap.get(key).equals(srlMap.get(key))) {
                System.out.println(key + " " +  relMap.get(key) + " " + srlMap.get(key));
            }
        }
        System.out.println();
    }


    private static void  checkTraditionalRelAndRelation(String[] dirs1, String[] dirs2, String type,boolean binary) {
        Map<String, Integer>  traRelMap = Arrays.stream(dirs1).map(FileUtil::readLines)
                .flatMap(o -> o.stream()
                        .map(x -> (JSONObject)JSONObject.parse(x))
                        .filter(x->x.getString("relation").equals(type))
                        .map(x -> String.join(" ", x.getJSONArray("token").toJavaList(String.class)))
                ).collect(Collectors.toMap(o->o, o->1, (n, o) -> n + o));

        Map<String, Integer>  relMap = Arrays.stream(dirs2).map(FileUtil::readLines)
                .flatMap(o -> o.stream()
                        .map(x -> x.split(" "))
                        .filter(x -> x[0].equals("1"))
                        .map(x -> String.join(" ", Arrays.copyOfRange(x, binary ? 5: 7, x.length))))
                .collect(Collectors.toMap(o->o, o->1, (n, o) -> n + o));

        int traRelNum = traRelMap.values().stream().reduce((o1, o2) -> o1+o2).orElse(0);
        int relNum = relMap.values().stream().reduce((o1, o2) -> o1+o2).orElse(0);

        List<String> a = new ArrayList<>(traRelMap.keySet());
        a.removeAll(relMap.keySet());
        List<String> b = new ArrayList<>(relMap.keySet());
        b.removeAll(traRelMap.keySet());

        System.out.println("traditional Rel lines");
        a.forEach(System.out::println);
        System.out.println("rel lines");
        b.forEach(System.out::println);

        System.out.println("rel num:" + relNum);
        System.out.println("traditional Rel num:" + traRelNum);

        List<String> intersection = new ArrayList<>(relMap.keySet());
        intersection.retainAll(traRelMap.keySet());
        for (String key: intersection) {
            if (!relMap.get(key).equals(traRelMap.get(key))) {
                System.out.println(key + " " +  relMap.get(key) + " " + traRelMap.get(key));
            }
        }
        System.out.println();
    }

    private static void checkTraditionalRelAndSRL(String [] dir1, String [] dir2, String type) {
        Map<String, Integer>  traRelMap = Arrays.stream(dir1).map(FileUtil::readLines)
                .flatMap(o -> o.stream()
                        .map(x -> (JSONObject)JSONObject.parse(x))
                        .filter(x->x.getString("relation").equals(type))
                        .map(x -> String.join(" ", x.getJSONArray("token").toJavaList(String.class)))
                ).collect(Collectors.toMap(o->o, o->1, (n, o) -> n + o));

        Map<String, Integer>  srlMap = Arrays.stream(dir2).map(FileUtil::readLines)
                .flatMap(o -> o.stream().map(x -> {
                    String [] arr = x.split("\t");
                    String sentence = arr[1];
                    String [] labels = arr[3].split(" ");
                    int count = (int) Arrays.stream(labels).filter(t -> t.equals("B-" + type)).count();
                    return new ImmutablePair<>(sentence, count);
                }))
                .filter(o->o.getRight()> 0)
                .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight, (n, o) -> n + o));

        int srlNum = srlMap.values().stream().reduce((o1, o2) -> o1+o2).orElse(0);
        int traRelNum = traRelMap.values().stream().reduce((o1, o2) -> o1+o2).orElse(0);


        List<String> a = new ArrayList<>(srlMap.keySet());
        a.removeAll(traRelMap.keySet());
        List<String> b = new ArrayList<>(traRelMap.keySet());
        b.removeAll(srlMap.keySet());
        System.out.println("srl lines");
        a.forEach(System.out::println);
        System.out.println("traditional rel lines");
        b.forEach(System.out::println);

        System.out.println("srl num:" + srlNum);
        System.out.println(" traditional rel num:" + traRelNum);

        List<String> intersection = new ArrayList<>(traRelMap.keySet());
        intersection.retainAll(srlMap.keySet());
        for (String key: intersection) {
            if (!traRelMap.get(key).equals(srlMap.get(key))) {
                System.out.println(key + " " +  traRelMap.get(key) + " " + srlMap.get(key));
            }
        }
        System.out.println();
    }


    private static void checkMultiHeadAndSRL(String [] dir1, String [] dir2, String type) {
        List<String> multiHeadLines =  Arrays.stream(dir1).map(FileUtil::readLines).flatMap(Collection::stream).collect(Collectors.toList());
        Map<String, Integer>  multiHeadMap = CollectionUtils.split(multiHeadLines, "").stream().map(lines -> {
            List<String> tokens = new ArrayList<>();
            int count = 0;
            for (String line: lines) {
                if (line.trim().length() == 0) continue;
                String [] array = line.replaceAll(" ", "").split("\t");
                tokens.add(array[1]);
                count += StringUtils.countMatches(array[3], type);
            }
            return new ImmutablePair<>(String.join(" ", tokens), count);
        }).filter(o->o.getRight()> 0)
                .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight, (n, o) -> n + o));


        Map<String, Integer>  srlMap = Arrays.stream(dir2).map(FileUtil::readLines)
                .flatMap(o -> o.stream().map(x -> {
                    String [] arr = x.split("\t");
                    String sentence = arr[1];
                    String [] labels = arr[3].split(" ");
                    int count = (int) Arrays.stream(labels).filter(t -> t.equals("B-" + type)).count();
                    return new ImmutablePair<>(sentence, count);
                })).filter(o->o.getRight()> 0)
                .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight, (n, o) -> n + o));

        int multiHeadNum = multiHeadMap.values().stream().reduce((o1, o2) -> o1+o2).orElse(0);
        int srlNum = srlMap.values().stream().reduce((o1, o2) -> o1+o2).orElse(0);



        List<String> a = new ArrayList<>(srlMap.keySet());
        a.removeAll(multiHeadMap.keySet());
        List<String> b = new ArrayList<>(multiHeadMap.keySet());
        b.removeAll(srlMap.keySet());
        System.out.println("srl lines");
        a.forEach(System.out::println);
        System.out.println("multi head lines");
        b.forEach(System.out::println);

        System.out.println("multi head rel num:" + multiHeadNum);
        System.out.println("srl num:" + srlNum);

        List<String> intersection = new ArrayList<>(multiHeadMap.keySet());
        intersection.retainAll(srlMap.keySet());
        for (String key: intersection) {
            if (!multiHeadMap.get(key).equals(srlMap.get(key))) {
                System.out.println(key + " " +  multiHeadMap.get(key) + " " + srlMap.get(key));
            }
        }
        System.out.println();
    }


    private static void checkNoTriggerLink() {
        ConsistencyCheck.checkTraditionalRelAndRelation(new String [] {"data/SpaceEval2015/processed_data/openNRE/AllLink/train.txt"},
                new String [] {"data/SpaceEval2015/processed_data/relation/no-trigger-non-MoveLink/train.txt"}, "LocatedIn", true);

        ConsistencyCheck.checkTraditionalRelAndRelation(new String [] {"data/SpaceEval2015/processed_data/openNRE/AllLink/val.txt"},
                new String [] {"data/SpaceEval2015/processed_data/relation/no-trigger-non-MoveLink/dev.txt"}, "LocatedIn", true);

        ConsistencyCheck.checkSRLAndRelation(new String [] {"data/SpaceEval2015/processed_data/SRL/NoTriggerLink/train.txt"},
                new String [] {"data/SpaceEval2015/processed_data/relation/no-trigger-non-MoveLink/train.txt"}, true);


        ConsistencyCheck.checkSRLAndRelation(new String [] {"data/SpaceEval2015/processed_data/SRL/NoTriggerLink/dev.txt"},
                new String [] {"data/SpaceEval2015/processed_data/relation/no-trigger-non-MoveLink/dev.txt"}, true);
    }


    private static void checkMoveLink() {
        ConsistencyCheck.checkSRLAndRelation(new String [] {"data/SpaceEval2015/processed_data/SRL/MoveLink/train.txt"},
                new String [] {"data/SpaceEval2015/processed_data/relation/MoveLink/train.txt"}, true);

        ConsistencyCheck.checkSRLAndRelation(new String [] {"data/SpaceEval2015/processed_data/SRL/MoveLink/dev.txt"},
                new String [] {"data/SpaceEval2015/processed_data/relation/MoveLink/dev.txt"}, true);

        ConsistencyCheck.checkTraditionalRelAndRelation(new String [] {"data/SpaceEval2015/processed_data/openNRE/AllLink/train.txt"},
                new String [] {"data/SpaceEval2015/processed_data/relation/MoveLink/train.txt"}, "mover", true);

        ConsistencyCheck.checkTraditionalRelAndRelation(new String [] {"data/SpaceEval2015/processed_data/openNRE/AllLink/val.txt"},
                new String [] {"data/SpaceEval2015/processed_data/relation/MoveLink/dev.txt"}, "mover", true);
    }


    private static void checkQSAndOLink() {
//        ConsistencyCheck.checkSRLAndRelation(new String [] {"data/SpaceEval2015/processed_data/SRL/OLink/train.txt",
//                        "data/SpaceEval2015/processed_data/SRL/ONoTrigger/train.txt"},
//                new String [] {"data/SpaceEval2015/processed_data/relation/OLink/train.txt"}, false);
//
//
//        ConsistencyCheck.checkSRLAndRelation(new String [] {"data/SpaceEval2015/processed_data/SRL/OLink/dev.txt",
//                        "data/SpaceEval2015/processed_data/SRL/ONoTrigger/dev.txt"},
//                new String [] {"data/SpaceEval2015/processed_data/relation/OLink/dev.txt"}, false);
//
//        ConsistencyCheck.checkSRLAndRelation(new String [] {"data/SpaceEval2015/processed_data/SRL/QSLink/train.txt",
//                        "data/SpaceEval2015/processed_data/SRL/QSNoTrigger/train.txt"},
//                new String [] {"data/SpaceEval2015/processed_data/relation/QSLink/train.txt"}, false);
//
//
//        ConsistencyCheck.checkSRLAndRelation(new String [] {"data/SpaceEval2015/processed_data/SRL/QSLink/dev.txt",
//                        "data/SpaceEval2015/processed_data/SRL/QSNoTrigger/dev.txt"},
//                new String [] {"data/SpaceEval2015/processed_data/relation/QSLink/dev.txt"}, false);
//
//
//        ConsistencyCheck.checkSRLAndRelation(new String [] {"data/SpaceEval2015/processed_data/SRL/NonMoveLink/train.txt",
//                        "data/SpaceEval2015/processed_data/SRL/NoTriggerLink/train.txt"},
//                new String [] {"data/SpaceEval2015/processed_data/relation/non-MoveLink/train.txt"}, false);


        ConsistencyCheck.checkSRLAndRelation(new String [] {"data/SpaceEval2015/processed_data/SRL/NonMoveLink/dev.txt",
                        "data/SpaceEval2015/processed_data/SRL/NoTriggerLink/dev.txt"},
                new String [] {"data/SpaceEval2015/processed_data/relation/non-MoveLink/dev.txt"}, false);
//
//        ConsistencyCheck.checkMultiHeadAndSRL(new String [] {"data/SpaceEval2015/processed_data/multi-head/AllLink-Head/train.txt"},
//                new String [] {"data/SpaceEval2015/processed_data/SRL/NonMoveLink/train.txt"},"trajector");
//
//        ConsistencyCheck.checkMultiHeadAndSRL(new String [] {"data/SpaceEval2015/processed_data/multi-head/AllLink-Head/dev.txt"},
//                new String [] {"data/SpaceEval2015/processed_data/SRL/NonMoveLink/dev.txt"},"trajector");
//
//        ConsistencyCheck.checkMultiHeadAndSRL(new String [] {"data/SpaceEval2015/processed_data/multi-head/AllLink-Head/train.txt"},
//                new String [] {"data/SpaceEval2015/processed_data/SRL/NonMoveLink/train.txt"},"landmark");
//        ConsistencyCheck.checkMultiHeadAndSRL(new String [] {"data/SpaceEval2015/processed_data/multi-head/AllLink-Head/dev.txt"},
//                new String [] {"data/SpaceEval2015/processed_data/SRL/NonMoveLink/dev.txt"},"landmark");
    }


    public static void main(String [] args) {







//        ConsistencyCheck.checkSRLAndRelation(new String [] {"data/SpaceEval2015/processed_data/SRL/MoveLink/train.txt"},
//                new String [] {"data/SpaceEval2015/processed_data/relation/MoveLink/train.txt"}, true);
//
//
//        ConsistencyCheck.checkSRLAndRelation(new String [] {"data/SpaceEval2015/processed_data/SRL/MoveLink/dev.txt"},
//                new String [] {"data/SpaceEval2015/processed_data/relation/MoveLink/dev.txt"}, true);


//
//
//        ConsistencyCheck.checkTraditionalRelAndRelation(new String [] {"data/SpaceEval2015/processed_data/openNRE/AllLink/train.txt"},
//                new String [] {"data/SpaceEval2015/processed_data/relation/MoveLink/train.txt"}, "mover", true);
//
//        ConsistencyCheck.checkTraditionalRelAndRelation(new String [] {"data/SpaceEval2015/processed_data/openNRE/AllLink/val.txt"},
//                new String [] {"data/SpaceEval2015/processed_data/relation/MoveLink/dev.txt"}, "mover", true);


//        ConsistencyCheck.checkNoTriggerLink();
//        ConsistencyCheck.checkMoveLink();
        ConsistencyCheck.checkQSAndOLink();
    }
}
