package edu.nju.ws.spatialie.spaceeval;

import org.checkerframework.checker.nullness.qual.NonNull;

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

    public String semantic_type="";

    Span(String id, String text, String label, int start, int end) {
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

    @Override
    public int compareTo(@NonNull Span span) {
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
