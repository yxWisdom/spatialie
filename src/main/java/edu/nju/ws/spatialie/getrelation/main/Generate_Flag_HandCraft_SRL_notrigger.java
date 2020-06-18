package edu.nju.ws.spatialie.getrelation.main;

import edu.nju.ws.spatialie.Link.LINK;
import edu.nju.ws.spatialie.Link.OTLINK;
import edu.nju.ws.spatialie.data.BratDocumentwithList;
import edu.nju.ws.spatialie.data.BratEntity;
import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.getrelation.*;
import edu.nju.ws.spatialie.utils.FileUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

public class Generate_Flag_HandCraft_SRL_notrigger {
    static String inputdir = "data/SpaceEval2015/processed_data/SRL/NoTriggerLink/";
    static String outputdir = inputdir.replaceFirst("data", "output");
    static String filename = "train.txt";
    static String confidencefile = "confidenceoutput.txt";

    static private void generateCorpus(String filepath) throws CloneNotSupportedException {
        NLPUtil.init();

        List<OTLINK> otlinkList = new ArrayList<>();
        List<LINK> linkList = new ArrayList<>();
        EvaluationCount count_all = new EvaluationCount();

        List<String> lines = FileUtil.readLines(filepath);

        List<String> output = new ArrayList<>();
        List<String> confidence_output = new ArrayList<>();

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
            if (samesentences.size()<=1) continue;
//            if (line.split("\t")[1].substring(0,5).compareTo("N")<0) continue;
//
//            line ="-1 -1\tPeru Monday , October 16th , 2006 As I said in the last entry , Peru ’s coast is home to one of the driest deserts in the world .\tO O O O O O O O B-SPATIAL_ENTITY O O O O O O O O B-PATH O O O B-PLACE O O O B-PLACE B-SPATIAL_SIGNAL O B-PLACE O\tO O O O O O O O O O O O O O O O O B-landmark O O O B-trajector O O O O O O O O\n";
//
//            samesentences.clear();
//            samesentences.addAll(Arrays.asList(line.split("\n")));

//            System.out.println(line);

            for (String s:samesentences){
                BratDocumentwithList bratDocument = new BratDocumentwithList(s);
                System.out.println(bratDocument.getContent());
                for (BratEvent event : bratDocument.getEventMap().values()) {
                    for (String role : event.getRoleMap().keySet()) {
                        String eid = event.getRoleId(role);
                        BratEntity entity = event.getEntities().get(eid);
                        System.out.print(role + ":" + entity.getText() + "\t");
                    }
                    System.out.println();
                    String str=new Scanner(System.in).nextLine();
                    int pos = -1;
                    while (true) {
                        pos = bratDocument.getContent().indexOf(str, pos + 1);
                        if (pos != -1) {
                            int begin = pos > 10 ? pos - 10 : 0;
                            int end = pos + 10 > bratDocument.getContent().length() ? bratDocument.getContent().length() : pos + 10;
                            System.out.println("neighbors:" + bratDocument.getContent().substring(begin, end));
                            String str2=new Scanner(System.in).nextLine();
                            if (str2.equals("y"))
                                break;
                            else {
                                if (str2.equals("n")) {
                                    continue;
                                } else {
                                    pos = -1;
                                    str = str2;
                                }
                            }
                        } else {
                            Scanner scan2 = new Scanner(System.in);
                            String str2 = null;
                            if (scan2.hasNext()) {
                                str2 = scan2.next();
                            }
                            pos = -1;
                            str = str2;
                        }
                    }
                    BratEntity e = new BratEntity(bratDocument, str, "SpatialFlag", pos, pos + str.length());
                    List<BratEvent> eventList = new ArrayList<>();
                    BratEvent event_new = new BratEvent();
                    event_new.addEntity(e);
                    event_new.addRole("flag", e.getId());
                    eventList.add(event_new);
                    String res = addFlag(s, bratDocument, eventList.get(0));
                    FileUtil.writeFile("output/multiFlagLink/" + filename, res, true);
                    eventList.clear();
                }
            }
//            if (bratDocument.getContent().charAt(0)<'D') continue;


        }
    }

    private static String addFlag(String originline, BratDocumentwithList bratDocument, BratEvent event) {
        String content = bratDocument.getContent();
        String[] tags = originline.split("\t")[2].split(" ");
        String[] labels = originline.split("\t")[3].split(" ");
        BratEntity e = event.getEntities().get(event.getRoleId("flag"));
//        System.out.println(e);
//        System.out.println(content);
        int start = e.getStart();
        int p = 0, i = 0;

        while (true) {
            if (p == start) break;
            p = content.indexOf(' ', p) + 1;
            i++;
//            System.out.println(p);
        }
        int begini = i;
        while (true) {
//            System.out.print(p);
            if (p == e.getEnd()) break;
            p = content.indexOf(' ', p + 1);
            if (p == -1) {
                p = content.length();
            }
            i++;
        }
        int endi = begini+e.getText().split(" ").length-1;
        String res = begini + " " + endi + "\t" + content;
        for (i = 0; i < tags.length; i++) {
            if (i == begini) {
                tags[i] = "B-" + e.getTag();
            } else if (i > begini && i < endi) {
                tags[i] = "I-" + e.getTag();
            }
        }
        for (i = 0; i < labels.length; i++) {
            if (i == begini) {
                labels[i] = "B-flag";
            } else if (i > begini && i < endi) {
                labels[i] = "I-flag";
            }
        }
        res = res + "\t" + StringUtils.join(tags, " ") + "\t" + StringUtils.join(labels, " ");
        return res;
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
            String confidence;
            if (event_ == null) {
                confidence = "0";
            } else {
                if (WordData.getConfidenceMap().get(event_.getRuleid()) != null) {
                    confidence = WordData.getConfidenceMap().get(event_.getRuleid());
                } else {
                    confidence = "-1";
                }
            }
            String res = confidence + "\n";
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
                            //特殊情况：connect
                            // trajector connect landmark-> connects trajector and landmark
                            if (bratDocument.getCompanyMap().get(id).size() >= 1) {
                                if (event.getRoleId("trigger") != null && event.getEntities().get(event.getRoleId("trigger")) != null) {
                                    if (event.getEntities().get(event.getRoleId("trigger")).getText().toLowerCase().contains("connect")) {
                                        for (String companyid : bratDocument.getCompanyMap().get(id)) {
                                            newe.removeEntity(newe.getRoleId("landmark"));
                                            newe.removeRole("landmark");
                                            newe.removeEntity(newe.getRoleId("trajector"));
                                            newe.removeRole("trajector");
                                            newe.addRole("landmark", companyid);
                                            newe.addEntity(bratDocument.getEntitybyID(companyid));
                                            newe.addRole("trajector", id);
                                            newe.addEntity(bratDocument.getEntitybyID(id));
                                        }
                                        continue;
                                    }
                                }
                            }
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
