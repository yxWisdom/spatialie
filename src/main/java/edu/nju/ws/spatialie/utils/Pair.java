package edu.nju.ws.spatialie.utils;


import edu.stanford.nlp.util.CollectionUtils;

import java.io.DataOutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

public class Pair<T1, T2> implements Comparable<Pair<T1, T2>>, Serializable {
    public T1 first;
    public T2 second;
    private static final long serialVersionUID = 1360822168806852921L;

    public Pair() {
    }

    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public T1 first() {
        return this.first;
    }

    public T2 second() {
        return this.second;
    }

    public void setFirst(T1 o) {
        this.first = o;
    }

    public void setSecond(T2 o) {
        this.second = o;
    }

    public String toString() {
        return "(" + this.first + "," + this.second + ")";
    }



    public int hashCode() {
        int firstHash = this.first == null ? 0 : this.first.hashCode();
        int secondHash = this.second == null ? 0 : this.second.hashCode();
        return firstHash * 31 + secondHash;
    }

    public List<Object> asList() {
        return CollectionUtils.makeList(this.first, this.second);
    }

    public int compareTo(Pair<T1, T2> another) {
        if (this.first() instanceof Comparable) {
            int comp = ((Comparable)this.first()).compareTo(another.first());
            if (comp != 0) {
                return comp;
            }
        }

        if (this.second() instanceof Comparable) {
            return ((Comparable)this.second()).compareTo(another.second());
        } else if (!(this.first() instanceof Comparable) && !(this.second() instanceof Comparable)) {
            throw new AssertionError("Neither element of pair comparable");
        } else {
            return 0;
        }
    }
}
