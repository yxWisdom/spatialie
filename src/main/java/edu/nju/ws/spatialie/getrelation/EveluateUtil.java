package edu.nju.ws.spatialie.getrelation;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import edu.nju.ws.spatialie.annprocess.BratUtil;
import edu.nju.ws.spatialie.data.BratDocumentwithList;
import edu.nju.ws.spatialie.data.BratEntity;
import edu.nju.ws.spatialie.data.BratEvent;

import java.util.*;

import static java.lang.Integer.max;

public class EveluateUtil {
    public static void eveluate(BratDocumentwithList bratDocument, List<BratEvent> eventList, EvaluationCount evel) {
        List<BratEvent> bratEvents = new ArrayList<>();
        for (BratEvent e : bratDocument.getEventMap().values()) {
            bratEvents.add(e);
        }
        countLinkTypes(bratEvents, evel.gold);
        countLinkTypes(eventList, evel.predict);
        countCorrect(bratEvents, eventList, evel.correct);
    }

    private static void countCorrect(List<BratEvent> eventList1, List<BratEvent> eventList2, Map<String, Integer> gold) {
        for (BratEvent event1 : eventList1) {
            int this_gold = 0;
            for (BratEvent event2 : eventList2) {
                int num = judgeSame(event1, event2);
                int num2 = judgeSameReverse(event1, event2);
                this_gold += max(num, num2);
            }
            int v = gold.get(translateType(event1.getType()));
            gold.put(translateType(event1.getType()), v + this_gold);
        }
    }

    private static int judgeSameReverse(BratEvent event1, BratEvent event2) {
        if (event1.getType().contains("O") || event1.getType().contains("T")) {
            List<String> ids_1 = new ArrayList<>(event1.getRoleIds("landmark"));
            List<String> ids_2 = new ArrayList<>(event1.getRoleIds("trajector"));
            event1.removeRole("landmark");
            event1.removeRole("trajector");
            for (String id : ids_1) {
                event1.addRole("trajector", id);
            }
            for (String id : ids_2) {
                event1.addRole("landmark", id);
            }
            return judgeSame(event1, event2);
        }
        return 0;
    }

    private static int judgeSame(BratEvent event1, BratEvent event2) {
        int size1 = count(event1);
        if (event1.is(event2)) return size1;
        if (size1 == 1) return 0;
        //TODO:标准有问题
        for (String role : event1.getRoleMap().keySet()) {
            if (event1.getRoleMap().get(role).size() > 1) {
                int count = 0;
                for (String id : event1.getRoleMap().get(role)) {
                    if (event2.getRoleMap().get(role).contains(id)) count++;
                }
                return count;
            }
        }
        return 0;
    }

    private static void countLinkTypes(List<BratEvent> values, Map<String, Integer> evel) {
        for (BratEvent event : values) {
            String type = translateType(event.getType());
            int num = count(event);
            int v = evel.get(type);
            evel.put(type, v + num);
        }
    }

    private static String translateType(String type) {
        if (type.equals("OLINK") || type.equals("TLINK")) return "OTLINK";
        else return type;
    }

    private static int count(BratEvent event) {
        int size = 1;
        for (String role : event.getRoleMap().keySet()) {
            if (event.getRoleMap().get(role).size() > 1) {
                size = event.getRoleMap().get(role).size();
            }
        }
        return size;
    }

    public static List<BratEvent> removeRedundancy(List<BratEvent> eventList, BratDocumentwithList bratDocument) {
        List<BratEvent> res = new ArrayList<>();
        if (bratDocument.getTrigger() != null) {
            for (BratEvent e : eventList) {
                Collection<String> listmap = e.getRoleMap().get("trigger");
                if (listmap != null && listmap.contains(bratDocument.getTrigger().getId())) res.add(e);
                listmap = e.getRoleMap().get("val");
                if (listmap != null && listmap.contains(bratDocument.getTrigger().getId())) res.add(e);
            }
        } else {
            for (BratEvent e : eventList) {
                if (e.getRoleMap().get("trigger").size() == 0 && e.getRoleMap().get("val").size() == 0) res.add(e);
            }
        }
        return res;
    }

