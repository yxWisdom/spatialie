package edu.nju.ws.spatialie.data;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import edu.nju.ws.spatialie.getrelation.EveluateUtil;
import edu.nju.ws.spatialie.getrelation.FindLINK;
import edu.nju.ws.spatialie.getrelation.FindTagUtil;
import edu.nju.ws.spatialie.getrelation.JudgeEntity;

import java.util.*;

public class BratDocumentwithList extends BratDocument {

    public boolean c;
    List<BratEntity> entityList = new ArrayList<>();
    List<Boolean> isCandidate = new ArrayList<>();
    Multimap<String, String> companyMap = ArrayListMultimap.create();
    ParseTree parseTree;
    BratEntity trigger = null;

    public List<Boolean> getIsCandidate() {
        return isCandidate;
    }

    public BratDocumentwithList(List<String> samesentences) {
        super(samesentences.get(0).split("\t")[1]);
        String[] t = samesentences.get(0).split("\t");

        Map<String, String> tagMap = transTag();
        Map<Integer, BratEntity> idxEntity = buildEntities(t[1], t[2], tagMap);
        int idx = 0;
        for (String sentence :samesentences) {
            int trigger_pos = Integer.parseInt(sentence.split("\t")[0].split(" ")[0]);
            buildEvents(trigger_pos, idxEntity, sentence.split("\t")[3],idx);
            idx++;
        }
        int trigger_pos = Integer.parseInt(t[0].split(" ")[0]);
        if (trigger_pos != -1) trigger = idxEntity.get(trigger_pos);
        init();
    }

    public Multimap<String, String> getCompanyMap() {
        return companyMap;
    }

    public BratEntity getTrigger() {
        return trigger;
    }

    public BratDocumentwithList(String textPath, String annPath) {
        super(textPath, annPath);
        entityList = (ArrayList<BratEntity>) this.getEntityMap().values();
        Collections.sort(entityList, Comparator.comparingInt(BratEntity::getStart));
        for (BratEntity ignored : entityList) {
            isCandidate.add(true);
        }

        parseTree = new ParseTree(getContent());
    }

    public BratDocumentwithList(String text) {
        super(text.split("\t")[1]);
        String[] t = text.split("\t");
        int trigger_pos = Integer.valueOf(t[0].split(" ")[0]);
        Map<String, String> tagMap = transTag();
        Map<Integer, BratEntity> idxEntity = buildEntities(t[1], t[2], tagMap);
        buildEvents(trigger_pos, idxEntity, t[3]);
        if (trigger_pos != -1) trigger = idxEntity.get(trigger_pos);
        init();
        this.c = isComplete();
    }

    public BratDocumentwithList(JSONObject object,String content) {
        super(content);
        String text = FindTagUtil.findtag(content);
        if (text==null) return;
        text = text.replaceAll("approx /.","approx ~");
        text = text.replaceAll("approx /. /.","approx ~ ~");
        String[] t = text.split("\t");
        int trigger_pos = Integer.valueOf(t[0].split(" ")[0]);
        Map<String, String> tagMap = transTag();
        Map<Integer, BratEntity> idxEntity = buildEntities(t[1], t[2], tagMap);
        init();
        BratEntity trigger = EveluateUtil.getEntity(this,object.getJSONObject("t"));
        if (trigger.getTag().equals(BratUtil.SPATIAL_SIGNAL)||trigger.getTag().equals(BratUtil.MEASURE)||trigger.getTag().equals(BratUtil.MOTION))
            this.trigger = trigger;
        else
            this.trigger = null;
//        this.trigger = null;
    }

    private Map transTag() {
        List<String> lines = Arrays.asList(("DATE Date\n" +
                "TIME Time\n" +
                "DURATION Duration\n" +
                "TIME_SET TimeSet\n" +
                "PLACE Place\n" +
                "MILITARY_PLACE MilitaryPlace\n" +
                "MILITARY_BASE MilitaryBase\n" +
                "COUNTRY Country\n" +
                "ADMIN_DIV AdministrativeDivision\n" +
                "PATH Path\n" +
                "P_MILITARY_PLACE P_MilitaryPlace\n" +
                "ORGANIZATION Organization\n" +
                "ARMY Army\n" +
                "PERSON Person\n" +
                "COMMANDER Commander\n" +
                "WEAPON Weapon\n" +
                "NONMOTION_EVENT Event\n" +
                "MILITARY_EXERCISE MilitaryExercise\n" +
                "CONFERENCE Conference\n" +
                "SPATIAL_ENTITY SpatialEntity\n" +
                "MEASURE Measure\n" +
                "SPATIAL_SIGNAL SpatialSignal\n" +
                "MOTION Motion\n" +
                "SpatialFlag SpatialFlag\n" +
                "MOTION_SIGNAL MotionSignal\n" +
                "LITERAL Literal").split("\n"));
        Map<String, String> tagMap = new HashMap<>();
        for (String line : lines) {
            tagMap.put(line.split(" ")[0], line.split(" ")[1]);
        }
        return tagMap;
    }

