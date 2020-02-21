package edu.nju.ws.spatialie.data;

public class BratRelation {
    private String id = null;
    private String tag =null;
    private BratEntity source = null;
    private BratEntity target = null;

    private String sourceId = null;
    private String targetId = null;

    public BratRelation(String id, String tag, BratEntity source, BratEntity target) {
        this.id = id.trim();
        this.tag = tag.trim();
        this.source = source;
        this.target = target;
    }

    public BratRelation(String id, String tag, String sourceId, String targetId) {
        this.id = id.trim();
        this.tag = tag.trim();
        this.sourceId = sourceId;
        this.targetId = targetId;
    }

    public BratRelation(String rawText) {
        String [] array = rawText.split("\t");
        this.id = array[0].trim();
        array = array[1].split(" ");
        this.tag = array[0].trim();
        this.sourceId = array[1].split(":")[1].trim();
        this.targetId = array[2].split(":")[1].trim();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public BratEntity getTarget() {
        return target;
    }

    public BratEntity getSource() {
        return source;
    }
}
