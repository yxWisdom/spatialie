package edu.nju.ws.gqr;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class RelationTest {

    private Relation relation;
    private Relation relation1;

    @Before
    public void before(){
        relation = new Relation();
        relation1 = new Relation(1);
    }

    @Test
    public void testNew() {
        Relation r = new Relation(relation1);
        TestCase.assertEquals(r, relation1);
        TestCase.assertNotSame(r, relation);
    }

    @Test
    public void isEmpty() {
        TestCase.assertTrue(relation.isEmpty());
        TestCase.assertTrue(!relation1.isEmpty());
    }

    @Test
    public void set() {
        relation.set(2);
        relation.set(3);
        System.out.println(relation.toString());

    }

    @Test
    public void unset() {
        relation1.unset(1);
        TestCase.assertEquals(relation, relation1);
    }

    @Test
    public void get() {
        TestCase.assertTrue(relation1.get(1));
        TestCase.assertFalse(relation1.get(2));
    }

    @Test
    public void union() {
        Relation r = new Relation();
        r.set(3);
        r.set(1);
        r.set(2);
        relation.set(2);
        relation.set(3);
        relation.union(relation1);
        TestCase.assertEquals(relation, r);
    }

    @Test
    public void intersect() {
        Relation r = new Relation(1);
        relation.set(1);
        relation.set(2);
        relation1.set(3);
        relation1.intersect(relation);
        TestCase.assertEquals(relation1, r);
    }

    @Test
    public void union1() {
        TestCase.assertEquals(Relation.union(relation1, relation), relation1);
    }

    @Test
    public void intersect1() {
        TestCase.assertEquals(Relation.intersect(relation1, relation), relation);
    }

    @Test
    public void isSubsetOf() {
        TestCase.assertTrue(relation.isSubsetOf(relation1));
    }

//    @Test
//    public void print() {
//    }

    @Test
    public void maxSize() {
        TestCase.assertEquals(Relation.maxSize(), Integer.MAX_VALUE);
    }

//    @Test
//    public void init() {
//    }
//
//    @Test
//    public void clean_up() {
//    }

    @Test
    public void size() {
        TestCase.assertEquals(1, relation1.size());
    }

    @Test
    public void negation() {
        relation.set(1);
        relation.set(2);
        relation.set(3);
        Relation r = new Relation(relation);
        r.unset(1);
        TestCase.assertEquals(r, relation.negation(relation1));
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        Relation r = relation1.clone();
        TestCase.assertEquals(r, relation1);
        TestCase.assertNotSame(r, relation1);
    }
}