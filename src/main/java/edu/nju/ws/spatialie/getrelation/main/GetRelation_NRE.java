package edu.nju.ws.spatialie.getrelation.main;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import edu.nju.ws.spatialie.Link.DLINK;
import edu.nju.ws.spatialie.Link.LINK;
import edu.nju.ws.spatialie.Link.MLINK;
import edu.nju.ws.spatialie.Link.OTLINK;
import edu.nju.ws.spatialie.data.BratUtil;
import edu.nju.ws.spatialie.data.BratDocumentwithList;
import edu.nju.ws.spatialie.data.BratEntity;
import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.getrelation.*;
import edu.nju.ws.spatialie.utils.FileUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GetRelation_NRE {
    static String inputdir = "data/SpaceEval2015/processed_data/openNRE/AllLink_12_5/";
    static String outputdir = inputdir.replaceFirst("data","output");
    static String filename = "train.txt";

    static private void generateCorpus(String filepath) throws CloneNotSupportedException {
        NLPUtil.init();

        List<DLINK> dlinkList = new ArrayList<>();
        List<OTLINK> otlinkList = new ArrayList<>();
        List<MLINK> mlinkList = new ArrayList<>();
        List<LINK> linkList = new ArrayList<>();
        EvaluationCount count_all = new EvaluationCount();

        List<String> lines = FileUtil.readLines(filepath);

        String lastcontent = "";
        BratDocumentwithList lastdocument = null;
        BratDocumentwithList bratDocument;
        List<BratEvent> lasteventlist = null;
        List<String> output = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i);

//            line ="{\"t\":{\"pos\":[34,35],\"name\":\"club\"},\"h\":{\"pos\":[24,25],\"name\":\"presentations\"},\"token\":[\"Colon\",\"is\",\"a\",\"city\",\"where\",\"foreigners\",\"are\",\"regularly\",\"held\",\"at\",\"gun\",\"point\",\"in\",\"the\",\"daytime\",\",\",\"and\",\"I\",\"left\",\"the\",\"marina\",\"only\",\"to\",\"give\",\"presentations\",\"at\",\"a\",\"local\",\"school\",\"as\",\"well\",\"as\",\"the\",\"rotary\",\"club\",\".\"],\"relation\":\"None\"}\n";

            JSONObject object = JSONObject.parseObject(line);
            List<String> words = JSON.parseArray(object.getJSONArray("token").toJSONString(), String.class);
            object = FindTagUtil.trimWords(object,words);

            String content = StringUtils.join(words, " ");
            List<BratEvent> eventList;
//                System.out.println(line);
            if (content.equals(lastcontent)) {
                bratDocument = lastdocument;
                eventList = lasteventlist;
            } else {
                lastcontent = content;
//                content = content.replaceAll("approx .","approx ~");
                bratDocument = new BratDocumentwithList(object, content);

                lastdocument = bratDocument;


                JudgeEntity.init(bratDocument);
//            System.out.println(line);
//            System.out.println(1);
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


                eventList = getTranslate(linkList, filepath, bratDocument);
                linkList.clear();

                eventList = Combinecompany(eventList, bratDocument);
                lasteventlist = eventList;
            }
            if (bratDocument.getParseTree()==null) continue;
            EvaluationCount evel = new EvaluationCount();
            String predict_res = EveluateUtil.eveluate_NRE_all(bratDocument, object, eventList, evel,true);

//            if (bratDocument.getTrigger()==null){
//                if (evel.recall() == 1) evel.predict=evel.gold;
//            }

            count_all.add(evel);
            if (evel.precision()!=1&&evel.allPredict()!=0) {
//                if (bratDocument.getContent().contains(object.getJSONObject("h").getString("name")+" of "+object.getJSONObject("t").getString("name")))
//                    System.out.println(object.getJSONObject("h").getString("name"));
                    System.out.println(lines.get(i));

                    //规则标记
//                    for (String key:evel.predict.keySet()){
//                        if (key.contains("LINK")||key.contains("NO")) continue;
//                        System.out.println(key);
//                    }

//                System.out.println("landmark:"+bratDocument.getEntitybyID(bratDocument.getEventMap().get("A1").getRoleId("landmark")).getText());
//                System.out.println("Trajector:"+bratDocument.getEntitybyID(bratDocument.getEventMap().get("A1").getRoleId("trajector")).getText());
//                System.out.println();
            }
            object.put("relation_predict",predict_res);
            output.add(object.toJSONString());
            if (i % 100 == 0) {
                System.out.println(count_all);
                FileUtil.writeFile(outputdir+filename,output,true);
                output.clear();
            }
        }
        System.out.println(count_all);
        FileUtil.writeFile(outputdir+filename,output,true);
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
        generateCorpus(inputdir+filename);
    }
}
