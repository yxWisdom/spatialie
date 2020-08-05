package edu.nju.ws.gqr;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class PriorityMapQueue<K, V> {
    private Map<K, V> map;
    private Queue<Pair<K, V>> queue;
    private Comparator<V> comparator;


    public PriorityMapQueue(Comparator<V> comparator) {
        this.map = new HashMap<>();
        Comparator<Pair<K, V>> queueComparator = (o1, o2) -> comparator.compare(o1.getRight(), o2.getRight());
        this.queue = new PriorityQueue<>(queueComparator);
        this.comparator = comparator;
    }

    public PriorityMapQueue() {
        this(null);
    }

    public Pair<K, V> peek() {
        return queue.peek();
    }

    public void add(Pair<K, V> pair) {
        if (comparator == null)
            addByComparable(pair);
        else
            addByComparator(pair);
        removeUselessElements();
    }

    private void addByComparable(Pair<K, V> pair) {
        K key = pair.getLeft();
        V value = pair.getRight();
        @SuppressWarnings("unchecked")
        Comparable<? super V> vCmp = (Comparable<? super V>) value;
        if (map.containsKey(key)) {
            V oldValue = map.get(key);
            if (vCmp.compareTo(oldValue) <= 0)
                return;
        }
        map.put(key, value);
        queue.offer(pair);
    }


    private void addByComparator(Pair<K, V> pair) {
        K key = pair.getLeft();
        V value = pair.getRight();
        if (map.containsKey(key)) {
            V oldValue = map.get(key);
            if (comparator.compare(value, oldValue) <= 0)
                return;
        }
        map.put(key, value);
        queue.offer(pair);
    }

    public Pair<K, V> remove() {
        if (queue.isEmpty())
            return null;
        Pair<K, V> pair = peek();
        queue.remove();
        map.remove(pair.getLeft());
        removeUselessElements();
        return pair;
    }

    private void removeUselessElements() {
        if (queue.isEmpty())
            return;
        Pair<K, V> element = queue.peek();
        K key = element.getLeft();
        V value = element.getRight();
        while (!value.equals(map.get(key)) && !queue.isEmpty()) {
            queue.poll();
            element = queue.peek();
            if (element == null)
                return;
            key = element.getLeft();
            value = element.getRight();
        }
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void clear() {
        this.queue.clear();
        this.map.clear();
    }
}
