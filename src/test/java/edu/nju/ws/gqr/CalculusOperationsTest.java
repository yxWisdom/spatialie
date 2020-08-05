package edu.nju.ws.gqr;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

public class CalculusOperationsTest {
    private CalculusOperations<Relation> calculusOperation;
    private Calculus calculus;

    @Before
    public void setUp() throws Exception {

        String calculusName = "rcc8";
        String dir = "resource/gqr/data";
        String config = "resource/gqr/data/rcc8.spec";
        CalculusReader reader = new CalculusReader(calculusName, dir, config);
        calculus = reader.makeCalculus();
        calculusOperation = new CalculusOperations<>(calculus, Relation.class);
    }


    @Test
    public void baseRelationsAreSerial() {
        TestCase.assertTrue(calculusOperation.baseRelationsAreSerial());
    }

    @Test
    public void getNewInstance() {
        Relation r1 = new Relation();
        Relation r2 = calculusOperation.getNewInstance();
        TestCase.assertEquals(r1, r2);
     }

    @Test
    public void getNewInstance1() {
        Relation r1 = new Relation(1);
        Relation r2 = calculusOperation.getNewInstance(r1);
        TestCase.assertEquals(r1, r2);
        TestCase.assertNotSame(r1, r2);
    }

    @Test
    public void getConverse() {
        Relation r1 = new Relation();
        Relation r2 = new Relation();
        r1.set(3);
        r1.set(4);
        r1.set(5);
        r2.set(calculus.getBaseRelationConverse(3));
        r2.set(calculus.getBaseRelationConverse(4));
        r2.set(calculus.getBaseRelationConverse(5));
        TestCase.assertEquals(r2, calculusOperation.getConverse(r1));

    }

    @Test
    public void getComposition() {
        Relation r1 = new Relation(2);
        Relation r2 = calculusOperation.getComposition(r1, r1);
        TestCase.assertEquals(r1, r2);
    }

    @Test
    public void getWeight() {
        System.out.println("weight:" + calculusOperation.getWeight(new Relation(2)));
    }

    @Test
    public void getIdentityRelation() {
        TestCase.assertEquals(calculus.getIdentityRelation(), calculusOperation.getIdentityRelation());
    }

    @Test
    public void getUniversalRelation() {
        TestCase.assertEquals(calculus.getUniversalRelation(), calculusOperation.getUniversalRelation());
    }

    @Test
    public void getCalculus() {
        TestCase.assertEquals(calculus, calculusOperation.getCalculus());
    }

//    @Test
//    public void getNegation() {
//        TestCase.assertEquals();
//    }

    @Test
    public void equals() {
    }

    @Test
    public void getRelClass() {
    }
}