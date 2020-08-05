package edu.nju.ws.gqr;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CSPSparse implements Iterable<Map.Entry<Pair<Integer, Integer>, Relation>> {
    private int size;
    private Calculus calculus;
    private Map<Pair<Integer, Integer>, Relation> network;

    public String name;


    CSPSparse(int size, Calculus calculus, String name) {
        this.size = size;
        this.calculus = calculus;
        this.name = name;
        this.network = new HashMap<>();
    }

    void addConstraint(int x, int y, Relation r) {
        int i = Math.min(x, y), j = Math.max(x, y);
        if (i != x) {
            r = calculus.getConverse(r);
        }
        Pair<Integer, Integer> p = new ImmutablePair<>(i, j);
        if (network.containsKey(p)) {
            Relation old_r = network.get(p);
            r.intersect(old_r);
        }
        network.put(p, r);
    }

    public int size() {
        return size;
    }

    @Override
    public Iterator<Map.Entry<Pair<Integer, Integer>, Relation>> iterator() {
        return network.entrySet().iterator();
    }
}