    private void buildEvents(int trigger_pos, Map<Integer, BratEntity> idxEntity, String labels) {
        String[] labelList = labels.split(" ");
        BratEvent newEvent = new BratEvent();
        newEvent.setId("A1");
        newEvent.setType("OTLINK");//无trigger
        for (int i = 0; i < labelList.length; i++) {
            if (labelList[i].startsWith("B")) {
                String label = labelList[i].substring(2);
                BratEntity entity = idxEntity.get(i);
                newEvent.addRole(label, entity.getId());
                newEvent.setEntities(entity.getId(), entity);
                if (i == trigger_pos) {
                    if (entity.getTag().equals(BratUtil.MOTION)) {
                        newEvent.setType("MLINK");
                    } else if (entity.getTag().equals(BratUtil.MEASURE)) {
                        newEvent.setType("DLINK");
                        newEvent.removeRole(label);
                        newEvent.addRole("val", entity.getId());
                    } else if (entity.getTag().equals(BratUtil.SPATIAL_SIGNAL)||entity.getTag().equals("SpatialFlag")) {
                        newEvent.setType("OTLINK");
                    }
                }
            }
        }
        getEventMap().put(newEvent.getId(), newEvent);
    }

    private void buildEvents(int trigger_pos, Map<Integer, BratEntity> idxEntity, String labels,int idx) {
        String[] labelList = labels.split(" ");
        BratEvent newEvent = new BratEvent();
        newEvent.setId("A"+idx);
        newEvent.setType("OTLINK");//无trigger
        for (int i = 0; i < labelList.length; i++) {
            if (labelList[i].startsWith("B")) {
                String label = labelList[i].substring(2);
                BratEntity entity = idxEntity.get(i);
                newEvent.addRole(label, entity.getId());
                newEvent.setEntities(entity.getId(), entity);
                if (i == trigger_pos) {
                    if (entity.getTag().equals(BratUtil.MOTION)) {
                        newEvent.setType("MLINK");
                    } else if (entity.getTag().equals(BratUtil.MEASURE)) {
                        newEvent.setType("DLINK");
                        newEvent.removeRole(label);
                        newEvent.addRole("val", entity.getId());
                    } else if (entity.getTag().equals(BratUtil.SPATIAL_SIGNAL)) {
                        newEvent.setType("OTLINK");
                    }
                }
            }
        }
        getEventMap().put(newEvent.getId(), newEvent);
    }

    private Map<Integer, BratEntity> buildEntities(String text, String tags, Map<String, String> tagMap) {
        int pos = 0;
        String[] words = text.split(" ");
        String[] tagList = tags.split(" ");
        int idx = 0;
        int id_idx = 0;
        Map<Integer, BratEntity> res = new HashMap<>();
        while (true) {
            if (!tagList[idx].equals("O")) {
                String tag = tagList[idx].split("-")[1];
                BratEntity newEntity = new BratEntity();
                newEntity.setTag(tagMap.get(tag));
                String entity_text = "";
                newEntity.setStart(pos);
                res.put(idx, newEntity);
                boolean ifb = false;
                while (tagList[idx].endsWith(tag) && (!ifb || tagList[idx].startsWith("I"))) {
                    entity_text = entity_text + " " + words[idx];
                    if (tagList[idx].startsWith("B")) ifb = true;
                    pos += words[idx].length() + 1;
                    idx++;
                    if (idx >= tagList.length) break;
                }
                newEntity.setEnd(pos - 1);
                newEntity.setText(entity_text.substring(1));
                newEntity.setId("T" + id_idx);
                id_idx++;
                addEntity(newEntity);
            } else {
                pos += words[idx].length() + 1;
                idx++;
            }
            if (idx >= tagList.length) break;
        }
        return res;
    }

    private void addEntity(BratEntity newEntity) {
        getEntityMap().put(newEntity.getId(), newEntity);
    }

    public void init() {
        for (BratEntity e : this.getEntityMap().values()) {
            entityList.add(e);
        }
        Collections.sort(entityList, Comparator.comparingInt(BratEntity::getStart));
        for (BratEntity ignored : entityList) {
            if (ignored.getTag().equals(BratUtil.MOTION_SIGNAL)) isCandidate.add(false); else isCandidate.add(true);
        }
        String content = getContent().replaceAll("\\.","~");
        content = content.replaceAll("\\?","~");
        content = content.substring(0,content.length()-1)+".";
        parseTree = new ParseTree(content);
    }

    public ParseTree getParseTree() {
        return parseTree;
    }

    public List<BratEntity> getEntityList() {
        return entityList;
    }

    public boolean getIsCandidate(int idx) {
        return isCandidate.get(idx);
    }

    public void noCandidate(int idx) {
        isCandidate.set(idx, false);
    }

