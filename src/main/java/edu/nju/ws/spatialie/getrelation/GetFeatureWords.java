package edu.nju.ws.spatialie.getrelation;

import edu.nju.ws.spatialie.annprocess.BratUtil;
import edu.nju.ws.spatialie.data.BratDocument;
import edu.nju.ws.spatialie.data.BratDocumentwithList;
import edu.nju.ws.spatialie.data.BratEntity;
import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.getrelation.main.GetRelation_SRL_notrigger_new;
import edu.nju.ws.spatialie.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GetFeatureWords {
    private static void CountLINKs(String raw_dir){
        File folder = new File(raw_dir);
        File [] files = folder.listFiles();
        assert files != null;
        for (int i=0; i<files.length;i+=2) {
            BratDocument bratDocument = new BratDocument(files[i + 1].getPath(), files[i].getPath());
            List<String> entities = new ArrayList<>();
            for (BratEvent e:bratDocument.getEventMap().values()){
                boolean f = false;
                for (String role:e.getRoleMap().keySet()){
                    if (e.getRoleMap().get(role).size()>1){
                        System.out.println(e.getType());
                        f = true;
                        break;
                    }
                }
                if (f) {
                    System.out.println(e.getSentence());
                    for (String role : e.getRoleMap().keySet()) {
                        System.out.print(role + ":");
                        for (String id : e.getRoleMap().get(role)) {
                            BratEntity entity = e.getEntities().get(id);
                            System.out.print(entity.getText() + " ");
                        }
                        System.out.println();
                    }
                }
            }
//            for (BratEntity e:bratDocument.getEntityMap().values()){
////                if (e.getTag().contains(BratUtil.MOTION_SIGNAL)){
////                    System.out.println(e.getText());
//////                    Set<Integer> s = SpatialSignalAttr.getType_spatial(e);
//////                    if (s!=null&&s.size()==1){
//////                        if (s.contains(2)||s.contains(5)){
//////                            System.out.println(e.getText());
//////                            String sentence = bratDocument.getCompleteSentence(e);
//////                            int[] pos = bratDocument.getCompleteSentencePos(e);
//////                            int begin = e.getStart()-pos[0];
//////                            int end = e.getEnd()-pos[0];
//////                            int y;
//////                            if (s.contains(2)) y = 2; else y = 5;
//////                            corenlp.saveLocalTreeFeatures(sentence,begin,end,y);
//////                        }
//////                    }
////                }
//            }
//            FileUtil.writeFile("data/fenci/entity_words.txt",entities,true);
//            for (Map.Entry<String, BratEvent> eventEntry:bratDocument.getEventMap().entries()){
//                String eventString=eventEntry.getKey();
//                BratEvent event=eventEntry.getValue();
//                String filename=event.getType();
//                if (!filename.equals("MLINK")) continue;
////                System.out.println(event.getSentence());
//                for (String juese:event.getMembers().keySet()){
//                    BratEntity trigger = event.getEntities().get(event.getMembers().get(juese));
//                    String text=trigger.getText();
//                    if (juese.equals("trigger")) {
//                        System.out.print(text+"\t");
//                        for (BratAttribute attr:trigger.getBratAttributes()){
//                            if (attr.getName().equals("motion_type")) System.out.print(attr.getValue()+"\t");
//                        }
//                        for (BratAttribute attr:trigger.getBratAttributes()){
//                            if (attr.getName().equals("motion_class")) System.out.println(attr.getValue());
//                        }
//                    }
//                }
////                for (BratAttribute relation:event.getAttributeMap()){
////                    System.out.println(relation.getValue());
////                }
//
//            }
        }
    }

    public static void CountLINKsinSRL(String filepath){
        NLPUtil.init();
        List<String> lines = FileUtil.readLines(filepath);
        GetRelation_SRL_notrigger_new.UserComparator comparator = new GetRelation_SRL_notrigger_new.UserComparator();
        lines.sort(comparator);
        for (int i = 0; i < lines.size(); i++) {
            List<String> samesentences = new ArrayList<>();
            String line = lines.get(i);
            samesentences.add(line);
            while (i + 1 < lines.size() && lines.get(i + 1).split("\t")[1].equals(line.split("\t")[1])) {
                i++;
                samesentences.add(lines.get(i));
            }
            BratDocumentwithList bratDocument = new BratDocumentwithList(samesentences);
            System.out.println(bratDocument.getContent());
            for (BratEvent e:bratDocument.getEventMap().values()){
                for (String role:e.getRoleMap().keySet()){
                    String eid = e.getRoleId(role);
                    BratEntity entity = e.getEntities().get(eid);
                    System.out.print(role+":"+entity.getText()+"\t");
                }
                System.out.println();
            }
            System.out.println();
        }
    }

    public static void main(String [] args) {
        CountLINKsinSRL("data/SpaceEval2015/processed_data/SRL/NoTriggerLink/train.txt");
//        GetFeatureWords.CountLINKs("data/SpaceEval2015/brat_format_data/train");
//        GetFeatureWords.CountLINKs("data/SpaceEval2015/brat_format_data/test");
    }
}
