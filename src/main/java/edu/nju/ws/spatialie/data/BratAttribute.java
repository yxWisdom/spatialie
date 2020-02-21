package edu.nju.ws.spatialie.data;

public class BratAttribute {
    private String id;
    private String name;
    private String value;
    private String owner;

    public BratAttribute(String id, String name, String value, String owner) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.owner = owner;
    }

    public BratAttribute(String rawText) {
        if (!rawText.startsWith("A")) {
            System.err.println("Attribute格式不正确！");
            return;
        }
        String [] t1 = rawText.trim().split("\t");
        String [] t2 = t1[1].split(" ");
        this.id = t1[0];
        this.name = t2[0];
        this.owner = t2[1];
        this.value = t2[2];
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getOwner() {
        return owner;
    }

    void setOwner(String entityId) {
        this.owner = entityId;
    }


    @Override
    public String toString() {
        return id + "\t" + name + " " + owner + " " + value;
    }


    public boolean same(BratAttribute attr) {
        return name.equals(attr.getName()) && value.equals(attr.getValue());
    }


    @Override
    public BratAttribute clone() throws CloneNotSupportedException {
        return (BratAttribute) super.clone();
    }
}
