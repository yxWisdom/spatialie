package edu.nju.ws.spatialie.getrelation;


import edu.nju.ws.spatialie.Link.DLINK;
import edu.nju.ws.spatialie.Link.LINK;
import edu.nju.ws.spatialie.Link.MLINK;
import edu.nju.ws.spatialie.Link.OTLINK;
import edu.nju.ws.spatialie.data.BratDocumentwithList;
import edu.nju.ws.spatialie.data.BratEntity;
import edu.nju.ws.spatialie.data.BratEvent;

import java.util.List;

public class TranslateUtil {

    private static final String MOTION_SIGNALID = "motion_signalID";
    private static final String MOVER = "mover";
    private static final String GROUND = "ground";
    private static final String SOURCE = "source";
    private static final String GOAL = "goal";
    private static final String VAL = "val";
    private static final String TRIGGER = "trigger";
    private static final String LANDMARK = "landmark";
    private static final String TRAJECTOR = "trajector";
    private static final String CONJ = "conj";
    private static final String FLAG = "flag";

    static public BratEvent translateOTlink(OTLINK tlink, BratEvent event, BratDocumentwithList bratDocument){
        List<BratEntity>  entityList = bratDocument.getEntityList();
       if (tlink.getTrigger()!=-1){
           event.setT_start_end(entityList.get(tlink.getTrigger()).getStart(),entityList.get(tlink.getTrigger()).getEnd());
           event.setMembers(TRIGGER,entityList.get(tlink.getTrigger()).getId());
            event.setEntities(entityList.get(tlink.getTrigger()).getId(),entityList.get(tlink.getTrigger()));
        }

        if (tlink.getFlag()!=-1){
            event.setT_start_end(entityList.get(tlink.getFlag()).getStart(),entityList.get(tlink.getFlag()).getEnd());
            event.setMembers(FLAG,entityList.get(tlink.getFlag()).getId());
            event.setEntities(entityList.get(tlink.getFlag()).getId(),entityList.get(tlink.getFlag()));
        }

        for (int idx:tlink.getLandmarks()){
            event.setMembers(LANDMARK,entityList.get(idx).getId());
            event.setEntities(entityList.get(idx).getId(),entityList.get(idx));
        }

        for (int idx:tlink.getTrajectors()){
            event.setMembers(TRAJECTOR,entityList.get(idx).getId());
            event.setEntities(entityList.get(idx).getId(),entityList.get(idx));
        }
        return event;
    }

    public static BratEvent translateLink(LINK link, BratEvent event, BratDocumentwithList bratDocument) {
        if (event.getType().equals("OTLINK")) {
            OTLINK tlink = (OTLINK) link;
            return translateOTlink(tlink, event, bratDocument);
        } else if (event.getType().equals("TLINK")){
            OTLINK tlink = (OTLINK)link;
            return translateOTlink(tlink,event,bratDocument);
        } else if (event.getType().equals("OLINK")){
            OTLINK olink = (OTLINK)link;
            return translateOTlink(olink,event,bratDocument);
        }else if (event.getType().equals("DLINK")){
            DLINK dlink = (DLINK)link;
            return translateDlink(dlink,event,bratDocument);
        } else{
            MLINK mlink = (MLINK)link;
            return translateMlink(mlink,event,bratDocument);
        }
    }

    private static BratEvent translateMlink(MLINK mlink, BratEvent event, BratDocumentwithList bratDocument) {
        List<BratEntity> entityList = bratDocument.getEntityList();
        event.setT_start_end(entityList.get(mlink.getTrigger()).getStart(),entityList.get(mlink.getTrigger()).getEnd());
        if (mlink.getTrigger()!=-1){
            event.setMembers(TRIGGER,entityList.get(mlink.getTrigger()).getId());
            event.setEntities(entityList.get(mlink.getTrigger()).getId(),entityList.get(mlink.getTrigger()));
        }

        for (int idx:mlink.getMovers()){
            event.setMembers(MOVER,entityList.get(idx).getId());
            event.setEntities(entityList.get(idx).getId(),entityList.get(idx));
        }
        return event;
    }

    private static BratEvent translateDlink(DLINK dlink, BratEvent event, BratDocumentwithList bratDocument) {
        List<BratEntity> entityList = bratDocument.getEntityList();
        event.setT_start_end(entityList.get(dlink.getVal()).getStart(),entityList.get(dlink.getVal()).getEnd());
        if (dlink.getVal()!=-1){
            event.setMembers(VAL,entityList.get(dlink.getVal()).getId());
            event.setEntities(entityList.get(dlink.getVal()).getId(),entityList.get(dlink.getVal()));
        }

        for (int idx:dlink.getLandmarks()){
            event.setMembers(LANDMARK,entityList.get(idx).getId());
            event.setEntities(entityList.get(idx).getId(),entityList.get(idx));
        }

        for (int idx:dlink.getTrajectors()){
            event.setMembers(TRAJECTOR,entityList.get(idx).getId());
            event.setEntities(entityList.get(idx).getId(),entityList.get(idx));
        }
        return event;
    }
}