    public static void eveluate_NRE(BratDocumentwithList bratDocument, JSONObject object, List<BratEvent> eventList, EvaluationCount evel) {
        JSONObject t = object.getJSONObject("t");
        JSONObject h = object.getJSONObject("h");
        String relation = object.getString("relation");
        BratEntity et = getEntity(bratDocument, t);
        BratEntity eh = getEntity(bratDocument, h);
        int count = 0;
        for (BratEvent event : eventList) {
            //TODO:目前只针对no trigger
            if (event.getRoleMap().values().contains(et.getId()) && event.getRoleMap().values().contains(eh.getId())) {
                if (event.getType().equals("OTLINK") && !event.getRoleMap().keySet().contains("trigger")) {
                    if (event.getRoleIds("trajector").contains(eh.getId())&&event.getRoleIds("landmark").contains(et.getId())) {
                        count++;
                        if (count == 1) evel.predict.replace(event.getType(), evel.predict.get(event.getType()) + 1);
                        switch (relation) {
                            case "LocatedIn":
                                //TODO:正反错了就两个都错了
                                if (event.getRoleIds("trajector") != null && event.getRoleIds("trajector").size() > 0) {
                                    if (event.getRoleIds("landmark") != null && event.getRoleIds("landmark").size() > 0) {
                                        if (event.getRoleIds("landmark").contains(et.getId()) && event.getRoleIds("trajector").contains(eh.getId())
                                                || event.getRoleIds("landmark").contains(eh.getId()) && event.getRoleIds("trajector").contains(et.getId()))
                                            evel.gold.replace(event.getType(), evel.gold.get(event.getType()) + 1);
                                    }
                                }
                        }
                    }
                }
//                break;
            }
        }
//        if (count>1)
//            System.out.println(object.toString());
        switch (relation) {
            case "LocatedIn":
                evel.correct.replace("OTLINK", evel.correct.get("OTLINK") + 1);
                break;
        }
    }

    public static BratEntity getEntity(BratDocumentwithList bratDocument, JSONObject object) {
        String text = object.getString("name");
        List<Integer> pos = JSON.parseArray(object.getJSONArray("pos").toJSONString(), Integer.class);
        for (BratEntity e : bratDocument.getEntityList()) {
            if (e.getText().equals(text) && countidx(e.getStart(), bratDocument) == pos.get(0)) return e;
        }
        return null;
    }

    private static Integer countidx(int p, BratDocumentwithList bratDocument) {
        int idx = 0;
        for (int tp = 0; tp < p; tp = bratDocument.getContent().indexOf(' ', tp + 1) + 1) {
            idx++;
        }
        return idx;
    }

