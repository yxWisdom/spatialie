package edu.nju.ws.spatialie.Link;

import java.util.Map;

public class LINK {
    Map<Integer,Integer> center;
    boolean iscompleted=false;
    String rule_id = "";

    public String getRule_id() {
        return rule_id;
    }

    public void setRule_id(String rule_id) {
        this.rule_id = rule_id;
    }

    public boolean isIscompleted() {
        return iscompleted;
    }

    public void Complete() {
        this.iscompleted = true;
    }

    public int getCenter(int idx){
        return center.get(idx);
    }

    public void addCenter(int son,int center){
        this.center.put(son,center);
    }
}
