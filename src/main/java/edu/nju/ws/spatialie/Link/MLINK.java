package edu.nju.ws.spatialie.Link;

import java.util.ArrayList;
import java.util.List;

public class MLINK extends LINK {
    int trigger = -1;
    List<Integer> movers= new ArrayList<>();

    public MLINK(int trigger) {
        this.trigger = trigger;
    }

    public int getTrigger() {
        return trigger;
    }

    public void setTrigger(int trigger) {
        this.trigger = trigger;
    }

    public List<Integer> getMovers() {
        return movers;
    }

    public void setMover(int mover) {
        this.movers.add(mover);
    }
}
