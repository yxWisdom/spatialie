package edu.nju.ws.spatialie.data;

import edu.nju.ws.spatialie.utils.FileUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BIODocument extends BratDocumentwithList {
    BratEntity trigger;
    List<String> conj = FileUtil.readLines("data/BIO/conj.txt");
    List<String> motion_signal = FileUtil.readLines("data/BIO/motion_signal.txt");
    List<String> ms_manner = FileUtil.readLines("data/BIO/MS_manner.txt");
    int length;
    String[] content;
    String[] taglist;
    String[] memberlist;
    public BIODocument(String text, String tags, String members) throws CloneNotSupportedException {
        super(text);
        tags = tags+" O";
        String[] taglist = tags.split(" ");
        this.taglist = taglist;
        members = members+" O";
        String[] memberlist = members.split(" ");
        this.memberlist = memberlist;
        length = taglist.length-1;

//        //补充ms和conj
//        for (int i = 0;i<text.length();i++){
//            if (tags.contains(BratUtil.MOTION)&&judgeequal(taglist,memberlist)) {
//                for (String c : motion_signal) {
//                    if (i + c.length() <= text.length()) {
//                        if (taglist[i].equals("O")&&text.substring(i).startsWith(c)&&corenlp.fenci(text,i,i+c.length())) {
//                            taglist[i] = "B-" + BratUtil.MOTION_SIGNAL;
//                            for (int ti = i + 1; ti < i + c.length(); ti++) {
//                                taglist[ti] = "I-" + BratUtil.MOTION_SIGNAL;
//                            }
//                            break;
//                        }
//                    }
//                }
//            }
//            for (String c:conj){
//                if (c.equals("位于")){
//                    System.out.println("1");
//                }
//                if (i+c.length()<=text.length()){
//                    if (taglist[i].equals("O")&&text.substring(i).startsWith(c)&&corenlp.fenci(text,i,i+c.length())){
//                        taglist[i] = "B-"+BratUtil.SPATIAL_CONJ;
//                        for (int ti = i+1;ti<i+c.length();ti++){
//                            taglist[ti] = "I-"+BratUtil.SPATIAL_CONJ;
//                        }
//                        break;
//                    }
//                }
//            }
//
//        }

        int begin = -1;
        int idx = 0;
        for (int i = 0;i<taglist.length;i++){
            if (begin!=-1&&!taglist[i].startsWith("I")){
                BratEntity entity = new BratEntity();
                String type = taglist[i-1].substring(2);
                entity.setTag(type);
                entity.setText(text.substring(begin,i));
                entity.setStart(begin);
                entity.setEnd(i);
                entity.setId("T"+idx);
                this.getEntityMap().put("T"+idx,entity);
                if (entity.getTag().equals(BratUtil.MOTION)){
                    BratAttribute a = new BratAttribute("A"+idx,"motion_class","move","T"+idx);
                    List<BratAttribute> al = new ArrayList<>();
                    al.add(a);
                    entity.setBratAttributes(al);
                } else if (entity.getTag().equals(BratUtil.MOTION_SIGNAL)){
                    BratAttribute a;
                    if (ms_manner.contains(entity.getText())){
                       a  = new BratAttribute("A"+idx,"MS_type","manner","T"+idx);
                    } else {
                        a = new BratAttribute("A"+idx,"MS_type","path","T"+idx);
                    }
                    List<BratAttribute> al = new ArrayList<>();
                    al.add(a);
                    entity.setBratAttributes(al);
                }else {
                    List<BratAttribute> al = new ArrayList<>();
                    entity.setBratAttributes(al);
                }

                idx++;
                begin = -1;
            }
            if (taglist[i].startsWith("B-")){
                begin = i;
                continue;
            }

        }

        BratEvent bratEvent = new BratEvent();
        this.getEventMap().put("E0",bratEvent);
        begin = -1;


        String linktype = null;
        Set<String> memberset = new HashSet<>();
        String duplicate = "";
        Set<Integer> startpos = new HashSet<>();
        for (int i = 0;i<memberlist.length;i++){
            if (begin!=-1&&!memberlist[i].startsWith("I")){
                String type = memberlist[i-1].substring(2);
                String id = "";
                for (BratEntity entity:this.getEntityMap().values()){
                    if (entity.getStart()==begin&&entity.getEnd()==i){
                        id = entity.getId();
                        break;
                    }
                }
                BratEntity e = getEntityMap().get(id);
                bratEvent.setEntities(id,e);
                if (e.getTag().equals(BratUtil.MEASURE)){
                    bratEvent.setMembers("val",id);

                } else {
                    if (bratEvent.getMembers().get(type)!=null) startpos.add(i); else
                        bratEvent.setMembers(type,id);
                }
                if (type.equals("trigger")){
                    this.trigger = e;
                    if (e.getTag().equals(BratUtil.MEASURE)){
                        linktype = "DLINK";
                    } else if (e.getTag().equals(BratUtil.MOTION)){
                        linktype = "MLINK";
                    } else {
                        String tag = e.getTag();
                        String[] olist = {"东","西","南","北","前","左","右","后","上","下"};
                        boolean f = false;
                        for (String o:olist){
                            if (tag.contains(o)){
                                f = true;break;
                            }
                        }
                        if (f){
                            linktype = "OLINK";
                        } else {
                            linktype = "TLINK";
                        }
                    }
                }
                begin = -1;
//                idx++;
            }
            if (memberlist[i].startsWith("B-")){
                if (memberset.contains(memberlist[i].substring(2))){
                    duplicate = memberlist[i].substring(2);
                } else {
                    memberset.add(memberlist[i].substring(2));
                }
                begin = i;
            }
        }
        if(linktype==null) linktype="TLINK";
        bratEvent.setType(linktype);
        if (!memberset.isEmpty()){
            int numevent = 1;
            for (int start:startpos) {
                BratEvent e = bratEvent.clone();
                BratEntity replacee = null;
                for (BratEntity entity:getEntityMap().values()){
                    if (entity.getEnd()==start) {
                        replacee = entity;
                        break;
                    }
                }
                String originid = e.getMemberId(duplicate);
                e.setMembers(duplicate,replacee.getId());
                e.getEntities().remove(originid);
                e.setEntities(replacee.getId(),replacee);
                this.getEventMap().put("E"+numevent,e);
                numevent++;
            }

        }
    }

    private boolean judgeequal(String[] taglist, String[] memberlist) {
        int i = 0;
        for (String s:memberlist){
            if (s.equals("I-trigger")){
                if (taglist[i].contains(BratUtil.MOTION)) return true; else return false;
            }
            i++;
        }
        return false;
    }
}
