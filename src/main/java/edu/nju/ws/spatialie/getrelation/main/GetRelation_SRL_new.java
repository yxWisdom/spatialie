package edu.nju.ws.spatialie.getrelation.main;

import edu.nju.ws.spatialie.Link.DLINK;
import edu.nju.ws.spatialie.Link.LINK;
import edu.nju.ws.spatialie.Link.MLINK;
import edu.nju.ws.spatialie.Link.OTLINK;
import edu.nju.ws.spatialie.annprocess.BratUtil;
import edu.nju.ws.spatialie.data.BratDocumentwithList;
import edu.nju.ws.spatialie.data.BratEntity;
import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.getrelation.*;
import edu.nju.ws.spatialie.utils.FileUtil;

import java.io.File;
import java.util.*;

public class GetRelation_SRL_new {
    static String inputdir = "data/SpaceEval2015/processed_data/SRL/DLink/";
    static String outputdir = inputdir.replaceFirst("data", "output");
    static String filename = "train.txt";

    static private void generateCorpus(String filepath) throws CloneNotSupportedException {
        NLPUtil.init();

        List<DLINK> dlinkList = new ArrayList<>();
        List<OTLINK> otlinkList = new ArrayList<>();
        List<MLINK> mlinkList = new ArrayList<>();
        List<LINK> linkList = new ArrayList<>();
        EvaluationCount count_all = new EvaluationCount();

        List<String> lines = FileUtil.readLines(filepath);

        List<String> output = new ArrayList<>();

        UserComparator comparator = new UserComparator();
        lines.sort(comparator);

        for (int i = 0; i < lines.size(); i++) {
            List<String> samesentences = new ArrayList<>();
            String line = lines.get(i);
            samesentences.add(line);
            while (i + 1 < lines.size() && lines.get(i + 1).split("\t")[1].equals(line.split("\t")[1])) {
                i++;
                samesentences.add(lines.get(i));
            }

//            line ="28 28\tThe Andean Paramo – the future of the mountaintops Monday , June 19th , 2006 Before biking out of Bogota , a young professor from a local university took me into the mountains that overlook the city .\tO O O O O O O O O O O O O O O O B-MOTION B-MOTION_SIGNAL I-MOTION_SIGNAL B-PLACE O O O B-SPATIAL_ENTITY O O O O B-MOTION B-SPATIAL_ENTITY B-MOTION_SIGNAL O B-PLACE O B-SPATIAL_SIGNAL O B-PLACE O\tO O O O O O O O O O O O O O O O O O O O O O O B-mover O O O O B-trigger O O O O O O O O O\n" +
//                    "34 34\tThe Andean Paramo – the future of the mountaintops Monday , June 19th , 2006 Before biking out of Bogota , a young professor from a local university took me into the mountains that overlook the city .\tO O O O O O O O O O O O O O O O B-MOTION B-MOTION_SIGNAL I-MOTION_SIGNAL B-PLACE O O O B-SPATIAL_ENTITY O O O O B-MOTION B-SPATIAL_ENTITY B-MOTION_SIGNAL O B-PLACE O B-SPATIAL_SIGNAL O B-PLACE O\tO O O O O O O O O O O O O O O O O O O O O O O O O O O O O O O O B-trajector O B-trigger O B-landmark O\n" +
//                    "16 16\tThe Andean Paramo – the future of the mountaintops Monday , June 19th , 2006 Before biking out of Bogota , a young professor from a local university took me into the mountains that overlook the city .\tO O O O O O O O O O O O O O O O B-MOTION B-MOTION_SIGNAL I-MOTION_SIGNAL B-PLACE O O O B-SPATIAL_ENTITY O O O O B-MOTION B-SPATIAL_ENTITY B-MOTION_SIGNAL O B-PLACE O B-SPATIAL_SIGNAL O B-PLACE O\tO O O O O O O O O O O O O O O O B-trigger O O O O O O O O O O O O B-mover O O O O O O O O\n";
//
//            samesentences.clear();
//            samesentences.addAll(Arrays.asList(line.split("\n")));

//            System.out.println(line);
            BratDocumentwithList bratDocument = new BratDocumentwithList(samesentences);
            JudgeEntity.init(bratDocument);
//            if (true) continue;

            bratDocument.dealCompany();
            int idx = 0;
            for (BratEntity entity : bratDocument.getEntityList()) {
                if (bratDocument.getIsCandidate(idx)) {
                    switch (entity.getTag()) {
                        case BratUtil.MOTION:
                            mlinkList.add(new MLINK(idx));
                            break;
                        case BratUtil.MEASURE:
                            dlinkList.add(new DLINK(idx));
                            break;
                        case BratUtil.SPATIAL_SIGNAL:
                            otlinkList.add(new OTLINK(idx));
                            break;
                    }
                }
                idx++;
            }

            Collections.reverse(dlinkList);
            Collections.reverse(otlinkList);
            Collections.reverse(mlinkList);

            int level = 0;
            while (level <= 5) {
                LINK link;
                boolean f = false;

                if (level == 4) {
                    Collections.reverse(dlinkList);
                    Collections.reverse(otlinkList);
                    Collections.reverse(mlinkList);
                }

                for (int k = bratDocument.getEntityList().size() - 1; k >= 0; k--) {
                    if ((link = FindOTLINK.findOTLINKwithoutTrigger(bratDocument, level, k)) != null) {
                        boolean fl = false;
                        for (LINK link_t : linkList) {
                            if (link_t.getClass().getSimpleName().equals("OTLINK")) {
                                OTLINK otlink_t = (OTLINK) link_t;
                                if (otlink_t.getTrigger() == -1) {
                                    OTLINK otlink = (OTLINK) link;
                                    if (otlink.getLandmarks().equals(otlink_t.getLandmarks()) && otlink.getTrajectors().equals(otlink_t.getTrajectors())) {
                                        fl = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (fl) continue;
                        linkList.add(link);
                        f = true;
                    }
                }
                if (f) {
                    level = 0;
                    continue;
                }

                // 同时识别一些和otlink搅在一起的dlink
                if ((link = FindOTLINK.findOTLINK(bratDocument, otlinkList, level, dlinkList)) != null) {
                    linkList.add(link);
                    level = 0;
                    continue;
                }
                if ((link = FindDLINK.findDLINK(bratDocument, dlinkList, level)) != null) {
//                    linkList.add(link);
                    level = 0;
                    continue;
                }
                if ((link = FindMLINK.findMLINK(bratDocument, mlinkList, level)) != null) {
                    linkList.add(link);
                    level = 0;
                    continue;
                }
                level++;
            }

//            addMatchlink(linkList,(List<LINK>)(Object)otlinkList);
//            addMatchlink(linkList,(List<LINK>)(Object)mlinkList);
            addMatchlink(linkList, (List<LINK>) (Object) dlinkList);

            otlinkList.clear();
            mlinkList.clear();
            dlinkList.clear();


            EvaluationCount evel = new EvaluationCount();
            List<BratEvent> eventList = getTranslate(linkList, filepath, bratDocument);
            linkList.clear();

            eventList = Combinecompany(eventList, bratDocument);

//            if (bratDocument.getTrigger()!=null)
//                eventList = EveluateUtil.removeRedundancy(eventList,bratDocument);
//            else
//                eventList = EveluateUtil.removeRedundancy_notrigger(eventList,bratDocument);
            eventList = EveluateUtil.removeRedundancy_notrigger(eventList, bratDocument);
            EveluateUtil.eveluate(bratDocument, eventList, evel);

//            if (bratDocument.getTrigger()==null){
//                if (evel.recall() == 1) evel.predict=evel.gold;
//            }

            count_all.add(evel);
            if (evel.precision() != 1&&eventList.size()!=0) {
                for (String line_ : samesentences) {
                    System.out.println(line_);

                }
                System.out.println(evel+"\n");
//                System.out.println("landmark:"+bratDocument.getEntitybyID(bratDocument.getEventMap().get("A0").getRoleId("landmark")).getText());
//                System.out.println("Trajector:"+bratDocument.getEntitybyID(bratDocument.getEventMap().get("A0").getRoleId("trajector")).getText());
//                System.out.println();
//                for (BratEvent e:eventList){
//                    if (e.getRuleid().equals("NT13")) {
//                        System.out.println("landmark:" + e.getEntities().get(e.getRoleId("landmark")).getText());
//                        System.out.println("trajector:" + e.getEntities().get(e.getRoleId("trajector")).getText());
//                    }
//                }
            }

            for (String line_ : samesentences) {
                String res = buildtags(line_, eventList, bratDocument);
                output.add(res);
            }

            FileUtil.writeFile(outputdir + filename, output, true);
            output.clear();

        }
        System.out.println(count_all);
//        FileUtil.writeFile(outputdir + filename, output, true);
    }

    public static class UserComparator implements Comparator<String> {
        @Override
        public int compare(String u1, String u2) {
            String s1 = u1.split("\t")[1];
            String s2 = u2.split("\t")[1];
            return s1.compareTo(s2);
        }
    }

    private static String buildtags(String line, List<BratEvent> eventList, BratDocumentwithList bratDocument) {
        String[] texts = line.split("\t");
        String text = texts[1];
        String[] tags = texts[2].split(" ");
        String[] labels = texts[3].split(" ");
        int trigger_idx = Integer.valueOf(texts[0].split(" ")[0]);
        int pos = 0;
        String[] words = text.split(" ");
        if (trigger_idx != -1) {
            for (int i = 0; i < trigger_idx; i++) {
                pos = pos + words[i].length() + 1;
            }
            BratEvent event_ = null;
            for (BratEvent event : eventList) {
                if (event.getRoleIds("trigger").size() > 0) {
                    if (event.getEntities().get(event.getRoleId("trigger")).getStart() == pos) {
                        event_ = event;
                        break;
                    }
                }
                if (event.getRoleIds("val").size() > 0) {
                    if (event.getEntities().get(event.getRoleId("val")).getStart() == pos) {
                        event_ = event;
                        break;
                    }
                }
            }
            boolean inlabel = false;
            String res = "";
            String label = null;
            int end = 0;
            pos = 0;
            for (int i = 0; i < words.length; i++) {
                boolean newlabel = false;
                if (!inlabel && event_ != null) {
                    for (String role : event_.getRoleMap().keySet()) {
                        for (String id : event_.getRoleIds(role)) {
                            int p = event_.getEntities().get(id).getStart();
                            if (p == pos) {
                                inlabel = true;
                                newlabel = true;
                                end = event_.getEntities().get(id).getEnd();
                                label = role;
                            }
                        }
                    }
                }
                String predict;
                if (!inlabel)
                    predict = "O";
                else {
                    if (label.equals("val"))
                        predict = "trigger";
                    else
                        predict = label;
                    if (newlabel)
                        predict = "B-" + predict;
                    else
                        predict = "I-" + predict;
                }
                res = res + words[i] + " " + tags[i] + " " + labels[i] + " " + predict + "\n";
                pos = pos + words[i].length() + 1;
                if (inlabel) {
                    if (pos >= end) {
                        inlabel = false;
                    }
                }
            }
            return res;
        } else {
            boolean inlabel = false;
            String res = "";
            String label = null;
            int end = 0;
            pos = 0;
            for (int i = 0; i < words.length; i++) {
                boolean newlabel = false;
                if (!inlabel) {
                    for (BratEvent event_ : eventList) {
                        for (String role : event_.getRoleMap().keySet()) {
                            for (String id : event_.getRoleIds(role)) {
                                int p = event_.getEntities().get(id).getStart();
                                if (p == pos) {
                                    inlabel = true;
                                    newlabel = true;
                                    end = event_.getEntities().get(id).getEnd();
                                    label = role;
                                }
                            }
                        }
                    }
                }
                String predict;
                if (!inlabel)
                    predict = "O";
                else {
                    if (label.equals("val"))
                        predict = "trigger";
                    else
                        predict = label;
                    if (newlabel)
                        predict = "B-" + predict;
                    else
                        predict = "I-" + predict;
                }
                res = res + words[i] + " " + tags[i] + " " + labels[i] + " " + predict + "\n";
                pos = pos + words[i].length() + 1;
                if (inlabel) {
                    if (pos >= end) {
                        inlabel = false;
                    }
                }
            }
            return res;
        }

    }

    private static List<BratEvent> Combinecompany(List<BratEvent> eventList, BratDocumentwithList bratDocument) throws CloneNotSupportedException {
        List<BratEvent> newList = new ArrayList<>();
        for (BratEvent event : eventList) {
            BratEvent newe = event.clone();
            for (String role : event.getRoleMap().keySet()) {
//                ArrayList<String> ids = (ArrayList<String>) event.getRoleMap().get(role);
                for (String id : event.getRoleMap().get(role)) {
                    if (bratDocument.getCompanyMap().keySet().contains(id)) {
                        if (role.equals("trigger") || role.equals("val")) {
                            for (String companyid : bratDocument.getCompanyMap().get(id)) {
                                newList.add(newe);
                                newe = newe.clone();
                                newe.removeRole(role, id);
                                newe.removeEntity(id);
                                newe.addRole(role, companyid);
                                newe.addEntity(bratDocument.getEntitybyID(companyid));
                            }
                        } else {
                            for (String companyid : bratDocument.getCompanyMap().get(id)) {
                                newe.addRole(role, companyid);
                                newe.addEntity(bratDocument.getEntitybyID(companyid));
                            }
                        }
                    }
                }
            }
            newList.add(newe);
        }
        return newList;
    }

    private static List<BratEvent> getTranslate(List<LINK> linkList, String filename, BratDocumentwithList bratDocument) {
        List<BratEvent> eventList = new ArrayList<>();
        for (LINK link : linkList) {
            BratEvent event = new BratEvent();
            event.setType(link.getClass().getSimpleName());
            event.setFilename(filename);
            event = TranslateUtil.translateLink(link, event, bratDocument);
            event.setRuleid(link.getRule_id());
            eventList.add(event);
        }
        return eventList;
    }

    private static void addMatchlink(List<LINK> linkList, List<LINK> linkList2) {
        for (LINK link : linkList2) {
            if (link.isIscompleted()) {
                linkList.add(link);
            }
        }
    }

    public static void main(String[] args) throws CloneNotSupportedException {
        File file = new File(outputdir);
        if (!file.exists()) {
            file.mkdirs();
        }
        generateCorpus(inputdir + filename);
    }
}