    public void dealCompany() {
        for (int i = 0; i < entityList.size() - 1; i++) {
            BratEntity e1 = entityList.get(i);
            BratEntity e2 = entityList.get(i + 1);
            // village B...
            if (e1.getEnd() + 1 == e2.getStart()) {
                if (e1.getTag().equals(BratUtil.PLACE) && e2.getTag().equals(BratUtil.PLACE)) {
                    if (e2.getText().matches("[A-Z].*")) {
                        noCandidate(i + 1);
                    }
                }
            }
        }



        for (int i = 0; i < entityList.size() - 1; i++) {
            if (!getIsCandidate(i)) continue;
            BratEntity e1 = entityList.get(i);
            if (FindLINK.getNext(i,this)==-1) break;
            BratEntity e2 = entityList.get(FindLINK.getNext(i, this));
            // up to
            if (FindLINK.hasNoNV(this, i, FindLINK.getNext(i, this))) {
                if (e1.getTag().equals(BratUtil.SPATIAL_SIGNAL) && e2.getTag().equals(BratUtil.SPATIAL_SIGNAL)) {
                    noCandidate(FindLINK.getNext(i, this));
                    companyMap.put(e1.getId(), e2.getId());
                }
            }
        }

        // we(Susan, Jam and me)
        int i = 0;
        while (i < entityList.size()) {
            BratEntity e = entityList.get(i);
            if (FindLINK.getNext(i, this)==-1) break;
            if (getIsCandidate(i) && e.getTag().equals(BratUtil.SPATIAL_ENTITY)) {
                if (getNextWord(e.getEnd()).equals("(")) {
                    int j = FindLINK.getNext(i, this);
                    BratEntity e2;
                    while (j!=-1&&j < entityList.size()) {
                        e2 = entityList.get(j);
                        if (!e2.getTag().equals(BratUtil.SPATIAL_ENTITY) ||
                                getContent().substring(e.getEnd(), e2.getStart()).contains(")")) break;
                        j = FindLINK.getNext(j, this);
                    }
                    if (j > FindLINK.getNext(i, this)) {
                        for (int k = FindLINK.getNext(i, this); k < j; k = FindLINK.getNext(k, this)) {
                            companyMap.put(e.getId(), entityList.get(k).getId());
                            noCandidate(k);
                        }
                    }
                    i = j;
                    if (i==-1) break;
                    continue;
                }
            }
            i=FindLINK.getNext(i, this);
            if (i==-1) break;
        }

        //A [v]... with B
        //A,B and C
        i = 0;
        List<Integer> possible_company = new ArrayList<>();
        while (i < entityList.size()) {
            int it = i;
            BratEntity start = entityList.get(it);
            int i_next = FindLINK.getNext(it, this);
            if (i_next == -1) break;
            BratEntity next = entityList.get(i_next);
            possible_company.clear();
            boolean ok = false;
            while (JudgeEntity.canBeTogether(start, next) && FindLINK.getSegment(this, it, i_next).contains(",")
                    && !FindLINK.hasPOS("N", this, it, i_next) && !FindLINK.hasPOS("V", this, it, i_next)) {
                possible_company.add(i_next);
                int i_next_t = FindLINK.getNext(i_next, this);
                if (i_next_t == -1) break;
                BratEntity next_t = entityList.get(i_next_t);
                if (JudgeEntity.canBeTogether(start, next_t) && FindLINK.getSegment(this, i_next, i_next_t).contains("and")
                        && !FindLINK.hasPOS("N", this, i_next, i_next_t) && !FindLINK.hasPOS("V", this, i_next, i_next_t)) {
                    possible_company.add(i_next_t);
                    ok = true;
                    break;
                }
                it = i_next;
                start = next;
                i_next = i_next_t;
                next = next_t;
            }
            if (ok) {
                for (int idx : possible_company) {
                    noCandidate(idx);
                    companyMap.put(entityList.get(i).getId(), entityList.get(idx).getId());
                }
                i = i_next;
            } else {
                i = FindLINK.getNext(i, this);
            }
        }
        companyMap = getParseTree().getSemanticCompany(this);
    }

    public String getNextWord(int pos) {
        int pos2 = getContent().indexOf(' ', pos + 1);
        return pos2 == -1 ? getContent().substring(pos + 1) : getContent().substring(pos + 1, pos2);
    }

    public BratEntity getEntitybyID(String companyid) {
        for (BratEntity e : entityList) {
            if (e.getId().equals(companyid)) return e;
        }
        return null;
    }

    public boolean isComplete() {
        BratEvent e = getEventMap().get("A1");
        if (e.getType().equals("MLINK")||trigger==null) {
            return (e.getRoleMap().keySet().size() >= 2);
        } else {
            return (e.getRoleMap().keySet().size() >= 3);
        }
    }

    public boolean isComplete(BratEvent e) {
        if (e.getType().equals("MLINK")||trigger==null) {
            return (e.getRoleMap().keySet().size() >= 2);
        } else {
            return (e.getRoleMap().keySet().size() >= 3);
        }
    }
}
