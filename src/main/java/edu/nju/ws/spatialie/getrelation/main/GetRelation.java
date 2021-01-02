package edu.nju.ws.spatialie.getrelation.main;

import edu.nju.ws.spatialie.Link.DLINK;
import edu.nju.ws.spatialie.Link.LINK;
import edu.nju.ws.spatialie.Link.MLINK;
import edu.nju.ws.spatialie.Link.OTLINK;
import edu.nju.ws.spatialie.data.BratUtil;
import edu.nju.ws.spatialie.data.BratDocumentwithList;
import edu.nju.ws.spatialie.data.BratEntity;
import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.getrelation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetRelation {
    static private void generateCorpus(String dir) {
        List<File> files;
        File folder = new File(dir);
        File[] fileArray = folder.listFiles();
        files = Arrays.asList(fileArray);
        List<DLINK> dlinkList = new ArrayList<>();
        List<OTLINK> otlinkList = new ArrayList<>();
        List<MLINK> mlinkList = new ArrayList<>();
        List<LINK> linkList = new ArrayList<>();
        EvaluationCount count_all = new EvaluationCount();
        for (int i = 0; i < files.size(); i += 2) {
            System.out.println(files.get(i+1).getName());
            BratDocumentwithList bratDocument = new BratDocumentwithList(files.get(i  + 1).getPath(), files.get(i).getPath());
            bratDocument.dealCompany();
            int idx = 0;
            for (BratEntity entity:bratDocument.getEntityList()){
                switch (entity.getTag()){
                    case BratUtil.MOTION:mlinkList.add(new MLINK(idx));break;
                    case BratUtil.MEASURE:dlinkList.add(new DLINK(idx));bratDocument.noCandidate(idx);break;
                    case BratUtil.SPATIAL_SIGNAL:otlinkList.add(new OTLINK(idx));bratDocument.noCandidate(idx);break;
                }
                idx++;
            }

            int level = 0;
            while (level<=7){
                LINK link;
                boolean f = false;
                for (int k =0;k<bratDocument.getEntityList().size();k++){
                    if ((link = FindOTLINK.findOTLINKwithoutTrigger(bratDocument,level,k))!=null){
                        linkList.add(link);
                        f = true;
                    }
                }
                if (f) continue;

                // 同时识别一些和otlink搅在一起的dlink
                if ((link = FindOTLINK.findOTLINK(bratDocument,otlinkList,level,dlinkList))!=null){
                    linkList.add(link);
                    continue;
                }
                if ((link = FindDLINK.findDLINK(bratDocument,dlinkList,level))!=null){
                    linkList.add(link);
                    continue;
                }
                if ((link = FindMLINK.findMLINK(bratDocument,mlinkList,level))!=null){
                    linkList.add(link);
                    continue;
                }
                level++;
            }

            addMatchlink(linkList,(List<LINK>)(Object)otlinkList);
            addMatchlink(linkList,(List<LINK>)(Object)mlinkList);
            addMatchlink(linkList,(List<LINK>)(Object)dlinkList);

            EvaluationCount evel = new EvaluationCount();
            List<BratEvent> eventList = getTranslate(linkList,dir,bratDocument);

            Combinecompany(eventList);

            EveluateUtil.eveluate(bratDocument,eventList,evel);

            count_all.add(evel);
        }
        System.out.println(count_all);
    }

    private static void Combinecompany(List<BratEvent> eventList) {
    }

    private static List<BratEvent> getTranslate(List<LINK> linkList, String filename, BratDocumentwithList bratDocument) {
        List<BratEvent> eventList = new ArrayList<>();
        for (LINK link:linkList){
            BratEvent event = new BratEvent();
            event.setType(link.getClass().getSimpleName());
            event.setFilename(filename);
            event = TranslateUtil.translateLink(link, event,bratDocument);
            eventList.add(event);
        }
        return eventList;
    }

    private static void addMatchlink(List<LINK> linkList, List<LINK> linkList2) {
        for (LINK link:linkList2){
            if (link.isIscompleted()){
                linkList.add(link);
            }
        }
    }
}
