package edu.nju.ws.spatialie.annprocess;

import edu.nju.ws.spatialie.data.*;
import edu.nju.ws.spatialie.utils.FileUtil;

import java.util.*;
import java.util.stream.Collectors;

public class ModifyAnnotation {

    static private Set<String> getBratIdSet(String line) {
        Set<String> ids = new HashSet<>();
        if (line.startsWith("A")) {
            ids.add(new BratAttribute(line).getOwner());
        }
        if (line.startsWith("#")) {
            ids.add(new BratNote(line).getOwner());
        }
        if (line.startsWith("R")) {
            BratRelation bratRelation = new BratRelation(line);
            ids.add(bratRelation.getSourceId());
            ids.add(bratRelation.getTargetId());
        }
        if (line.startsWith("E")) {
            BratEvent bratEvent = new BratEvent(line);
            ids.addAll(bratEvent.getRoleMap().values());
        }
        return ids;
    }

    // 根据所在行数去除标注文件中的文本，生成新的标注文件
    private static void removeLinesByIndex(String inDir, String outDir, String documentName, Collection<Integer> indexes) {
        String inTextPath = inDir + "/" + documentName + ".txt";
        String inAnnPath = inDir + "/" + documentName + ".ann";

        String outTextPath = outDir + "/" + documentName + ".txt";
        String outAnnPath = outDir + "/" + documentName + ".ann";

        List<String> annLines = FileUtil.readLines(inAnnPath);
        String text = FileUtil.readFile(inTextPath);
        assert text != null;
        String [] textLines = text.split("\n");


        List<String> noteOrAttrLines =  annLines.stream().filter(l->l.startsWith("A")|| l.startsWith("#"))
                .collect(Collectors.toList());
//        List<String> relationLines =  annLines.stream().filter(l->l.startsWith("R")).collect(Collectors.toList());
//        List<String> eventLines =  annLines.stream().filter(l->l.startsWith("E")).collect(Collectors.toList());
        List<String> eventOrRelationLines = annLines.stream().filter(l->l.startsWith("R") || l.startsWith("E"))
                .collect(Collectors.toList());

        List<BratEntity> entities = annLines.stream().filter(t->t.startsWith("T"))
                .map(t->new BratEntity(t, text))
                .sorted(Comparator.comparing(BratEntity::getStart))
                .collect(Collectors.toList());

        Set<BratEntity> retainedEntities = new HashSet<>();
        Set<BratEntity> removedEntities =  new HashSet<>();

        List<String> retainedTextLines = new ArrayList<>();
        List<String> removedTextLines = new ArrayList<>();

        int offset = 0, reduction = 0, index = 0;
        int start, end, entityStart, entityEnd;

        for (int i=0; i<textLines.length;i++) {

            if (indexes.contains(i+1)) {
                reduction += textLines[i].length() + 1;
                removedTextLines.add(textLines[i]);
            } else {
                retainedTextLines.add(textLines[i]);
            }
            start = offset;
            end = offset + textLines[i].length() + 1;
            for(;index < entities.size();index++) {
                BratEntity bratEntity =  entities.get(index);
                if (!(start <= (entityStart = bratEntity.getStart()) && (entityEnd = bratEntity.getEnd()) <= end)) break;
                if (indexes.contains(i+1)) {
                    removedEntities.add(bratEntity);
                } else {
                    bratEntity.setStart(entityStart - reduction);
                    bratEntity.setEnd(entityEnd - reduction);
                    retainedEntities.add(bratEntity);
                }
            }
            offset += textLines[i].length() + 1;
        }

//        Set<String> removedEntityIdSet = removedEntities.stream().map(BratEntity::getId).collect(Collectors.toSet());
//        Set<String> retainedEntityIdSet = retainedEntities.stream().map(BratEntity::getId).collect(Collectors.toSet());

        Set<String> retainedAllIds = retainedEntities.stream().map(BratEntity::getId).collect(Collectors.toSet());

        eventOrRelationLines = eventOrRelationLines.stream()
                .filter(x -> retainedAllIds.containsAll(getBratIdSet(x)))
                .collect(Collectors.toList());
        retainedAllIds.addAll(eventOrRelationLines.stream().map(l -> l.split("\t")[0]).collect(Collectors.toList()));

//        relationLines = relationLines.stream()
//                .filter(x -> retainedAllIds.containsAll(getBratIdSet(x)))
//                .collect(Collectors.toList());
//        retainedAllIds.addAll(relationLines.stream().map(l -> l.split("\t")[0]).collect(Collectors.toList()));

        noteOrAttrLines = noteOrAttrLines.stream()
                .filter(x -> retainedAllIds.containsAll(getBratIdSet(x)))
                .collect(Collectors.toList());

        List<String> newAnnLines = retainedEntities.stream().map(BratEntity::toString).collect(Collectors.toList());
        newAnnLines.addAll(noteOrAttrLines);
        newAnnLines.addAll(eventOrRelationLines);
//        newAnnLines.addAll(eventLines);
//        newAnnLines.addAll(relationLines);

        FileUtil.writeFile(outTextPath, retainedTextLines);
        FileUtil.writeFile(outAnnPath, newAnnLines);
        FileUtil.writeFile("data/msra_data/removed/"+documentName+".txt", removedTextLines);

    }

    private static void func_1() {
        String inDir = "data/msra_data/processed_v1";
        String outDir = "data/msra_data/processed_v2";
        removeLinesByIndex(inDir, outDir, "0000", Arrays.asList(16, 33, 34, 76, 77, 81, 86));
        removeLinesByIndex(inDir, outDir, "0001", Arrays.asList(1, 4, 20, 28, 50, 67));
        removeLinesByIndex(inDir, outDir, "0002", Arrays.asList(3, 4, 12, 14, 37, 45, 53, 54, 97, 98, 108, 113,115));
    }

    public static void main(String [] args) {
        func_1();

    }

}
