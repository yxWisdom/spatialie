package edu.nju.ws.gqr;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WeightedTripleIterator<R extends Relation, N extends CSP<R, ? extends CalculusOperations<R>>> {
    private static <R extends Relation, N extends CSP<R, ? extends CalculusOperations<R>>> boolean revise(N csp, int i, int j, int k) {
        R oldEdge = csp.getConstraint(i, k);
        R r1 = csp.getConstraint(i, j);
        R r2 = csp.getConstraint(j, k);
        R comp = csp.getCalculus().getComposition(r1, r2);
        @SuppressWarnings("unchecked")
        R newEdge = (R) R.intersect(oldEdge, comp);
        if (!oldEdge.equals(newEdge)) {
            csp.setConstraint(i, k, newEdge);
            return true;
        }
        return false;
    }

    private PriorityMapQueue<Integer, Integer> queue;

    private void addToQueue(int i, int j, N csp) {
        assert i != j;
        int a = Math.min(i, j);
        int b = Math.max(i, j);
        int pos = a * csp.size() + b;
        int weight = csp.getCalculus().getWeight(csp.getConstraint(a, b));
        queue.add(new ImmutablePair<>(pos, weight));
    }

    private List<Pair<Integer, Integer>> pc1(N csp) {
        int size = csp.size();

        while (!queue.isEmpty()) {
            int index = queue.peek().getLeft();
            queue.remove();
            int i = index / size;
            int j = index % size;
            assert i < j;

            for (int k = 0; k < size; ++k) {
                if (revise(csp, i, j, k)) {
                    if (csp.getConstraint(i, k).isEmpty()) {
                        return describeTriple(i, j, k);
                    }
                    addToQueue(i, k, csp);
                }
                if (revise(csp, k, i, j)) {
                    if (csp.getConstraint(k, j).isEmpty()) {
                        return describeTriple(i, j, k);
                    }
                    addToQueue(k, j, csp);
                }
            }
        }
        return new ArrayList<>();
    }

    private List<Pair<Integer, Integer>> describeTriple(int i, int j, int k) {
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        res.add(new ImmutablePair<>(i, j));
        res.add(new ImmutablePair<>(i, k));
        res.add(new ImmutablePair<>(j, k));
        return res;
    }

    WeightedTripleIterator() {
        this.queue = new PriorityMapQueue<>(Comparator.comparingInt(Integer::intValue).reversed());
    }

    List<Pair<Integer, Integer>> enforce(N csp) {
        queue.clear();
        int size = csp.size();
        if (size < 2)
            return new ArrayList<>();
        if (csp.getConstraint(0, 1).isEmpty())
            return describeTriple(0, 1, 2);
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if (csp.getCalculus().baseRelationsAreSerial() && csp.getConstraint(i, j).equals(csp.getCalculus().getUniversalRelation()))
                    continue;
                addToQueue(i, j, csp);
            }
        }
        return pc1(csp);
    }

    List<Pair<Integer, Integer>> enforce(N csp, int i, int j) {
        queue.clear();
        int size = csp.size();
        if (size > 0 && csp.getConstraint(i, j).isEmpty()) {
            return describeTriple(0, i, j);
        }
        addToQueue(i, j, csp);
        return pc1(csp);
    }

}
