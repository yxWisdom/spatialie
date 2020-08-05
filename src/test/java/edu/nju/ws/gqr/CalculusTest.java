package edu.nju.ws.gqr;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

public class CalculusTest {

    private Calculus calculus;

    @Before
    public void setUp() throws Exception {
        String calculusName = "rcc8";
        String dir = "resource/gqr/data";
        String config = "resource/gqr/data/rcc8.spec";
        CalculusReader reader = new CalculusReader(calculusName, dir, config);
        calculus = reader.makeCalculus();

    }


    @Test
    public void baseRelationsAreSerial() {
        TestCase.assertTrue(calculus.baseRelationsAreSerial());
    }

    @Test
    public void getIdentityBaseRelation() {
        TestCase.assertEquals(2, (int) calculus.getIdentityBaseRelation());
    }

    @Test
    public void getIdentityRelation() {
        Relation r = new Relation(2);
        TestCase.assertEquals(r, calculus.getIdentityRelation());
    }

    @Test
    public void getUniversalRelation() {
        Relation r = new Relation(2);
        for (int i=0;i<calculus.size(); i++) {
            r.set(i);
        }
        TestCase.assertEquals(r, calculus.getUniversalRelation());
    }

    @Test
    public void getBaseRelationConverse() {

        TestCase.assertEquals(3, (int)calculus.getBaseRelationConverse(4));
        TestCase.assertEquals(4, (int)calculus.getBaseRelationConverse(3));
    }

    @Test
    public void getConverse() {
        Relation r1 = new Relation();
        Relation r2 = new Relation();
        r1.set(3);
        r1.set(4);
        r1.set(5);
        r1.set(6);
        r2.set(calculus.getBaseRelationConverse(3));
        r2.set(calculus.getBaseRelationConverse(4));
        r2.set(calculus.getBaseRelationConverse(5));
        r2.set(calculus.getBaseRelationConverse(6));
        TestCase.assertEquals(r2, calculus.getConverse(r1));
    }

    @Test
    public void getBaseRelationComposition() {


    }

    @Test
    public void getComposition() {
        Relation r1 = new Relation(1);
        Relation r2 = new Relation(2);
        TestCase.assertEquals(calculus.getComposition(r1, r2), calculus.getBaseRelationComposition(1, 2));
    }

    @Test
    public void encodeRelation() {
        String str = "EC PO DC";
        System.out.println(calculus.encodeRelation(str).toString());
    }

    @Test
    public void getWeight() {
        Relation r1 = new Relation();
        r1.set(1);
        r1.set(2);
        TestCase.assertEquals(calculus.getWeightBaseRelation(1) + calculus.getWeightBaseRelation(2), calculus.getWeight(r1));
    }

    @Test
    public void getWeightBaseRelation() {
    }

    @Test
    public void getBaseRelationName() {
        System.out.println(calculus.getBaseRelationName(2));
    }

    @Test
    public void getNumberOfBaseRelations() {
        TestCase.assertEquals(calculus.getNumberOfBaseRelations(), calculus.size());
    }

    @Test
    public void relationToString() {
        System.out.println(calculus.relationToString(new Relation(1)));
    }

    @Test
    public void checkConverseTable() {
        TestCase.assertTrue(calculus.checkConverseTable());
    }

    @Test
    public void checkCompositionTable() {
        TestCase.assertTrue(calculus.checkCompositionTable());
    }

    @Test
    public void getName() {
    }

    @Test
    public void equals() {
        TestCase.assertEquals(calculus, calculus);

    }
}