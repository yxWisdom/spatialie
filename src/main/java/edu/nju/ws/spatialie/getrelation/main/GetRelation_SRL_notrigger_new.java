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

public class GetRelation_SRL_notrigger_new {
    static String inputdir = "data/SpaceEval2015/processed_data/SRL/NoTriggerLink/";
    static String outputdir = inputdir.replaceFirst("data", "output");
    static String filename = "test.txt";
    static String confidencefile="confidenceoutput.txt";

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

        List<String> answer = FileUtil.readLines("output/FlagLink/"+filename);

        for (int i = 0; i < lines.size(); i++) {
            List<String> samesentences = new ArrayList<>();
            String line = lines.get(i);
            samesentences.add(line);
            while (i + 1 < lines.size() && lines.get(i + 1).split("\t")[1].equals(line.split("\t")[1])) {
                i++;
                samesentences.add(lines.get(i));
            }
//            if (samesentences.size()==1) continue;

//            line ="-1 -1\tMy approach was quite complicated ; I detoured to the top of the hill to a place with a wayside cross and some nice benches .\tO B-MOTION O O O O B-SPATIAL_ENTITY B-MOTION B-MOTION_SIGNAL O B-PLACE O O B-PLACE B-MOTION_SIGNAL O B-PLACE O O O B-SPATIAL_ENTITY O O O B-SPATIAL_ENTITY O\tO O O O O O O O O O B-trajector O O B-landmark O O O O O O O O O O O O\n" +
//                    "-1 -1\tMy approach was quite complicated ; I detoured to the top of the hill to a place with a wayside cross and some nice benches .\tO B-MOTION O O O O B-SPATIAL_ENTITY B-MOTION B-MOTION_SIGNAL O B-PLACE O O B-PLACE B-MOTION_SIGNAL O B-PLACE O O O B-SPATIAL_ENTITY O O O B-SPATIAL_ENTITY O\tO O O O O O O O O O O O O O O O B-landmark O O O B-trajector O O O O O\n" +
//                    "-1 -1\tMy approach was quite complicated ; I detoured to the top of the hill to a place with a wayside cross and some nice benches .\tO B-MOTION O O O O B-SPATIAL_ENTITY B-MOTION B-MOTION_SIGNAL O B-PLACE O O B-PLACE B-MOTION_SIGNAL O B-PLACE O O O B-SPATIAL_ENTITY O O O B-SPATIAL_ENTITY O\tO O O O O O O O O O O O O O O O B-landmark O O O O O O O B-trajector O\n";
//
//            samesentences.clear();
//            samesentences.addAll(Arrays.asList(line.split("\n")));

//            System.out.println(line);
            BratDocumentwithList bratDocument = new BratDocumentwithList(samesentences);
            JudgeEntity.init(bratDocument);

            bratDocument.dealCompany();
            while (true) {
                LINK link;
                boolean f = false;
                for (int level = 0;level<2;level++) {
                    for (int k = bratDocument.getEntityList().size() - 1; k >= 0; k--) {
                        List<OTLINK> links = FindOTLINK_notrigger.findOTLINKwithoutTrigger(bratDocument, k, linkList,level);
                        if (links != null) {
                            for (OTLINK l : links) {
                                linkList.add(l);
                            }
                            if (links.size() > 0) f = true;
                        }
                    }
                }
                if (!f) {
                   break;
                }
            }

            otlinkList.clear();


            EvaluationCount evel = new EvaluationCount();
            List<BratEvent> eventList = getTranslate(linkList, filepath, bratDocument);
            linkList.clear();

            eventList = Combinecompany(eventList, bratDocument);
            eventList = splitEvent(eventList);
            eventList = EveluateUtil.removeRedundancy_notrigger(eventList, bratDocument,"");


            eventList = FindOTLINK_notrigger.findPossibleFlags(bratDocument,eventList,samesentences.size()-eventList.size());
            eventList = FindOTLINK_notrigger.getTop(bratDocument.getEventMap().size(),eventList);
            if (eventList.size()<samesentences.size()){
                eventList = FindOTLINK_notrigger.randomFlags(bratDocument,eventList,samesentences.size()-eventList.size());
            }


            //一个句子对应k个gold关系，则截取可信度最高的前k个关系
//            eventList = FindOTLINK_notrigger.getTop(bratDocument.getEventMap().size(),eventList);
//
//            if (eventList.size()<samesentences.size()){
//                eventList = FindOTLINK_notrigger.findPossibleFlags(bratDocument,eventList,samesentences.size()-eventList.size());
//                if (eventList.size()<samesentences.size()){
//                    eventList = FindOTLINK_notrigger.randomFlags(bratDocument,eventList,samesentences.size()-eventList.size());
//                }
//            }

//
            for (BratEntity e:bratDocument.getEntityList()){
                e.setEnd(e.getStart()+e.getText().length());
            }

            List<BratEvent> candidateevents1 = new ArrayList<>();
            List<BratEvent> candidateevents2 = new ArrayList<>();
            int similar = 2;
            while (similar>0){
                for (BratEvent event1 : bratDocument.getEventMap().values()) {
                    if (candidateevents1.contains(event1)) continue;
                    for (BratEvent event2 : eventList) {
                        if (candidateevents2.contains(event2)) continue;
                        int similarity = EveluateUtil.compareSimilarity(event1,event2);
                        if (similarity==similar){
                            candidateevents1.add(event1);
                            candidateevents2.add(event2);
                            break;
                        }
                    }
                }
                if (candidateevents1.size()==bratDocument.getEventMap().size()) break;
                similar--;
            }
            if (candidateevents1.size()<bratDocument.getEventMap().size()){
                for (BratEvent event1 : bratDocument.getEventMap().values()) {
                    if (candidateevents1.contains(event1)) continue;
                    for (BratEvent event2 : eventList) {
                        if (candidateevents2.contains(event2)) continue;
                        candidateevents1.add(event1);
                        candidateevents2.add(event2);
                    }
                }
            }
            for (int idx = 0;idx<candidateevents1.size();idx++){
                BratEvent e = candidateevents1.get(idx);
                for (String s:samesentences) {
                    String trajector = getRole(s.split("\t")[1],s.split("\t")[3],"trajector");
                    String landmark = getRole(s.split("\t")[1],s.split("\t")[3],"landmark");
                    BratEntity t = e.getEntities().get(e.getRoleId("trajector"));
                    BratEntity l = e.getEntities().get(e.getRoleId("landmark"));
                    if (trajector.equals(t.getText())&&landmark.equals(l.getText())) {
                        String res = addFlag(s, bratDocument, candidateevents2.get(idx), true);
                        FileUtil.writeFile("output/NoTriggerLink/"+filename,res,true);
                        break;
                    }
                }
                //                    FileUtil.writeFile("output/NoTriggerLink/test.txt",res,true);
            }

//            for (String s:samesentences){
//                for (BratEvent event:eventList){
//                    String res = addFlag(s,bratDocument,event,true);
////                    FileUtil.writeFile("output/NoTriggerLink/test.txt",res,true);
//                }
//            }
//
//            if (samesentences.size()==1){
//                String res = addFlag(samesentences.get(0),bratDocument,eventList.get(0),true);
//                FileUtil.writeFile("output/NoTriggerLink/"+filename,res,true);
//            }

//            if (eventList.get(0).getRuleid().equals("NT3")) {
                System.out.println(bratDocument.getContent());
                for (String s : answer) {
                    if (s.contains(bratDocument.getContent())) {
                        int begin = Integer.parseInt(s.split("\t")[0].split(" ")[0]);
                        int end = Integer.parseInt(s.split("\t")[0].split(" ")[0]);
                        List<String> flags = Arrays.asList(bratDocument.getContent().split(" ")).subList(begin, end + 1);
                        String flag = StringUtils.join(flags, " ");
                        System.out.print("flag:[" + flag + "]\t");
                        break;
                    }
                }
                for (BratEvent e : bratDocument.getEventMap().values()) {
                    for (String role : e.getRoleMap().keySet()) {
                        System.out.print(role+":[");
                        for (String roleid:e.getRoleIds(role)){
                            BratEntity entity = e.getEntities().get(roleid);
                            System.out.print(entity.getText());
                        }
                        System.out.print("]\t");
                    }
                    System.out.println();
                }
                System.out.println("-------");
                for (BratEvent e : eventList) {
                    for (String role : e.getRoleMap().keySet()) {
                        System.out.print(role+":[");
                        for (String roleid:e.getRoleIds(role)){
                            BratEntity entity = e.getEntities().get(roleid);
                            System.out.print(entity.getText() );
                        }
                        System.out.print("]\t");
                    }
                    System.out.println(e.getRuleid());
                }
                if (eventList.get(0).getRoleId("trajector").equals(bratDocument.getEventMap().get("A0").getRoleId("landmark"))) {
                    System.out.println("WTF");
                }
                System.out.println();
//            }


//            if (eventList.size()<samesentences.size()) {
//
//            }



        }
    }

    private static String getRole(String content, String label, String l) {
        String[] words =content.split(" ");
        String[] labels = label.split(" ");
        String res = "";
        for (int i = 0;i<words.length;i++){
            if (labels[i].endsWith(l)) res = res+" "+words[i];
        }
        return res.substring(1);
    }

    private static List<BratEvent> splitEvent(List<BratEvent> eventList) throws CloneNotSupportedException {
        List<BratEvent> res = new ArrayList<>();
        for (BratEvent e:eventList){
            int count = countEvent(e);
            if (count>1){
                String multirole = "";
                List<String> multiroleids = new ArrayList<>();
                for (String role:e.getRoleMap().keySet()){
                    Collection<String> roleids = e.getRoleIds(role);
                    if (roleids.size()>1) {
                        multirole = role;
                        for (String roleid:roleids){
                            multiroleids.add(roleid);
                        }
                        break;
                    }
                }
                List<BratEvent> newes = new ArrayList<>();
                for (int i = 0;i<count;i++){
                    BratEvent newe = e.clone();
                    newe.removeRole(multirole);
                    newe.addRole(multirole,multiroleids.get(i));
                    for (String multiroleid:multiroleids){
                        if (!multiroleid.equals(multiroleids.get(i))){
                            newe.removeEntity(multiroleid);
                        }
                    }
                    newes.add(newe);
                }
                res.addAll(newes);
            } else {
                res.add(e);
            }
        }
        return res;
    }

    private static int countEvent(BratEvent e) {
        for (String role:e.getRoleMap().keySet()){
            Collection<String> roleids = e.getRoleIds(role);
            if (roleids.size()>1) return roleids.size();
        }
        return 1;
    }

    private static String addFlag(String originline, BratDocumentwithList bratDocument, BratEvent event,boolean rulelabel) {
        String content = bratDocument.getContent();
        String[] tags = originline.split("\t")[2].split(" ");
        String[] labels = originline.split("\t")[3].split(" ");
        BratEntity e = event.getEntities().get(event.getRoleId("flag"));
//        System.out.println(e);
//        System.out.println(content);
        int start = e.getStart();
        int p = 0,i=0;

        while (true){
            if (p==start) break;
            p = content.indexOf(' ',p)+1;
            i++;
//            System.out.println(p);
        }
        int begini = i;
        while (true){
//            System.out.print(p);
            if (p==e.getEnd()) break;
            p = content.indexOf(' ',p+1);
            if (p==-1){
                p = content.length();
            }
            i++;
        }
        int endi = i-1;
        String res = begini+" "+endi+"\t"+content;
        for (i = 0;i<tags.length;i++){
            if (i==begini){
                tags[i]="B-"+e.getTag();
            } else if (i>begini&&i<endi+1){
                tags[i]="I-"+e.getTag();
            }
        }
        for (i = 0;i<labels.length;i++){
            if (i==begini){
                labels[i]="B-flag";
            } else if (i>begini&&i<endi+1){
                labels[i]="I-flag";
            }
        }
        res = res+"\t"+ StringUtils.join(tags, " ");
        if (rulelabel){
            int pos = 0;
            String[] words = content.split(" ");
            String inE = null;
            BratEntity t = null;
            List<String> rulelabels = new ArrayList<>();
            for (String w:words){
                String label = "O";
                if (inE!=null){
//                    BratEntity entity = event.getEntities().get(event.getRoleId(inE));
                    if (pos>=t.getStart()&&pos<t.getEnd()){
                        label="I-"+inE;
                    } else {
                        inE=null;
                        t = null;
                    }
                }
                if (inE==null){
                    for (String role:event.getRoleMap().keySet()){
                        Collection<String> roleids = event.getRoleIds(role);
                        for (String roleid:roleids){
                            BratEntity entity = event.getEntities().get(roleid);
                            if (pos>=entity.getStart()&&pos<entity.getEnd()){
                                label="B-"+role;
                                inE = role;
                                t = entity;
                            }
                        }

                    }
                }
                pos +=w.length()+1;
                rulelabels.add(label);
            }
            res = res+"\t"+StringUtils.join(rulelabels, " ")+"\t"+StringUtils.join(labels, " ");
        } else{
            res = res+"\t"+StringUtils.join(labels, " ");
        }
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
            if (event_==null){
                confidence="0";
            } else {
                if (WordData.getConfidenceMap().get(event_.getRuleid()) != null) {
                    confidence = WordData.getConfidenceMap().get(event_.getRuleid());
                } else {
                    confidence = "-1";
                }
            }
            String res = confidence+"\n";
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
                            if (bratDocument.getCompanyMap().get(id).size()>=1){
                                if (event.getRoleId("trigger")!=null&&event.getEntities().get(event.getRoleId("trigger"))!=null) {
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
