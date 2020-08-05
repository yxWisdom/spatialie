//package edu.nju.ws.gqr;
//
//import java.util.List;
//
//public class CalculusOri {
//    private String calculusName;
//    private List<String> baseRelationNames;
//    private int identity;
//    private List<Integer> converseTable;
//    private List<List<Relation>> compositionTable;
//
//    // Calculus property
//    private boolean baseRelationsSerial;
//
//    private List<Integer> relationWeights;
//
//    private Relation universalRelation;
//
//    private Relation identityRelation;
//
//
//    CalculusOri(String calculusName, List<String> baseRelationNames, int identity, List<Integer> converseTable,
//                List<List<Relation>> compositionTable, List<Integer> weights) {
//        this.calculusName = calculusName;
//        this.baseRelationNames = baseRelationNames;
//        this.identity = identity;
//        this.converseTable = converseTable;
//        this.compositionTable = compositionTable;
//        this.baseRelationsSerial = decideBaseRelationsSerial(compositionTable);
//        this.relationWeights = weights;
//        this.universalRelation = buildUniversalRelation(getNumberOfBaseRelations());
//        this.identityRelation = buildIdentityRelation(identity);
//
//        assert identity < getNumberOfBaseRelations();
//        assert !converseTable.isEmpty();
//        assert !compositionTable.isEmpty();
//        assert converseTable.size() == compositionTable.size();
//
//        for (int i=0; i<converseTable.size(); i++) {
//            for (int j=i+1; j<converseTable.size(); j++) {
//                assert compositionTable.get(i).size() == compositionTable.get(j).size();
//            }
//        }
//    }
//    final boolean baseRelationsAreSerial() {return baseRelationsSerial;}
//
//    final Integer getIdentityBaseRelation() {
//        return identity;
//    }
//
//    final Relation getIdentityRelation() {
//        return identityRelation;
//    }
//
//    final Relation getUniversalRelation() {
//        return universalRelation;
//    }
//
//    final Integer getBaseRelationConverse(int i) {
//        return converseTable.get(i);
//    }
//
//    final Relation getConverse(Relation r) {
//        Relation res = new Relation();
//        for (Integer element : r) {
//            res.set(getBaseRelationConverse(element));
//        }
//        return res;
//    }
//
//    final Relation getBaseRelationComposition(int i, int j) {
//        return compositionTable.get(i).get(j);
//    }
//
//    // Get the composition of two (arbitrary) relations
//    final Relation getComposition(Relation r1, Relation r2) {
//        Relation res = new Relation();
//        for (Integer e1: r1) {
//            for (Integer e2: r2) {
//                res.union(getBaseRelationComposition(e1, e2));
//                if (res.size() == getNumberOfBaseRelations()) {
//                    return res;
//                }
//            }
//        }
//        return res;
//    }
//
//    /**
//     * Convert a string representation of a relation to its binary representation
//     */
//    Relation encodeRelation(String s) {
//        Relation r = new Relation();
//        String [] names = s.split("\\s");
//
//        for (String name: names) {
//            int relationId = -1;
//            for (int i = 0 ; i < getNumberOfBaseRelations(); i++) {
//                if (getBaseRelationName(i).equals(name)) {
//                    relationId = i;
//                    break;
//                }
//            }
//            if (relationId == -1) {
//                System.err.printf("Warning: unknown relation: \"%s\"", name);
//                continue;
//            }
//            r.set(relationId);
//        }
//        return r;
//    }
//
//    int getWeight(Relation r) {
//        int res = 0;
//        for (Integer element: r) {
//            res += relationWeights.get(element);
//        }
//        return res;
//    }
//
//
//    int getWeightBaseRelation(int i) {
//        return relationWeights.get(i);
//    }
//
//    String  getBaseRelationName(int i) {
//        return baseRelationNames.get(i);
//    }
//
//    public int getNumberOfBaseRelations() {
//        return converseTable.size();
//    }
//
//    String relationToString(Relation r) {
//        StringBuilder sb = new StringBuilder("(");
//        for (int ele: r) {
//            sb.append(" ").append(getBaseRelationName(ele));
//        }
//        sb.append(")");
//        return sb.toString();
//    }
//
//
//    /**
//     *  check converse function
//     * @return true if no error can be found in syntactical definition
//     */
//    boolean checkConverseTable() {
//        System.out.println("Checking integrity of the converse table ...");
//        if (identity != getBaseRelationConverse(identity)) {
//            String identity_name = getBaseRelationName(identity);
//            System.err.printf("ERROR: conv(%s) != %s\n", identity_name, identity_name);
//            return false;
//        }
//
//        // conv(conv(a)) == a ?
//        for (int i=0; i<getNumberOfBaseRelations(); i++) {
//            int c_i = getBaseRelationConverse(i);
//            int c_c_i = getBaseRelationConverse(c_i);
//            if (i!=c_c_i) {
//                String name = getBaseRelationName(i);
//                String c_name = getBaseRelationName(c_i);
//                String c_c_name = getBaseRelationName(c_c_i);
//                System.err.printf("ERROR: conv(conv(%s)!=%s\n", name, name);
//                System.err.printf("ERROR: conv(%s)=%s conv(%s)=%s\n", name, c_name, c_name, c_c_name);
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * check composition function
//     * @return true if no error can be found in syntactical definition
//     */
//    boolean checkCompositionTable() {
//        System.out.println("Checking integrity of the composition table ...");
//
//        // check I o R = R and R o I = R
//        for (int i=0; i<getNumberOfBaseRelations();i++) {
//            Relation r = new Relation(i);
//            Relation r1 = getBaseRelationComposition(i, identity);
//            Relation r2 = getBaseRelationComposition(identity, i);
//            String name = getBaseRelationName(i), composeName1 = relationToString(r1), composeName2=relationToString(r2);
//
//            if (!r1.equals(r)) {
//                System.err.println("Checking I o R = R ...");
//                System.err.printf("ERROR: I o %s = %s \n", name, composeName1);
//            }
//
//            if (!r2.equals(r)) {
//                System.err.println("Checking R o I= R ...");
//                System.err.printf("ERROR: %s o I = %s \n", name, composeName2);
//            }
//
//        }
//
//        // check conv(A o B) = conv(A) o conv(B) ?
//        for (int i=0; i<getNumberOfBaseRelations(); i++) {
//            for (int j=0; j<getNumberOfBaseRelations();j++) {
//                Relation r1 = getConverse(getBaseRelationComposition(i, j));
//                Relation r2 = getBaseRelationComposition(getBaseRelationConverse(i), getBaseRelationConverse(j));
//
//                if (!r1.equals(r2))  {
//                    String name_i = getBaseRelationName(i), name_j = getBaseRelationName(j);
//                    System.err.println("Checking conv(A o B) = conv(A) o conv(B) ...");
//                    System.err.printf("ERROR: conv(%s o %s) = %s\n", name_i, name_j, relationToString(r1));
//                    System.err.printf("ERROR: conv(%s) o conv(%s) = %s\n", name_i, name_j, relationToString(r2));
//                }
//            }
//        }
//
//        // check conv(A) \in comp(B,C) <=> conv(C) \in comp(A,B)
//
//        for (int i = 0; i < getNumberOfBaseRelations(); i++) {
//            int c_i = getBaseRelationConverse(i);
//            for (int j = 0 ; j < getNumberOfBaseRelations(); j++) {
//                Relation relation_i_j = getBaseRelationComposition(i, j);
//                for (int k = 0; k < getNumberOfBaseRelations(); k++) {
//                    Relation relation_j_k = getBaseRelationComposition(j, k);
//                    int c_k = getBaseRelationConverse(k);
//                    if (relation_i_j.get(c_k) != relation_j_k.get(c_i)) {
//                        Relation rel_i = new Relation(i), rel_j = new Relation(j), rel_k = new Relation(k);
//                        System.err.println("heck conv(A) \\in comp(B,C) <=> conv(C) \\in comp(A,B) ...");
//                        System.err.println("ERROR INFO:");
//                        System.err.printf("conv(%s) in comp(%s, %s) ? %b \n", relationToString(rel_i),
//                                relationToString(rel_j), relationToString(rel_k), relation_j_k.get(c_i));
//                        System.err.printf("conv(%s) in comp(%s, %s) ? %b \n", relationToString(rel_k),
//                                relationToString(rel_i), relationToString(rel_j), relation_i_j.get(c_k));
//                        return false;
//                    }
//                }
//            }
//        }
//        return true;
//    }
//
//
//    String getName()  { return calculusName; }
//
//    // TODO: quiet simple
//    boolean equals(CalculusOri c) {
//        return calculusName.equals(c.getName());
//    }
//
//
//    /**
//     * Implementation of the Calculus class template
//     *
//     * Parts of the code in this file are taken from Bernhard Nebel's reasoner for
//     * Allen's interval algebra, which is again based on work by Peter van Beek.
//     * The code is used with permission from the authors.
//     */
//
//    static boolean decideBaseRelationsSerial(List<List<Relation>> compositionTable) {
//        for (List<Relation> list: compositionTable)
//            for (Relation r: list) {
//                if (r.isEmpty())
//                    return false;
//            }
//        return true;
//    }
//
//
//    static Relation buildIdentityRelation(int id) {
//        Relation r = new Relation();
//        r.set(id);
//        return r;
//    }
//
//    static Relation buildUniversalRelation(int size) {
//        Relation r = new Relation();
//        for (int i = 0; i < size; i++) {
//            r.set(size);
//        }
//        return r;
//    }
//
//
//
//}
