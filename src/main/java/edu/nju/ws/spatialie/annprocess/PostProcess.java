package edu.nju.ws.spatialie.annprocess;

import edu.nju.ws.spatialie.data.*;
import edu.nju.ws.spatialie.utils.FileUtil;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class PostProcess {
    private Map<String, BratEntity> entityMap;
    private List<BratAttribute> bratAttributeList;
    private List<BratNote> bratNoteList;

    private boolean isEntity(String id) {
        return id.startsWith("T");
    }

    private boolean isAttribute(String id) {
        return id.startsWith("A");
    }

    private boolean isNote(String id) {
        return id.startsWith("#");
    }

    private boolean iseEvent(String id) {
        return id.startsWith("E");
    }

    private void loadAnnFile(String inputPath) {
        entityMap = new HashMap<>();
        bratAttributeList = new ArrayList<>();
        bratNoteList = new ArrayList<>();
        List<String> lines = FileUtil.readLines(inputPath);
        for (String line: lines) {
            if (isAttribute(line)) {
                BratAttribute attr = new BratAttribute(line);
//                attributeMap.put(attr.getEntityId(), attr);
                bratAttributeList.add(attr);
            } else if (isEntity(line)) {
                BratEntity bratEntity = new BratEntity(line, "", inputPath);
                entityMap.put(bratEntity.getId(), bratEntity);
            } else if (isNote(line)) {
                BratNote bratNote = new BratNote(line);
                bratNoteList.add(bratNote);
            }
        }
        filterAttribute();
        linkAttrToEntity();
        filterEntity();

//        for (Map.Entry<String, BratAttribute> entry: attributeMap.entrySet()) {
//            if (entityMap.containsKey(entry.getKey())) {
//                entityMap.get(entry.getKey()).getBratAttributes().add(entry.getValue());
//            }
//
//        }
//        for (Map.Entry<String, BratEntity> entry: entityMap.entrySet()) {
//            if (entry.getValue().getBratAttributes().size() > 1) {
//                System.out.println(1);
//            }
//
//        }
    }

    private void linkAttrToEntity() {
        for (BratAttribute attr: bratAttributeList) {
            if (entityMap.containsKey(attr.getOwner())) {
                entityMap.get(attr.getOwner()).getBratAttributes().add(attr);
            }
        }
        for (BratNote bratNote : bratNoteList) {
            if (entityMap.containsKey(bratNote.getOwner())) {
                entityMap.get(bratNote.getOwner()).setBratNote(bratNote);
            }
        }
    }


    private void filterEntity() {
//        String [] array = {"AnnotationStatus", "Date", "Time", "Duration", "TimeSet", }
        String [] array1 = {"Literal", "MotionSignal", "Motion", "SpatialSignal", "Measure",
                "SpatialEntity","AnnotationStatus", "TLINK", "OLINK", "DLINK", "MLINK", "BratEvent"};
        String [] array2 = {"MilitaryExercise", "Conference", "Weapon", "Army"};
//               ,"Time", "Date", "TimeSet", "Duration"};
        Set<String> set1 = new HashSet<>(Arrays.asList(array1));
        Set<String> set2 = new HashSet<>(Arrays.asList(array2));

        entityMap = entityMap.entrySet().stream()
                .filter(x->!set1.contains(x.getValue().getTag()))
                .filter(x -> x.getValue().isNAM()||set2.contains(x.getValue().getTag()))
                .filter(x -> x.getValue().getText().length() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void filterEntityPlace() {
        String [] array1 = {BratUtil.PLACE,BratUtil.MILITARY_BASE, BratUtil.MILITARY_PLACE, BratUtil.COUNTRY,BratUtil.ADMIN_DIV,
                BratUtil.PATH, BratUtil.P_MILITARY_PLACE, BratUtil.PERSON, BratUtil.COMMANDER, BratUtil.EVENT, BratUtil.MILITARY_EXERCISE,
                BratUtil.CONFERENCE, BratUtil.ORGANIZATION};
        String [] array2 = {"MilitaryExercise", "Conference", "Weapon", "Army"};
        Set<String> set1 = new HashSet<>(Arrays.asList(array1));
        Set<String> set2 = new HashSet<>(Arrays.asList(array2));

        entityMap = entityMap.entrySet().stream()
                .filter(x->set1.contains(x.getValue().getTag()))
                .filter(x -> x.getValue().isNAM()||set2.contains(x.getValue().getTag()))
                .filter(x -> x.getValue().getText().length() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void filterAttribute() {
        bratAttributeList = bratAttributeList.stream()
                .filter(x -> !x.getName().equals("mod"))
                .collect(Collectors.toList());
    }

    private void saveAllMention(String dirPath) {
        File folder = new File(dirPath);
        File [] files = folder.listFiles();
        assert files != null;
        Map<String, BratEntity> textToEntityMap = new HashMap<>();
        for (File file: files) {
            if (!file.getPath().endsWith(".ann")) continue;
            loadAnnFile(file.getPath());
            for (Map.Entry<String, BratEntity> entry: entityMap.entrySet()) {
                BratEntity e1 = entry.getValue();
                BratEntity e2 = textToEntityMap.get(e1.getText());
                if (e1.equals(e2)) {
                    e2.increase();
                } else if(e1.equalsWithoutAttributes(e2)){
                    if (e2.getBratAttributes().size() < e1.getBratAttributes().size())
                        e2.setBratAttributes(e1.getBratAttributes());
                    e2.increase();
                } else if(e2 == null) {
                    textToEntityMap.put(e1.getText(), e1);
                } else{
                    System.out.println(e1.getFilename());
                    System.out.println(e1.toString());
                    System.out.println(e2.getFilename());
                    System.out.println(e2.toString());
                }
            }
        }
        System.out.println(1);
    }


    Map<String, BratEntity> getAllMentionType(String dirPath) {
        File folder = new File(dirPath);
        File [] files = folder.listFiles();
        assert files != null;
        Map<String, List<BratEntity>> textToEntityListMap = new HashMap<>();
        Map<String, BratEntity> textToEntityMap = new HashMap<>();
        for (File file: files) {
            if (!file.getPath().endsWith(".ann")) continue;
            loadAnnFile(file.getPath());
            for (Map.Entry<String, BratEntity> entry: entityMap.entrySet()) {
                BratEntity e1 = entry.getValue();
                List<BratEntity> bratEntityList = textToEntityListMap.get(e1.getText());
                if (bratEntityList == null)
                    textToEntityListMap.put(e1.getText(), new ArrayList<BratEntity>(){{add(e1);}});
                else{
                    boolean flag = false;
                    for (BratEntity e2: bratEntityList) {
                        if(e1.equals(e2)){
                            e2.increase();
                            flag = true;
                        }
                    }
                    if (!flag) bratEntityList.add(e1);
                }
            }
        }
        for (String key: textToEntityListMap.keySet()) {
            List<BratEntity> list = textToEntityListMap.get(key);
            List<BratEntity> newList = new ArrayList<>();
            for (BratEntity e1: list) {
                boolean flag2=false;
                for (BratEntity e2: newList) {
                    if (e2.equalsWithoutAttributes(e1)) {
                        if (e2.getBratAttributes().size() < e1.getBratAttributes().size() || e2.getCount() < e1.getCount())
                            e2.setBratAttributes(e1.getBratAttributes());
                        e2.setCount(e2.getCount() + e1.getCount());
                        flag2=true;
                        break;
                    }
                }
                if (!flag2) {
                    newList.add(e1);
                }
            }
            newList.stream().max(Comparator.comparing(BratEntity::getCount)).ifPresent(o->textToEntityMap.put(key, o));
//            textToEntityMap.put(key, newList);
//            if (textToEntityMap.get(key).size()>1)
//                System.out.println(1);
        }
        return textToEntityMap;

    }

    public static void main(String  [] args) {
//        PostProcess p = new PostProcess();
//        p.saveAllMention("data/annotation/0-49");
//        p.saveAllMention_multiValue("data/annotation/0-49");
    }
}
