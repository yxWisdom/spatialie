package edu.nju.ws.spatialie.spaceeval;

import org.apache.xpath.operations.Bool;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xyu
 * @version 1.0
 * @date 2019/12/26 20:07
 */
public class Span implements Comparable<Span> {
    public String id;
    public String text;
    public String label;
    public int start;
    public int end;

    Map<String, String> attributeMap = new HashMap<>();
//    public String semantic_type="";

    public Span(String id, String text, String label, int start, int end) {
        this.id = id;
        this.text = text;
        this.label = label;
        this.start = start;
        this.end = end;
    }

    Span(Span span) {
        this.id = span.id;
        this.text = span.text;
        this.label = span.label;
        this.start = span.start;
        this.end = span.end;
    }

    public Boolean hasAttribute(String attrName){
        return attributeMap.containsKey(attrName);
    }

    public String getAttribute(String attrName) {
        return attributeMap.getOrDefault(attrName, null);
    }

    public void setAttribute(String attrName, String attrValue) {
        attributeMap.put(attrName, attrValue);
    }

    @Override
    public int compareTo(@NonNull Span span) {
//        return start == span.start ? Integer.compare(end, span.end) : Integer.compare(start, span.start);
        return Integer.compare(start, span.start);
    }

    @Override
    public String toString() {
        return id + " " + text + " " + label + " " + start + " " + end;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || this.getClass() != obj.getClass())
            return false;

        Span span = (Span) obj;

        return (this.id.equals(span.id)) && this.text.equals(span.text) && this.label.equals(span.label) &&
                this.start == span.start && this.end == span.end;
    }

}
