package edu.nju.ws.spatialie.data;

import java.util.*;

public class BratEntity {
    private String id = "";
    private String text = null;
    private String tag = null;
    private int start = -1;
    private int end = -1;
    private int count = 0;
    private List<BratAttribute> bratAttributes = null;
    private BratNote bratNote = null;
    private String filename = null;
    public int parent_count = 0;
    public BratEntity() {
    }

    private void init(String id, String text, String tag, int start, int end, String content) {
        while (start >= 0 && start < content.length() && content.charAt(start) == ' '){
            start++;
        }
        while(end >= 0 && end < content.length() && content.charAt(end-1) == ' '){
            end--;
        }
        assert text.equals(content.substring(start, end));
        this.id = id;
        this.text = text.trim();
        this.tag  = tag;
        this.start = start;
        this.end = end;
        this.bratAttributes = new ArrayList<>();
        this.count = 1;
    }

    public BratEntity(String id, String text, String tag, int start, int end) {
        init(id, text, tag, start, end, "");
    }

    public BratEntity(String rawText, String content) {
        String [] t1 = rawText.split("\t");
        String [] t2 = t1[1].split(" ");
        init(t1[0], t1[2], t2[0], Integer.parseInt(t2[1]), Integer.parseInt(t2[2]), content);
    }

    public BratEntity(String id, BratEntity bratEntity) {

    }

    public BratEntity(String rawText, String content, String filename) {
        this(rawText, content);
        this.filename = filename;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start =start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getEnd() {
        return end;
    }

    public String getAttr(String attrName) {
        for (BratAttribute bratAttribute : bratAttributes) {
            if (attrName.equals(bratAttribute.getName()))
                return bratAttribute.getValue();
        }
        return null;
    }

    public List<BratAttribute> getBratAttributes() {
        return bratAttributes;
    }

    public void setBratAttributes(List<BratAttribute> bratAttributes) {
        this.bratAttributes = bratAttributes;
    }

    @Override
    public String toString() {
        return id + "\t" + tag + " " + start + " " + end + "\t" + text;
    }

//    @Override
//    public int hashCode() {
//        return Objects.hash(text, tag);
//    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof BratEntity))
            return false;
        BratEntity bratEntity = (BratEntity)obj;
        boolean eq = text.equals(bratEntity.getText()) && tag.equals(bratEntity.getTag())
                && bratAttributes.size() == bratEntity.bratAttributes.size();
        if (!eq)
            return false;
        for(BratAttribute attr: bratAttributes) {
            if (!attr.getValue().equals(bratEntity.getAttr(attr.getName())))
                return false;
        }
        return true;
    }

    public boolean equalsWithoutAttributes(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof BratEntity))
            return false;
        BratEntity instance = (BratEntity)obj;
        return text.equals(instance.getText()) && tag.equals(instance.getTag());
    }

    public void increase() {
        count++;
    }

    public void decrease() {
        count = count==0 ? 0 : count-1;
    }

    public void setCount(int count) {
        this.count = count<=0 ? 0 : count;
    }

    public int getCount() {
        return count;
    }


    public boolean isNAM() {
        return !"NOM".equals(getAttr("form"));
    }

    public String getFilename() {
        return filename;
    }

    public BratNote getBratNote() {
        return bratNote;
    }

    public void setBratNote(BratNote bratNote) {
        this.bratNote = bratNote;
    }

    public List<String> toStringList() {
        List<String> list = new ArrayList<>();
        list.add(toString());
        for(BratAttribute bratAttribute : bratAttributes) {
            list.add(bratAttribute.toString());
        }
        if (bratNote != null) {
            list.add(bratNote.toString());
        }
        return list;
    }

    public void addAttribute(BratAttribute bratAttribute) {
        if (bratAttribute == null)
            return;
        for (BratAttribute a: bratAttributes) {
            if (a.getName().equals(bratAttribute.getName())) {
                a.setValue(bratAttribute.getValue());
                return;
            }
        }
        bratAttribute.setOwner(id);
        bratAttributes.add(bratAttribute);
    }

    @Override
    public BratEntity clone() throws CloneNotSupportedException {
        return (BratEntity)super.clone();
    }

    public int getParent_count() {
        return parent_count;
    }

    public void setParent_count(int parent_count) {
        this.parent_count = parent_count;
    }
}
