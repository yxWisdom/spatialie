package edu.nju.ws.spatialie.Link;

import java.util.ArrayList;
import java.util.List;

public class DLINK extends LINK{
    int val = -1;
    List<Integer> trajectors = new ArrayList<>();
    List<Integer> landmarks = new ArrayList<>();

    public List<Integer> getTrajectors() {
        return trajectors;
    }

    public List<Integer> getLandmarks() {
        return landmarks;
    }

    public DLINK(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
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

    public void removeTrajector(int idx_next2) {
        trajectors.remove((Integer)idx_next2);
    }
}
