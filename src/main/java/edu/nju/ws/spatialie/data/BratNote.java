package edu.nju.ws.spatialie.data;

public class BratNote {
    private String id;
    private String note;
    private String owner;

    public BratNote(String rawText) {
        if (!rawText.startsWith("#")) {
            System.err.println("Notes格式不正确！");
            return;
        }
        String [] t1 = rawText.trim().split("\t");
        String [] t2 = t1[1].split(" ");
        this.id = t1[0].trim();
        this.note = t1[2].trim();
        this.owner = t2[1];
    }

    public BratNote(String id, String note, String entityId) {
        this.id = id;
        this.note = note;
        this.owner = entityId;
    }


    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @Override
    public String toString() {
        return this.id + "\t" + "AnnotatorNotes " + owner + "\t" + note;
    }
}
