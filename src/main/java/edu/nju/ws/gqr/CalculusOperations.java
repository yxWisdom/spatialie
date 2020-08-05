package edu.nju.ws.gqr;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class CalculusOperations<R extends Relation> {

    private Calculus calculus;

    private R identity;

    private R universal;

    private Class<R> relClass;

    private int preComputationOffset;

    private List<R> preComputedCompositionTable;

    private List<R> preComputedConverseTable;

    private void preComputedComposition() {
    }

    private void setPreComputedConverse() {
    }


    private int[] splitValues = new int[2];

    private int[] quadrantSizes = new int[2];

    private List<Integer> preComputedWeights;

    private void preComputedWeights() {
    }

    private List<List<R>> compositionTable;


    private List<R> preComputedSplit;

    private void setPreComputedSplit() {
    }


    CalculusOperations(Calculus calculus, Class<R> rClass) {
        this.calculus = calculus;
        this.relClass = rClass;
        assert R.maxSize() >= getNumberOfBaseRelations();

        this.identity = getNewInstance(calculus.getIdentityRelation());
        this.universal = getNewInstance(calculus.getUniversalRelation());
        buildCompositionTable();
    }

    private void buildCompositionTable() {
        compositionTable = new ArrayList<>();
        for (int i = 0; i < getNumberOfBaseRelations(); i++) {
            compositionTable.add(new ArrayList<>());
            for (int j = 0; j < getNumberOfBaseRelations(); j++) {
                Relation r = calculus.getBaseRelationComposition(i, j);
                compositionTable.get(i).add(getNewInstance(r));
            }
        }
    }

//    private void buildIdentityRelation() {
//        this.identity = getNewInstance();
//        assert this.identity != null;
//        this.identity.set(calculus.getIdentityBaseRelation());
//    }
//
//
//    private void buildUniversalRelation() {
//        this.universal = getNewInstance();
//        assert this.universal != null;
//        for (int i = 0; i < getNumberOfBaseRelations(); i++) {
//            this.universal.set(i);
//        }
//    }


    boolean baseRelationsAreSerial() {
        return calculus.baseRelationsAreSerial();
    }

    private int getNumberOfBaseRelations() {
        return calculus.getNumberOfBaseRelations();
    }

    R getNewInstance() {
        try {
            return relClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    R getNewInstance(Relation r) {
        try {
            return relClass.getConstructor(Relation.class).newInstance(r);
        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }


    private R computeConverse(R r) {
        R res = getNewInstance();
        assert res != null;
        for (Integer element : r) {
            res.set(calculus.getBaseRelationConverse(element));
        }
        return res;
    }

    R getConverse(R r) {
        return computeConverse(r);
    }

    private R computeComposition(R a, R b) {
        R res = getNewInstance();
        assert res != null;
        for (Integer e1 : a) {
            for (Integer e2 : b) {
                res.union(compositionTable.get(e1).get(e2));
                if (res.equals(universal)) {
                    return universal;
                }
            }
        }
        return res;
    }

    R getComposition(R a, R b) {
        return computeComposition(a, b);
    }

    private int computeWeight(R r) {
        int res = 0;
        for (int i = 0; i < getNumberOfBaseRelations(); i++) {
            if (r.get(i))
                res += calculus.getWeightBaseRelation(i);
        }
        return res;
    }

    int getWeight(R r) {
        return computeWeight(r);
    }

    R getIdentityRelation() {
        return identity;
    }

    R getUniversalRelation() {
        return universal;
    }

    Calculus getCalculus() {
        return calculus;
    }

    R getNegation(R r) {
        return getNewInstance(universal.negation(r));
    }

    public boolean equals(CalculusOperations c) {
        // TODO: too weak
        return calculus.equals(c.calculus);
    }


    Class<R> getRelClass() {
        return this.relClass;
    }

}
