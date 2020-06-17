package edu.nju.ws.spatialie.Link;

import java.util.ArrayList;
import java.util.List;

public class OTLINK extends LINK{
    int trigger=-1;
    int flag = -1;
    List<Integer> trajectors= new ArrayList<>();
    List<Integer> landmarks= new ArrayList<>();

    public OTLINK(int trigger) {
        this.trigger = trigger;
    }

    public int getTrigger() {
        return trigger;
    }

    public List<Integer> getTrajectors() {
        return trajectors;
    }

    public List<Integer> getLandmarks() {
        return landmarks;
    }

    public void setTrigger(int trigger) {
        this.trigger = trigger;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }

    public int getTrajector(int idx) {
        return trajectors.get(idx);
    }

    public void addTrajectors(int trajector) {
        this.trajectors.add(trajector);
    }

    public int getLandmark(int idx) {
        return landmarks.get(idx);
    }

    public void addLandmarks(int landmarks) {
        this.landmarks.add(landmarks);
    }

    public void removeTrajector(int idx) {
        trajectors.remove((Integer)idx);
    }
}
