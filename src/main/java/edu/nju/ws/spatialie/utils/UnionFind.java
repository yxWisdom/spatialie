package edu.nju.ws.spatialie.utils;

import java.util.*;

/**
 * @author xyu
 * @version 1.0
 * @date 2019/12/26 22:10
 */

// 并查集
public class UnionFind<T> {
    private Map<T, Integer> objToIdMap;
    private Map<Integer, T> idToObjMap;
//    private List<T> objects;
    private List<Integer> parent;
    private List<Integer> rank;
    private int capacity;
    private int size;

    public UnionFind() {
       this(1000);
    }

    private UnionFind(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        idToObjMap = new HashMap<>();
        objToIdMap = new HashMap<>();
        parent = new ArrayList<>(this.capacity);
        rank = new ArrayList<>(this.capacity);
        for (int i=0; i < this.capacity; i++) {
            this.parent.add(i);
            this.rank.add(0);
        }
    }

    private void check(T t) {
        if (!objToIdMap.containsKey(t)) {
            int id = size;
            size++;
            objToIdMap.put(t, id);
            idToObjMap.put(id, t);
        }
    }

    private int _find(int x) {
        if (parent.get(x) == x) {
            return x;
        }
        else {
            parent.set(x, _find(parent.get(x)));
            return parent.get(x);
        }
    }

    private void _unite(int x, int y) {
        x = _find(x);
        y = _find(y);

        if (x == y) return;

        if (rank.get(x) < rank.get(y)) {
            parent.set(x, y);
        } else {
            parent.set(y, x);
            if (rank.get(x).equals(rank.get(y)))
                rank.set(x, rank.get(x) + 1);
        }
    }

    public int find(T t) {
        check(t);
        return _find(objToIdMap.get(t));
    }

    public void unite(T t1, T t2) {
        check(t1);
        check(t2);
        _unite(objToIdMap.get(t1), objToIdMap.get(t2));
    }

    public boolean same(T t1, T t2) {
        return find(t1) == find(t2);
    }

    public List<Set<T>> getDisjointSet() {
        Map<Integer, Set<T>> idToSetMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            int id = _find(i);
            idToSetMap.putIfAbsent(id, new HashSet<>());
            idToSetMap.get(id).add(idToObjMap.get(i));
        }
        return new ArrayList<>(idToSetMap.values());
    }
}