    public static String eveluate_NRE_all(BratDocumentwithList bratDocument, JSONObject object, List<BratEvent> eventList, EvaluationCount evel, boolean notrigger) {
        JSONObject t = object.getJSONObject("t");
        JSONObject h = object.getJSONObject("h");
        String relation = object.getString("relation");
        BratEntity et = getEntity(bratDocument, t);
        BratEntity eh = getEntity(bratDocument, h);
        int count = 0;
        String res = "None";
        for (BratEvent event : eventList) {
            if (event.getRoleMap().values().contains(et.getId()) && event.getRoleMap().values().contains(eh.getId())) {
                if (event.getRoleIds("trigger").contains(et.getId()) || event.getRoleIds("val").contains(et.getId())) {
                    count++;
                    if (count == 1) {
                        evel.predict.replace(event.getType(), evel.predict.get(event.getType()) + 1);
                        evel.addPredictbyId(event.getRuleid());
                        if (event.getRoleIds("mover").size()>0)
                            res = "mover";
                        else if (event.getRoleIds("landmark").contains(eh.getId()))
                            res = "landmark";
                        else
                            res = "trajector";
                    }
                    switch (relation) {
                        case "trajector":
                            if (event.getRoleIds("trigger") != null && event.getRoleIds("trigger").size() > 0) {
                                if (event.getRoleIds("trajector") != null && event.getRoleIds("trajector").size() > 0) {
                                    if (event.getRoleIds("trajector").contains(eh.getId()) && event.getRoleIds("trigger").contains(et.getId()))
                                    {
                                        evel.correct.replace(event.getType(), evel.correct.get(event.getType()) + 1);
                                        evel.addCorrectbyId(event.getRuleid());
                                    }
                                }
                            }
                            if (event.getRoleIds("val") != null && event.getRoleIds("val").size() > 0) {
                                if (event.getRoleIds("trajector") != null && event.getRoleIds("trajector").size() > 0) {
                                    if (event.getRoleIds("trajector").contains(eh.getId()) && event.getRoleIds("val").contains(et.getId()))
                                    {
                                        evel.correct.replace(event.getType(), evel.correct.get(event.getType()) + 1);
                                        evel.addCorrectbyId(event.getRuleid());
                                    }
                                }
                            }
                            break;
//                       TODO:不考虑顺序
                        case "landmark":
                            if (event.getRoleIds("trigger") != null && event.getRoleIds("trigger").size() > 0) {
                                if (event.getRoleIds("landmark") != null && event.getRoleIds("landmark").size() > 0) {
                                    if (event.getRoleIds("landmark").contains(eh.getId()) && event.getRoleIds("trigger").contains(et.getId()))
                                    {
                                        evel.correct.replace(event.getType(), evel.correct.get(event.getType()) + 1);
                                        evel.addCorrectbyId(event.getRuleid());
                                    }
                                }
                            }
                            if (event.getRoleIds("val") != null && event.getRoleIds("val").size() > 0) {
                                if (event.getRoleIds("landmark") != null && event.getRoleIds("landmark").size() > 0) {
                                    if (event.getRoleIds("landmark").contains(eh.getId()) && event.getRoleIds("val").contains(et.getId()))
                                    {
                                        evel.correct.replace(event.getType(), evel.correct.get(event.getType()) + 1);
                                        evel.addCorrectbyId(event.getRuleid());
                                    }
                                }
                            }
                            break;
                        case "mover":
                            if (event.getRoleIds("trigger") != null && event.getRoleIds("trigger").size() > 0) {
                                if (event.getRoleIds("mover") != null && event.getRoleIds("mover").size() > 0) {
                                    if (event.getRoleIds("mover").contains(eh.getId()) && event.getRoleIds("trigger").contains(et.getId()))
                                    {
                                        evel.correct.replace(event.getType(), evel.correct.get(event.getType()) + 1);
                                        evel.addCorrectbyId(event.getRuleid());
                                    }
                                }
                            }
                            break;
                    }
                }
                else
                    if (notrigger) {
                        if (event.getType().equals("OTLINK") && !event.getRoleMap().keySet().contains("trigger")) {
                            if (event.getRoleIds("trajector").contains(eh.getId())&&event.getRoleIds("landmark").contains(et.getId())) {
                                count++;
                                if (count == 1)
//                                    evel.predict.replace(event.getType(), evel.predict.get(event.getType()) + 1);
                                evel.predict.replace("NOTRIGGER", evel.predict.get("NOTRIGGER") + 1);
                                evel.addPredictbyId(event.getRuleid());
                                res = "LocatedIn";
                                if (relation.equals("LocatedIn")) {
                                    if (event.getRoleIds("trajector") != null && event.getRoleIds("trajector").size() > 0) {
                                        if (event.getRoleIds("landmark") != null && event.getRoleIds("landmark").size() > 0) {
                                            if (event.getRoleIds("landmark").contains(et.getId()) && event.getRoleIds("trajector").contains(eh.getId())
                                                    || event.getRoleIds("landmark").contains(eh.getId()) && event.getRoleIds("trajector").contains(et.getId())) {
//                                                evel.gold.replace(event.getType(), evel.gold.get(event.getType()) + 1);
                                                evel.correct.replace("NOTRIGGER", evel.correct.get("NOTRIGGER") + 1);
                                                evel.addCorrectbyId(event.getRuleid());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                break;
            }
        }

        switch (relation) {
            case "trajector":
            case "landmark":
                if (et.getTag().equals(BratUtil.SPATIAL_SIGNAL))
                    evel.gold.replace("OTLINK", evel.gold.get("OTLINK") + 1);
                else {
                    evel.gold.replace("DLINK", evel.gold.get("DLINK") + 1);
//                    System.out.println(bratDocument.getContent());
                }
                break;
            case "LocatedIn":
//                evel.correct.replace("OTLINK", evel.correct.get("OTLINK") + 1);
                evel.gold.replace("NOTRIGGER", evel.gold.get("NOTRIGGER") + 1);
                break;
            case "mover":
                evel.gold.replace("MLINK", evel.gold.get("MLINK") + 1);
                break;
        }
        return res;
    }

}
