package edu.nju.ws.gqr;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class Relation implements Iterable<Integer>, Cloneable {
    private Set<Integer> elements;

    public Relation() {
        elements = new TreeSet<>();
    }

    public Relation(int pos) {
        elements = new TreeSet<>();
        set(pos);
    }

    public Relation(Relation r) {
        elements = new TreeSet<>(r.elements);
    }

    final boolean isEmpty() {
        return elements.isEmpty();
    }


    public final void set(int p) {
        elements.add(p);
    }

    public final void unset(int p) {
        elements.remove(p);
    }

    public final boolean get(int p) {
        return elements.contains(p);
    }


    final void union(Relation r) {
        elements.addAll(r.elements);
    }

    final void intersect(Relation r) {
        elements.retainAll(r.elements);
    }

    static Relation union(Relation r1, Relation r2) {
        Relation res = new Relation(r1);
        res.union(r2);
        return res;
    }

    static Relation intersect(Relation r1, Relation r2) {
        Relation res = new Relation(r1);
        res.intersect(r2);
        return res;
    }

    final boolean isSubsetOf(Relation r) {
        return r.elements.containsAll(elements);
    }

    public final String toString() {
        return elements.toString();
    }


    static int maxSize() {
        return Integer.MAX_VALUE;
    }

//    public static void init() {
//    }
//
//    public static void clean_up() {
//    }

    public long size() {
        return elements.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || this.getClass() != obj.getClass())
            return false;

        return elements.equals(((Relation) obj).elements);
    }

    @Override
    public Iterator<Integer> iterator() {
        return elements.iterator();
    }

    Relation negation(Relation r) {
//        Relation res = new Relation();
        elements.removeAll(r.elements);
        return this;
    }

    @Override
    public Relation clone() throws CloneNotSupportedException {
        return (Relation) super.clone();
    }
}


//package edu.nju.ws.gqr;
//
//import java.util.*;
//
//public class Relation {
//    private List<Integer> elements;
//
//    public Relation() {
//        elements = new ArrayList<>();
//    }
//
//    public final boolean none() {
//        return elements.isEmpty();
//    }
//
//
//    public final void set(int p) {
//        int index = Collections.binarySearch(elements, p);
//        if (index >= 0 && elements.get(index).equals(p)) {
//            return;
//        }
//        elements.add(index, p);
//    }
//
//    public final void unset(int p) {
//        int index = Collections.binarySearch(elements, p);
//        if (index >= 0 && elements.get(index).equals(p))
//            elements.remove(index);
//    }
//
//    public final boolean get(int p) {
//        return Collections.binarySearch(elements, p) >= 0;
//    }
//
//
//    public final void and(Relation r) {
//        Relation result = new Relation();
//        Iterator<Integer> it1 = elements.listIterator();
//        Iterator<Integer> it2 = r.elements.listIterator();
//        int a = it1.next(), b = it2.next();
//        while (it1.hasNext() && it2.hasNext()) {
//            if (a < b){
//                a = it1.next();
//            } else if (a > b) {
//                b = it2.next();
//            } else if (a == b) {
//                result.elements.add(a);
//                a = it1.next();
//                b = it2.next();
//            }
//        }
//        elements = result.elements;
//    }
//
//    public final Relation or(Relation r) {
//        Relation result = new Relation();
//        Iterator<Integer> it1 = elements.listIterator();
//        Iterator<Integer> it2 = r.elements.listIterator();
//        int a = it1.next(), b = it2.next();
//        while (it1.hasNext() && it2.hasNext()) {
//            if (a < b){
//                a = it1.next();
//            } else if (a > b) {
//                b = it2.next();
//            } else if (a == b) {
//                result.elements.add(a);
//                a = it1.next();
//                b = it2.next();
//            }
//        }
//        return result;
//    }
//}
