package edu.nju.ws.spatialie.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import edu.stanford.nlp.util.ArrayMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.text.html.parser.Entity;
import java.util.*;
import java.util.stream.Collectors;

public class BratEvent implements Cloneable{
    private String id = null;
    private String filename = null;
    private String type = null;
    private String sentence = null;
    private int start = -1; // 句子的起点
    private int end = -1;
    private int t_start = -1;  // token的起点
    private int t_end = -1;
    private BratNote bratNote = null;
    private Multimap<String, String> roleMap = null;
    private Map<String, BratEntity> entities = null;
    private List<BratAttribute> bratAttributes = null;
    private boolean hasTrigger = true;
    private String ruleid = null;

    @Override
    public BratEvent clone() throws CloneNotSupportedException {
        BratEvent bratEvent = (BratEvent) super.clone();
        if (this.bratAttributes != null)
            bratEvent.bratAttributes = new ArrayList<>(this.bratAttributes);
        if (this.roleMap != null)
            bratEvent.roleMap = ArrayListMultimap.create(this.roleMap);
        if (this.entities != null)
            bratEvent.entities = new HashMap<>(this.entities);
        return bratEvent;
    }

    public BratEvent() {
        this.bratAttributes = new ArrayList<>();
        this.roleMap = ArrayListMultimap.create();
        this.entities = new ArrayMap<>();
    }

    public BratEvent(String text) {
        this(text, "");
    }

    public BratEvent(String text, String filename) {
        String [] splits = text.split("\t");
        String id = splits[0].trim();
        List<String> rolePairs= Arrays.asList(splits[1].split(" "));
        String eventType =  rolePairs.get(0).split(":")[0].trim();
        rolePairs = rolePairs.subList(1, rolePairs.size());

        this.id = id;
        this.filename = filename;
        this.type = eventType;
        this.roleMap = ArrayListMultimap.create();
        this.entities = new HashMap<>();
        this.bratAttributes = new ArrayList<>();
        for (String rel: rolePairs) {
            String [] pair = rel.split(":");
            this.roleMap.put(pair[0].trim().replaceAll("\\d+$", ""), pair[1].trim());
        }
    }

    public void addRole(String role, String entityId) {
        roleMap.put(role, entityId);
    }

    public void removeRole(String role) {
        roleMap.removeAll(role);
    }

    public void removeRole(String role, String entityId) {
        roleMap.remove(role, entityId);
    }

    public void addEntity(BratEntity bratEntity) {
        entities.putIfAbsent(bratEntity.getId(), bratEntity);
    }

    private static void crossJoin(List<Pair<String, List<String>>> groups, List<BratEvent> res, BratEvent tmpEvent, int pos)
            throws CloneNotSupportedException {
        if (pos == groups.size()) {
            res.add(tmpEvent.clone());
            return;
        }
        Pair<String, List<String>> pair = groups.get(pos);
        String type = pair.getLeft();
        for (String entity_id: pair.getRight()) {
            tmpEvent.addRole(type, entity_id);
            crossJoin(groups, res, tmpEvent, pos + 1);
            tmpEvent.removeRole(type);
        }

    }

    // 产生每个角色只有一个实体的实例
    public List<BratEvent> parse() throws CloneNotSupportedException {
        List<Pair<String, List<String>>> roleGroups = this.roleMap.keySet().stream()
                .map(k -> new ImmutablePair<String,  List<String>>(k, new ArrayList<>(roleMap.get(k))))
                .collect(Collectors.toList());
        BratEvent tmpEvent = this.clone();
        List<BratEvent> events = new ArrayList<>();
        crossJoin(roleGroups, events, tmpEvent, 0);
        return events;
    }

