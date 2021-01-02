package edu.nju.ws.spatialie.annprocess;

import com.google.common.collect.Multimap;
import edu.nju.ws.spatialie.data.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CheckAnnotation {

    // 检查相邻的地名是否标注了PartOf以及LocationIn
    private static void checkContinuousPlaceAnnotation(String baseDir) {
        File folder = new File(baseDir);
        File [] files = folder.listFiles();
        assert files != null;
        for (int i=0; i<files.length;i+=2) {
            BratDocument bratDocument = new BratDocument(files[i+1].getPath(),files[i].getPath());
            Map<String, BratEntity> entityMap = bratDocument.getEntityMap();
            Map<String, BratRelation> relationMap = bratDocument.getRelationMap();
            Set<Pair<String, String>> relationSet = relationMap.values().stream()
                    .filter(r -> r.getTag().equals("partOf") ||  r.getTag().equals("locatedIn"))
                    .map(r -> new ImmutablePair<>(r.getTargetId(), r.getSourceId()))
                    .collect(Collectors.toSet());
            List<BratEntity> entities = entityMap.values().stream()
                    .sorted(Comparator.comparing(BratEntity::getStart))
                    .collect(Collectors.toList());

            Set<String> placeTypes = new HashSet<>(Arrays.asList("Place", "MilitaryPlace", "MilitaryBase", "Country","AdministrativeDivision", "Path", "P_MilitaryPlace"));
            BratEntity current, next;
            for (int j = 0; j < entities.size() - 1; j++) {
                current = entities.get(j);
                next = entities.get(j+1);
                if (placeTypes.contains(current.getTag()) && placeTypes.contains(next.getTag())
                        && current.getEnd() == next.getStart() &&
                        !relationSet.contains(new ImmutablePair<>(current.getId(), next.getId()))) {
                    System.out.println(files[i+1].getPath() + " " + current.toString() + " " + next.toString());
                }
            }
        }
    }


    // 获取二元关系的最大跨越距离
    private static void getMaxDistanceForBinaryRelation(String baseDir) {
        File folder = new File(baseDir);
        File [] files = folder.listFiles();
        assert files != null;
        int max = -1;
        for (int i=0; i<files.length;i+=2) {
            BratDocument bratDocument = new BratDocument(files[i+1].getPath(),files[i].getPath());
            Map<String, BratEntity> entityMap = bratDocument.getEntityMap();
            List<BratRelation> relations = bratDocument.getRelationMap().values().stream()
                    .filter(r -> r.getTag().equals("partOf") ||  r.getTag().equals("locatedIn"))
                    .collect(Collectors.toList());
            for (BratRelation r: relations) {
                BratEntity e1 = entityMap.get(r.getSourceId()), e2 = entityMap.get(r.getTargetId());
                int distance = e1.getStart() > e2.getStart() ? e1.getStart() - e2.getEnd():e2.getStart()-e1.getEnd();
                max = Math.max(distance, max);
                if (max == distance) {
                    System.out.println(e1.toString() + e2.toString());
                }
            }
        }
        System.out.println(max);
    }


    // 检查
    private static void checkMotionLinkTrigger(String baseDir) {
        File folder = new File(baseDir);
        File [] files = folder.listFiles();
        assert files != null;
        for (int i=0; i<files.length;i+=2) {
            BratDocument bratDocument = new BratDocument(files[i+1].getPath(),files[i].getPath());
            for (BratEvent event:bratDocument.getEventMap().values()) {
                if(event.getType().equals("MLINK") && event.getRoleIds("mover").contains("")) {
                    System.out.println(event.getSentence());
                }
            }

        }
    }

    private static void checkPunctuationInRelationText(String baseDir) {
        File folder = new File(baseDir);
        File [] files = folder.listFiles();
        assert files != null;
        Pattern pattern = Pattern.compile("[\\pP\'\"“”‘’]");
        Set<String > punctuations = new HashSet<>();
        for (int i=0; i<files.length;i+=2) {
            BratDocument bratDocument = new BratDocument(files[i+1].getPath(),files[i].getPath());
            for (BratEvent event: bratDocument.getEventMap().values()) {
//                if (bratDocument.getContent().substring(event.getT_start(), event.getT_end()).)
                for (int j=event.getT_start(); j < event.getT_end();j++) {
                    String tmp = String.valueOf(bratDocument.getContent().charAt(j));
                    Matcher mc = pattern.matcher(tmp);
                    if (mc.find()){
                        if (!punctuations.contains(tmp))
                            System.out.println(tmp + "\t" + bratDocument.getContent().substring(event.getT_start(), event.getT_end()));
                        punctuations.add(tmp);
                    };
                }
            }
        }
//        punctuations.forEach(System.out::println);
//        System.out.println(punctuations);
    }



    private static void checkSingleConjWithoutTrigger(String baseDir) {
        File folder = new File(baseDir);
        File [] files = folder.listFiles();
        assert files != null;
        for (int i=0; i<files.length;i+=2) {
            BratDocument bratDocument = new BratDocument(files[i+1].getPath(),files[i].getPath());
            for (BratEvent event: bratDocument.getEventMap().values()) {
                Multimap<String, String> roleMap = event.getRoleMap();
                if (event.getType().equals("TLINK") || event.getType().equals("OLINK")) {
                    if (!roleMap.containsKey("trigger")) {
                        if (roleMap.containsKey("conj")) {
                            if (roleMap.get("conj").size() > 1) {
                                System.out.println("2 more conj:" + event.getSentence());
                            }
                        } else if(event.getId().startsWith("E")){
                            System.out.println("no conj:" + event.getSentence());
                        }
                    }
                }
            }
        }
    }

    //检查连续相邻的地点是否在关系中
    private static void  checkContinuousPlaceNotInRelation(String baseDir) {
        File folder = new File(baseDir);
        File [] files = folder.listFiles();
        assert files != null;

        int count = 0;
        for (int i=0; i<files.length;i+=2) {
            BratDocument bratDocument = new BratDocument(files[i+1].getPath(),files[i].getPath());

            Set<Pair<String, String>> pairs = bratDocument.getRelationMap().values().stream()
                    .filter(r-> r.getTag().equals("locatedIn") || r.getTag().equals("partOf"))
                    .map(r -> new ImmutablePair<>(r.getTargetId(), r.getSourceId()))
                    .collect(Collectors.toSet());

            List<BratEntity> entities = bratDocument.getEntityMap().values().stream()
                    .filter(e -> BratUtil.subtypeMap.getOrDefault(e.getTag(), "").equals("Place"))
                    .sorted(Comparator.comparingInt(BratEntity::getStart)).collect(Collectors.toList());


            for (int j=0; j < entities.size()-1; j++) {
                BratEntity e1 = entities.get(j), e2 = entities.get(j + 1);
                if (e1.getEnd() == e2.getStart()) {
                    if (!pairs.contains(new ImmutablePair<>(e1.getId(), e2.getId())))
                        System.out.println(files[i+1].getPath() + " " + e1.getText() + " " + e2.getText());
                    else
                        count++;
                }
            }
        }
        System.out.println(count);
    }

    //检查partOf中的地点是否在其他关系中
    private static void  check_1(String baseDir) {
        File folder = new File(baseDir);
        File [] files = folder.listFiles();
        assert files != null;
        for (int i=0; i<files.length;i+=2) {
            BratDocument bratDocument = new BratDocument(files[i+1].getPath(),files[i].getPath());
            List<BratEntity> sortedEntities = bratDocument.getEntityMap().values().stream()
                    .sorted(Comparator.comparing(BratEntity::getStart))
                    .collect(Collectors.toList());

            Set<Pair<String, String>> pairs = bratDocument.getRelationMap().values().stream()
                    .filter(r-> r.getTag().equals("locatedIn") || r.getTag().equals("partOf"))
                    .map(r -> new ImmutablePair<>(r.getTargetId(), r.getSourceId()))
                    .collect(Collectors.toSet());

            Set<String> entityIdsInEvent = bratDocument.getEventMap().values().stream().flatMap(o->o.getRoleMap().values()
                    .stream()).collect(Collectors.toSet());

            for (int k=0; k < sortedEntities.size()-1; k++) {
                BratEntity e1 = sortedEntities.get(k), e2 = sortedEntities.get(k + 1);
                if (e1.getEnd() == e2.getStart() && pairs.contains(new ImmutablePair<>(e1.getId(), e2.getId()))
                        && entityIdsInEvent.contains(e2.getId())) {
                    System.out.println(files[i+1] + " " + e2.toString());
                }
            }
        }
    }

    // 打印Literal标签所在的文档
    private static void  showLiteral(String baseDir) {
        File folder = new File(baseDir);
        File [] files = folder.listFiles();
        assert files != null;

        for (int i=0; i<files.length;i+=2) {
            BratDocument bratDocument = new BratDocument(files[i+1].getPath(),files[i].getPath());
            for (BratEvent event: bratDocument.getEventMap().values()) {
                for (String id: event.getRoleMap().values()) {
                    if (bratDocument.getEntityMap().get(id).getTag().equals("Literal")) {
                        System.out.println(files[i+1] + " " + event.getSentence());
                    }
                }
            }
        }
    }

    public static void main(String [] args) {
//        CheckAnnotation.checkContinuousPlaceAnnotation("data/annotation/0-49");
//        CheckAnnotation.getMaxDistanceForBinaryRelation("data/annotation/0-49");
//        CheckAnnotation.checkMotionLinkTrigger("data/annotation/0-49");
//        CheckAnnotation.checkPunctuationInRelationText("data/annotation/0-49");
//        CheckAnnotation.checkSingleConjWithoutTrigger("data/annotation/0-49");
//        CheckAnnotation.checkSingleConjWithoutTrigger("data/annotation/msra");
//        CheckAnnotation.check_1("data/annotation/0-49");

        CheckAnnotation.checkSingleConjWithoutTrigger("data/annotation/msra");
    }
}
