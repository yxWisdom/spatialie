package edu.nju.ws.gqr;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CSP<R extends Relation, C extends CalculusOperations<R>> {
    private C calculus;

    private List<R> matrix;

    private int size;

    private int getPos(int row, int col) {
        return row * size + col;
    }

//    private Class<R> relClass;

    private String name;

    CSP(int size, C calculus, String name) {
        this.calculus = calculus;
        this.size = size;
        this.name = name;
        assert size > 1;
//        this.relClass = calculus.getRelClass();

        matrix = new ArrayList<>(Collections.nCopies(size*size, calculus.getUniversalRelation()));
        for (int i=0; i < size; i++) {
            matrix.set(getPos(i,i), calculus.getIdentityRelation());
        }

    }

    CSP(CSPSparse cspSparse, C calculus) {
        this.calculus = calculus;
        this.size = cspSparse.size();
        this.name = cspSparse.name;

        matrix = new ArrayList<>(Collections.nCopies(size*size,  calculus.getUniversalRelation()));
        for (int i=0; i < size; i++) {
            matrix.set(getPos(i,i), calculus.getIdentityRelation());
        }

        for (Map.Entry<Pair<Integer, Integer>, Relation> entry : cspSparse) {
            Pair<Integer, Integer> pair = entry.getKey();
            R relation = calculus.getNewInstance(entry.getValue());
            setConstraint(pair.getLeft(), pair.getRight(), relation);
        }
    }

//    CSP(CSP<Relation, CalculusOperations<Relation>> csp, C nc) {
//        this.calculus = nc;
//        this.size = csp.size();
//        this.name = csp.name;
//
//        matrix = new ArrayList<>(size*size);
//
//        for (int i = 0; i < size; ++i) {
//            for (int j = 0; j < size; j++) {
//                setConstraint(i, j, );
//            }
//        }
//    }

    void setConstraint(int x, int y, R r) {
        matrix.set(getPos(x, y), r);
        matrix.set(getPos(y, x), calculus.getConverse(r));
    }

    R getConstraint(int x, int y) {
        assert (matrix.get(getPos(x, y)).equals(calculus.getConverse(matrix.get(getPos(y, x)))));
        return matrix.get(getPos(x, y));
    }

    int size() {
        return size;
    }

    boolean equals(CSP<R, C> csp) {
        return size == csp.size() && calculus.equals(csp.calculus) && matrix.equals(csp.matrix);
    }

    C getCalculus() {
        return calculus;
    }

    public String getName() {
        return name;
    }
}