    public void setSentence(String content, Collection<BratEntity> entities, int maxLength) {
        int start = Integer.MAX_VALUE, end = Integer.MIN_VALUE;
        for (BratEntity entity: entities) {
            start = Math.min(start, entity.getStart());
            end = Math.max(end, entity.getEnd());
        }

        this.t_start = start;
        this.t_end = end;

        int line_start = start, line_end = end;
        while(line_start > 0 && content.charAt(line_start) != '\n')  line_start--;
        while(line_end < content.length() && content.charAt(line_end) != '\n' ) line_end++;

        if (line_end - line_start <= maxLength) {
            start = line_start;
            end = line_end;
        } else {
            Set<Character> punctuations = new HashSet<>(Arrays.asList('?', '!', '。','？', '！', '\n', ',', ';', '，', '；'));
            while (true) {
                int tmp_start = start>0 ? start-1 : start, tmp_end = end+1;
                while(tmp_start > line_start && !punctuations.contains(content.charAt(tmp_start)))  tmp_start--;
                if (end-tmp_start <= maxLength)
                    start = tmp_start;
                while(tmp_end <= line_end && !punctuations.contains(content.charAt(tmp_end-1)))  tmp_end++;
                if (tmp_end - start <= maxLength)
                    end = tmp_end;
                else break;;
            }
        }
        if (content.charAt(start) == '\n' || content.charAt(start) == '\r') start++;
        if (content.charAt(end-1) == '\n' || content.charAt(end-1) == '\r') end--;
        this.sentence = content.substring(start,end);
        this.start = start;
        this.end = end;

    }

    public void setSentence(String content, int maxLength) {
        this.setSentence(content, this.entities.values(), maxLength);
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
    public String toString() {
        return this.getId();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if ((obj == null) || (obj.getClass() != this.getClass())) return false;

        BratEvent event = (BratEvent) obj;

        if (this.id != null && !this.id.equals(event.id)) return false;

        for (String key: roleMap.keySet()) {
            if (!event.roleMap.containsKey(key)) return false;
            Set<String> entitySet = new HashSet<>(roleMap.get(key));
            entitySet.removeAll(event.roleMap.get(key));
            if (entitySet.size()!=0) return false;
        }
        return true;
    }

//    public void setEntities(Collection<BratEntity> entities) {
//        for (BratEntity entity: entities) {
//            this.entities.putIfAbsent(entity.getId(), entity);
//        }
//    }

    public Collection<String> getRoleIds(String roleName) {
        return roleMap.get(roleName);
    }

    public String getRoleId(String roleName) {
        List<String> roleIds = new ArrayList<>(roleMap.get(roleName));
        if (roleIds.size() != 0) return roleIds.get(0);
        else return "";
    }

    public boolean hasRole(String roleName) {
        return this.roleMap.containsKey(roleName);
    }


    public Multimap<String, String> getRoleMap() {
        return roleMap;
    }

    public void setRoleMap(Multimap<String, String> roleMap) {
        this.roleMap = roleMap;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BratNote getBratNote() {
        return bratNote;
    }

    public void setBratNote(BratNote bratNote) {
        this.bratNote = bratNote;
    }

    public String getSentence() {
        return sentence;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getT_start() {
        return t_start;
    }

    public int getT_end() {
        return t_end;
    }

    public String getAttribute(String attrName) {
        for (BratAttribute bratAttribute: this.bratAttributes) {
            if (bratAttribute.getName().equals(attrName)) return bratAttribute.getValue();
        }
        return "";
    }

    public boolean isHasTrigger() {
        return hasTrigger;
    }

    public void setHasTrigger(boolean hasTrigger) {
        this.hasTrigger = hasTrigger;
    }

    public static void main(String [] args) {
        BratEvent e1 = new BratEvent(),  e2 = new BratEvent();
        e1.setId("T1");
        e2.setId("T1");
        e1.addRole("a", "T1");
        e2.addRole("a","T1");
        System.out.println(e1.equals(e2));

    }

    public void setEntities(String id, BratEntity e) {
        entities.put(id,e);
    }

    public void setMembers(String val, String id) {
        addRole(val,id);
    }

    public Multimap<String, String> getMembers() {
        return roleMap;
    }

    public String getMemberId(String duplicate) {
        return ((List<String>)getRoleIds(duplicate)).get(0);
    }

    public Map<String, BratEntity> getEntities() {
        return entities;
    }

    public void setT_start_end(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public boolean is(Object obj)
    {
        if (this == obj) return true;
        if ((obj == null) || (obj.getClass() != this.getClass())) return false;

        BratEvent event = (BratEvent) obj;

//        if (this.id != null && !this.id.equals(event.id)) return false;

        for (String key: roleMap.keySet()) {
            if (!event.roleMap.containsKey(key)) return false;
            Set<String> entitySet = new HashSet<>(roleMap.get(key));
            entitySet.removeAll(event.roleMap.get(key));
            if (entitySet.size()!=0) return false;
        }
        return true;
    }

    public void removeEntity(String id){
        entities.remove(id);
    }

    public String getRuleid() {
        return ruleid;
    }

    public void setRuleid(String ruleid) {
        this.ruleid = ruleid;
    }
}
