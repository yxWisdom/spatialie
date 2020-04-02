package edu.nju.ws.spatialie.getrelation;

import java.util.HashMap;
import java.util.Map;

public class EvaluationCount {
    public Map<String,Integer> gold = new HashMap<>();
    public Map<String,Integer> correct = new HashMap<>();
    public Map<String,Integer> predict = new HashMap<>();
    String OTLINK = "OTLINK";
    String MLINK = "MLINK";
    String DLINK = "DLINK";
    String NOTRIGGER = "NOTRIGGER";

    public void addCorrectbyId(String id){
        if (correct.keySet().contains(id)){
            int v = correct.get(id);
            correct.replace(id,v+1);
        } else {
            correct.put(id,1);
        }
    }

    public void addPredictbyId(String id){
        if (predict.keySet().contains(id)){
            int v = predict.get(id);
            predict.replace(id,v+1);
        } else {
            predict.put(id,1);
        }
    }

    public EvaluationCount() {
        gold.put(OTLINK,0);
        gold.put(MLINK,0);
        gold.put(DLINK,0);
        correct.put(OTLINK,0);
        correct.put(MLINK,0);
        correct.put(DLINK,0);
        predict.put(OTLINK,0);
        predict.put(MLINK,0);
        predict.put(DLINK,0);
        gold.put(NOTRIGGER,0);
        correct.put(NOTRIGGER,0);
        predict.put(NOTRIGGER,0);
    }

    public int allGold(){
        int sum = 0;
        for (int v:gold.values()) sum+=v;
        return sum;
    }

    public int allCorrect(){
        int sum = 0;
        for (int v:correct.values()) sum+=v;
        return sum;
    }

    public int allPredict(){
        int sum = 0;
        for (int v:predict.values()) sum+=v;
        return sum;
    }

    public double precision(){
        return 1.0*allCorrect()/allPredict();
    }

    public double recall(){
        return 1.0*allCorrect()/allGold();
    }

    public void add(EvaluationCount e2){
        gold.put(OTLINK,gold.get(OTLINK)+e2.gold.get(OTLINK));
        gold.put(MLINK,gold.get(MLINK)+e2.gold.get(MLINK));
        gold.put(DLINK,gold.get(DLINK)+e2.gold.get(DLINK));
        gold.put(NOTRIGGER,gold.get(NOTRIGGER)+e2.gold.get(NOTRIGGER));
        correct.put(OTLINK,correct.get(OTLINK)+e2.correct.get(OTLINK));
        correct.put(MLINK,correct.get(MLINK)+e2.correct.get(MLINK));
        correct.put(DLINK,correct.get(DLINK)+e2.correct.get(DLINK));
        correct.put(NOTRIGGER,correct.get(NOTRIGGER)+e2.correct.get(NOTRIGGER));
        predict.put(OTLINK,predict.get(OTLINK)+e2.predict.get(OTLINK));
        predict.put(MLINK,predict.get(MLINK)+e2.predict.get(MLINK));
        predict.put(DLINK,predict.get(DLINK)+e2.predict.get(DLINK));
        predict.put(NOTRIGGER,predict.get(NOTRIGGER)+e2.predict.get(NOTRIGGER));
        for (String key:e2.predict.keySet()){
            if (key.contains("LINK")||key.contains(NOTRIGGER)) continue;
            if (predict.keySet().contains(key)){
                predict.replace(key,predict.get(key)+e2.predict.get(key));
            } else {
                predict.put(key,e2.predict.get(key));
            }
        }
        for (String key:e2.correct.keySet()){
            if (key.contains("LINK")||key.contains(NOTRIGGER)) continue;
            if (correct.keySet().contains(key)){
                correct.replace(key,correct.get(key)+e2.correct.get(key));
            } else {
                correct.put(key,e2.correct.get(key));
            }
        }
    }

    @Override
    public String toString() {
        String res = "";
        res= res+OTLINK+"\t"+correct.get(OTLINK)+"\t"+predict.get(OTLINK)+"\t"+gold.get(OTLINK)+"\n";
        res= res+MLINK+"\t"+correct.get(MLINK)+"\t"+predict.get(MLINK)+"\t"+gold.get(MLINK)+"\n";
        res= res+DLINK+"\t"+correct.get(DLINK)+"\t"+predict.get(DLINK)+"\t"+gold.get(DLINK)+"\n";
        res= res+NOTRIGGER+"\t"+correct.get(NOTRIGGER)+"\t"+predict.get(NOTRIGGER)+"\t"+gold.get(NOTRIGGER)+"\n";
        res = res+"p:"+"\t"+precision()+"\t"+"r:"+"\t"+recall()+"\n";
        for (String key:predict.keySet()){
            if (key.contains("LINK")||key.contains(NOTRIGGER)) continue;
            res = res+key+"\t" +predict.get(key)+"\t"+(correct.keySet().contains(key)?correct.get(key):0)+"\n";
        }
        return res;
    }
}
