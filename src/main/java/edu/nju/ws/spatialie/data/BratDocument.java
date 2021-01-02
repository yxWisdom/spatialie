package edu.nju.ws.spatialie.data;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import edu.nju.ws.spatialie.utils.FileUtil;
import org.apache.commons.lang3.tuple.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static edu.nju.ws.spatialie.data.BratUtil.subtypeMap;


public class BratDocument {

    private String filename;
    private String content;
    private Map<String, BratEntity> entityMap = new HashMap<>();
    private Map<String, BratRelation> relationMap = new HashMap<>();
    private Map<String, BratEvent> eventMap = new HashMap<>();

    public BratDocument(String text) {
        content = text;
    }

    private boolean isEntity(String id) {
        return id.startsWith("T");
    }

    private boolean isAttribute(String id) {
        return id.startsWith("A");
    }

    private boolean isNote(String id) {
        return id.startsWith("#");
    }

    private boolean isEvent(String id) {
        return id.startsWith("E");
    }

    private boolean isRelation(String id) {
        return id.startsWith("R");
    }


    public Map<String, BratEntity> getEntityMap() {
        return entityMap;
    }

    public Map<String, BratRelation> getRelationMap() {
        return relationMap;
    }

    public Map<String, BratEvent> getEventMap() {
        return eventMap;
    }

    public String getContent(){
        return content;
    }

    public BratDocument(String textPath, String annPath)  {
        this.filename = textPath;
        this.content = FileUtil.readFile(textPath);
        List<String> lines = FileUtil.readLines(annPath);
        Map<String, String> eventLinesMap = new HashMap<>();
        Map<String, String> coreferenceMap = new HashMap<>();

        Map<String, BratNote> noteMap = new HashMap<>();
        Map<String, BratAttribute> attributeMap = new HashMap<>();

        for (String line: lines) {
            if (isEntity(line)) {
                BratEntity bratEntity = new BratEntity(line, content);
                entityMap.put(bratEntity.getId(), bratEntity);
            }
            if (isAttribute(line)){
                BratAttribute bratAttribute = new BratAttribute(line);
                attributeMap.put(bratAttribute.getId(), bratAttribute);
            }
            if (isNote(line)) {
                BratNote bratNote = new BratNote(line);
                noteMap.put(bratNote.getId(), bratNote);
            }
            if (isRelation(line)) {
                BratRelation bratRelation = new BratRelation(line);
                relationMap.put(bratRelation.getId(), bratRelation);
                if (bratRelation.getTag().equals("coreference")) {
                    coreferenceMap.put(bratRelation.getSourceId(), bratRelation.getTargetId());
                    coreferenceMap.put(bratRelation.getTargetId(), bratRelation.getSourceId());
                }
            }
            if (isEvent(line)) {
                String id = line.split("\\s")[0];
                eventLinesMap.put(id, line);
            }
        }
        // 解析BratEvent
        for (Map.Entry<String, String> entry: eventLinesMap.entrySet()) {
            String id = entry.getKey(), line = entry.getValue();
            BratEvent event = new BratEvent(line, filename);
            if (event.getRoleMap().size() == 0) {
                if (!coreferenceMap.containsKey(id)) continue;
                String coreferenceId = coreferenceMap.get(id);
                String coreferenceLine = eventLinesMap.get(coreferenceId);
                String mergeLine = line + coreferenceLine.substring(coreferenceLine.indexOf(" "));
                event = new BratEvent(mergeLine, filename);
            }
            eventMap.put(event.getId(), event);
        }

        // 链接属性
        for (Map.Entry<String, BratAttribute> entry: attributeMap.entrySet()) {
            BratAttribute  attribute= entry.getValue();
            String owner = attribute.getOwner();
            if (entityMap.containsKey(owner)) {
                entityMap.get(owner).addAttribute(attribute);
            }
            if (eventMap.containsKey(owner)) {
                eventMap.get(owner).addAttribute(attribute);
            }
        }

        // 链接备注
        for (Map.Entry<String, BratNote> entry: noteMap.entrySet()) {
            BratNote note= entry.getValue();
            String owner = note.getOwner();
            if (entityMap.containsKey(owner)) {
                entityMap.get(owner).setBratNote(note);
            }
            if (eventMap.containsKey(owner)) {
                eventMap.get(owner).setBratNote(note);
            }
        }
        // 添加没有触发词的关系
        for (BratRelation relation: relationMap.values()) {
            if (relation.getTag().equals("partOf") || relation.getTag().equals("locatedIn")) {
                BratEvent bratEvent = new BratEvent();
                bratEvent.setId(relation.getId());
                bratEvent.setType("TLINK");
                bratEvent.addRole("trajector", relation.getSourceId());
                bratEvent.addRole("landmark", relation.getTargetId());
                bratEvent.addAttribute(new BratAttribute("", "QS_type", "IN", bratEvent.getId()));
                bratEvent.setHasTrigger(false);
                eventMap.put(bratEvent.getId(), bratEvent);
            }
        }

        // 设置每个LINK所在的句子
        int max_len = 0;
        for (Map.Entry<String, BratEvent> entry: eventMap.entrySet()) {
            BratEvent bratEvent = entry.getValue();
            for (String eid: bratEvent.getRoleMap().values()) {
                bratEvent.addEntity(entityMap.get(eid));
            }
            bratEvent.setSentence(content, 96);
            max_len = Math.max(bratEvent.getSentence().length(), max_len);
        }

//        for (String eventId: eventMap.keySet()) {
//            Collection<BratEvent> bratEvents = eventMap.get(eventId);
//            List<BratEntity> members = bratEvents.stream().flatMap(o->o.getRoleMap().values().stream())
//                    .distinct().map(o->this.entityMap.get(o)).collect(Collectors.toList());
//            for (BratEvent bratEvent: bratEvents) {
//                for (String entityId: bratEvent.getRoleMap().values()) {
//                    bratEvent.addEntity(entityMap.get(entityId));
//                }
//                bratEvent.setSentence(content, members, 96);
//                max_len = Math.max(bratEvent.getSentence().length(), max_len);
//            }
//        }
//        System.out.println(max_len);
//        List<BratEvent> bratEvents = eventMap.entries().stream().flatMap(o -> o.getValue().stream()).collect(Collectors.toList())
    }

    private boolean isChar(char ch) {
        return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z');
    }

    private boolean isCharOrNumber(char ch) {
        return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z') || ('0' <= ch && ch <= '9') || ch=='-';
    }


    // 生成CoNLL2003格式的语料
    public List<Pair<String, List<String[]>>> tokenRows(Set<String> types, int max_len) {
        List<BratEntity> entities = entityMap.values().stream().filter(x -> types.contains(x.getTag()))
                .sorted(Comparator.comparingInt(BratEntity::getStart))
                .collect(Collectors.toList());

        List<String> labels = new ArrayList<>();
        for (int i=0; i<content.length();i++) {
            labels.add("O");
        }

        for (BratEntity entity: entities) {
            int start = entity.getStart(), end = entity.getEnd();
            for (int i=start; i < end; i++) {
                String prefix = (i == start) ? "B-" : "I-";
                String tag = subtypeMap.getOrDefault(entity.getTag(), entity.getTag());
                labels.set(i,  prefix + tag);
            }
        }

        List<String []> tmp_rows = new ArrayList<>();
        List<Integer> tokenPos = new ArrayList<>();
        for (int i = 0; i < content.length();i++) {
            char ch = content.charAt(i);
            tokenPos.add(i);
            if (isChar(ch)) {
                int j = i + 1;
                while(isChar(content.charAt(j))) {
                    j++;
                }
                tmp_rows.add(new String [] {content.substring(i, j).replaceAll("\\s", ""), labels.get(i)});
                i = j - 1;
            } else {
                if (ch == '\n'){
                    tmp_rows.add(new String [] {"\n", ""});
                } else if (ch == ' '){
                    tmp_rows.add(new String [] {"", ""});
                } else  {
                    tmp_rows.add(new String [] {String.valueOf(ch), labels.get(i)});
                }
            }
        }
        tokenPos.add(content.length());

        List<Pair<Set<String>, List<Integer>>> punctuationGroup = new ArrayList<Pair<Set<String>, List<Integer>>>(){{
            add(new MutablePair<>(new HashSet<>(Arrays.asList("?", "!", "。","？", "！", "\n")), new ArrayList<>()));
            add(new MutablePair<>(new HashSet<>(Arrays.asList(";", "；", "】")), new ArrayList<>()));
            add(new MutablePair<>(new HashSet<>(Arrays.asList(",", "，", "")), new ArrayList<>()));
            add(new MutablePair<>(new HashSet<>(Arrays.asList("、", "》", "”", "——", "（", "）", "——", "—")), new ArrayList<>()));
        }};

        int current_pos = 0, offset = 0, next_pos=0, current_status=3;
        List<Pair<String, List<String []>>> rows_group = new ArrayList<>();

        while(current_pos + offset < tmp_rows.size()) {
            String token = tmp_rows.get(current_pos + offset)[0];
            if (offset == 0 && token.equals("")) {
                current_pos += 1;
                continue;
            }
            for (int i = 0; i <= current_status; i++) {
                if (punctuationGroup.get(i).getLeft().contains(token)) {
//                    delimit_pos = offset;
                    current_status = i;
                    punctuationGroup.get(i).getRight().add(0, offset);
                    break;
                }
            }
            offset++;
            if (offset >= max_len || current_status == 0) {
                List<Integer> punctuations = punctuationGroup.stream().map(Pair::getRight)
                        .flatMap(Collection::stream).collect(Collectors.toList());
                if (punctuations.size() == 0) System.out.println("error");
                int status = 0;
                for (Integer delimit: punctuations) {
                    status = 0;
                    next_pos = current_pos + delimit + 1;
                    for (Map.Entry<String, BratEvent> entry: eventMap.entrySet()) {
                        int e_start = entry.getValue().getT_start(),e_end = entry.getValue().getT_end();
                        int start_pos = tokenPos.get(current_pos), end_pos = tokenPos.get(next_pos);
                        if (start_pos <= e_start && e_end <= end_pos) {
                            status = 1;
                        }
                        if (!(start_pos <= e_start && e_end <= end_pos || e_end <= start_pos || e_start >= end_pos)) {
                            status = -1;break;
                        }
                    }
                    if (status >= 0) break;
                }
//                String text = content.substring(current_pos, next_pos);
                if (status < 0) {
                    next_pos = current_pos + punctuations.get(0) + 1;;
                    status = 1;
                }
                List<String []> filter_rows = tmp_rows.subList(current_pos, next_pos).stream().
                        filter(x->x[0].trim().length()>0).collect(Collectors.toList());
                if (filter_rows.size() > 0)
                    rows_group.add(new ImmutablePair<>(String.valueOf(status),filter_rows));
                current_pos = next_pos;
                offset = 0;
                current_status = 3;
                punctuationGroup.forEach(x -> x.getRight().clear());
            }
        }
//        if (j > 0) {
//            String seqType = "0";
//            for (Map.Entry<String, BratEvent> entry: eventMap.entries()) {
//                int start = entry.getValue().getStart(),end = entry.getValue().getEnd();
//                if (start >= i && end <= tmp_rows.size()) {
//                    seqType = "1";
//                }
//            }
//            List<String []> filter_rows = tmp_rows.subList(i, tmp_rows.size())
//                    .stream().filter(o->o[0].length()>0 && !o[0].equals("\n")).collect(Collectors.toList());
//            rows_group.add(new ImmutablePair<>(seqType,filter_rows));
//        }

//        assert rows_group.size() == seqTypeList.size();

        return rows_group;
    }


//    public void filterEntity(Set<String> entityTypeSet, boolean isNAM) {
//        for(Map.Entry<String, BratEntity> entry: entityMap.entrySet()) {
//            BratEntity entity = entry.getValue();
//            if (entityTypeSet.contains(entity.getTag())) {
//                entityMap.remove(entry.getKey());
//            }
//            if (isNAM && !entity.isNAM()) {
//                entityMap.remove(entry.getKey());
//            }
//        }
//    }

    class Token {
        String token;
        int begin;
        int end;
        Token(String token, int begin, int end) {
            this.token = token;
            this.begin = begin;
            this.end = end;
        }

        @Override
        public String toString() {
            return token + "  " + begin + " " + end;
        }
    }

    public List<Token> tokens(String sentence) {

//        if (sentence.contains("IS的圣战者在伦"))
//            System.out.println(1);

        sentence = sentence.replaceAll("\\s", " ");
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < sentence.length();i++) {
            char ch = sentence.charAt(i);
            if (isCharOrNumber(ch)) {
                int j= i + 1;
                while(j < sentence.length() && isCharOrNumber(sentence.charAt(j))) {
                    j++;
                }
                tokens.add(new Token(sentence.substring(i, j), i, j));
                i = j - 1;
            } else if(ch != ' ') {
                tokens.add(new Token(String.valueOf(ch), i, i + 1));
            }
        }
        return tokens;
    }

    private String getRelLine(String status, BratEvent event, String...ids) {
        StringBuilder line = new StringBuilder();
        line.append(status).append(" ");
        int  notFind = 0;

        String sentence = event.getSentence();
        List<Token> tokens = this.tokens(sentence);
        List<String> tokenStr = tokens.stream().map(t -> t.token).collect(Collectors.toList());

        for(String id: ids) {
            int startIdx = -1, endIdx = -1;
            if (entityMap.containsKey(id)) {
                BratEntity entity = entityMap.get(id);
                int begin = entity.getStart() - event.getStart(), end = entity.getEnd() - event.getStart();
                for (int i = 0; i < tokens.size(); i++) {
                    Token token = tokens.get(i);
                    if (begin == token.begin) {
                        startIdx = i;
                    }
                    if (end == token.end) {
                        endIdx = i; break;
                    }
                }
                if (startIdx == -1 && endIdx == -1) {
                    notFind ++;
                }
                if (startIdx != -1 && endIdx == -1) {
                    endIdx = startIdx;
                }
                assert String.join("", tokenStr.subList(startIdx,endIdx+1)).equals(entity.getText());
            }
            line.append(startIdx).append(" ").append(endIdx).append(" ");
        }
        line.append(tokens.stream().map(t -> t.token).collect(Collectors.joining(" ")));
        assert notFind < 2;
        return line.toString();
    }

    private List<BratEntity> getEntitiesInRange(Collection<BratEntity> entities, int start, int end) {
        List<BratEntity> res = new ArrayList<>();
        for (BratEntity entity: entities) {
            if (start <= entity.getStart() && entity.getEnd() <= end) {
                res.add(entity);
            }
        }
        return res;
    }

    private int calcEntityDis(BratEntity e1, BratEntity e2) {
        int start, end;
        start = Math.min(e1.getStart(), e2.getStart());
        end = Math.max(e1.getEnd(), e2.getEnd());
        return end - start;
    }


    private boolean isLink(String linkName, String trajectorId, String triggerId, String landmarkId) {
        for (Map.Entry<String, BratEvent> map: eventMap.entrySet()) {
            BratEvent bratEvent = map.getValue();
            try {
                if ( bratEvent.getType().equals(linkName) &&
                        bratEvent.getRoleIds("trajector").contains(trajectorId) &&
                        bratEvent.getRoleIds("trigger").contains(triggerId) &&
                        bratEvent.getRoleIds("landmark").contains(landmarkId)) {
                    return true;
                }
            } catch (Exception e) {
                System.exit(-1);
            }
        }
        return false;
    }

    private boolean isMLink(String  mover, String trigger) {
        for (Map.Entry<String, BratEvent> map: eventMap.entrySet()) {
            BratEvent bratEvent = map.getValue();
            if ( bratEvent.getType().equals("MLINK") &&
                    bratEvent.getRoleIds("mover").contains(mover) &&
                    bratEvent.getRoleIds("trigger").contains(trigger)) {
                return true;
            }
        }
        return false;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }


    public Map<String, List<List<String>>> getRelation() {
        List<List<String>> TLink = new ArrayList<>();
        List<List<String>> OLink = new ArrayList<>();
        List<List<String>> MLink = new ArrayList<>();
        List<List<String>> DLink = new ArrayList<>();
        List<List<String>> allLink = new ArrayList<>();


        Set<String> types = new HashSet<>(Arrays.asList("Place", "MilitaryPlace", "MilitaryBase", "Country","AdministrativeDivision",
                "Path", "P_MilitaryPlace", "SpatialEntity", "SpatialSignal", "Motion", "Event",
                "MilitaryExercise", "Conference"));
        List<BratEntity> filterEntities = entityMap.values().stream()
                .filter(o -> types.contains(o.getTag()))
                .collect(Collectors.toList());

        int posTLink = 0, posOLink = 0, posDLink = 0, posMLink = 0;
        int distanceLimit = 50;
        int max_distance = 0;


        List<BratEvent> events = this.eventMap.values().stream()
                .filter(o -> !o.getType().equals("DLINK"))
                .filter(distinctByKey(BratEvent::getSentence))
                .collect(Collectors.toList());

//        for (BratEvent o:events) {
////            System.out.println(event.toString());
//            if (o.getType().equals("MLINK")) {
//                System.out.println(o.getMemberId("mover") + ":"+o.getMemberId("trigger"));
//            }
//            else {
//                System.out.println(o.getMemberId("trajector") + ":"+o.getMemberId("trigger")+":"+o.getMemberId("landmark"));
//            }
//        }
//        System.out.println("***************");
//
//        for (BratEvent o: eventMap.values()){
//            if (o.getType().equals("MLINK")) {
//                System.out.println(o.getMemberId("mover") + ":"+o.getMemberId("trigger"));
//            }
//            else {
//                System.out.println(o.getMemberId("trajector") + ":"+o.getMemberId("trigger")+":"+o.getMemberId("landmark"));
//            }
//        }
        for (BratEvent event: events) {
            List<BratEntity> entities = getEntitiesInRange(filterEntities, event.getStart(), event.getEnd());
//            List<BratEntity> elements = new ArrayList<>();
            List<BratEntity> spatialSignals = new ArrayList<>();
            List<BratEntity> motions =new ArrayList<>();
            List<BratEntity> spatialEntities = new ArrayList<>();
            List<BratEntity> trajectors = new ArrayList<>();
            List<BratEntity> landmarks = new ArrayList<>();

            Set<Triple<String, String, String>> triples = new HashSet<>();

            for (BratEntity entity: entities) {
                String type = subtypeMap.containsKey(entity.getTag())?subtypeMap.get(entity.getTag()):entity.getTag();
                if (type.equals("SpatialSignal")) spatialSignals.add(entity);
                if (type.equals("Motion")) motions.add(entity);
                if (type.equals("SpatialEntity")){
                    spatialEntities.add(entity);
                }

                if (type.equals("Path") || type.equals("Place") || type.equals("SpatialEntity") || type.equals("Event")) {
                    trajectors.add(entity);
                }
                // spatial signal 暂时不当做landmarks
                if (type.equals("Path") || type.equals("Place") || type.equals("SpatialEntity")) {
                    landmarks.add(entity);
                }
//                 switch (entity.getTag()) {
//                    case "SpatialSignal": spatialSignals.add(entity); break;
//                    case "Motion": motions.add(entity); break;
//                    case "SpatialEntity": spatialEntities.add(entity);break;
//                    case "Place" : trajectors.add(entity);landmarks.add(entity);break;
//                    default: elements.add(entity); break;
//                }
            }
            spatialSignals.add(new BratEntity());

            List<String> negTLink = new ArrayList<>();
            List<String> tmpTLink= new ArrayList<>();
            List<String> negOLink = new ArrayList<>();
            List<String> tmpOLink = new ArrayList<>();
            List<String> negDLink = new ArrayList<>();
            List<String> tmpDLink = new ArrayList<>();
            List<String> negMLink = new ArrayList<>();
            List<String> tmpMLink = new ArrayList<>();
            List<String> tmpAllLink = new ArrayList<>();
            List<String> allNegLink = new ArrayList<>();
            for (BratEntity trajector: trajectors) {
                for (BratEntity landmark: landmarks) {
//                    String trajector = tra.get(i).getId();
//                    String landmark = elements.get(j).getId();
                    int duplicate = 0;
                    if (trajector.getId().equals(landmark.getId()))
                        continue;
                    int distance = calcEntityDis(trajector, landmark);
                    for (BratEntity trigger: spatialSignals) {
//                        String trigger = spatialSignals.get(k).getId();
                        String [] args = {trajector.getId(), trigger.getId(), landmark.getId()};

                        boolean isTLink = isLink("TLINK", trajector.getId(), trigger.getId(), landmark.getId());
                        boolean isOLink = isLink("OLINK", trajector.getId(), trigger.getId(), landmark.getId());
                        boolean isDLink = isLink("DLINK", trajector.getId(), trigger.getId(), landmark.getId());
//
//                        if (isTLink) {
//                            posTLink ++;
//                            String line = getRelLine("1", event, args);
//                            tmpTLink.add(line);
//                        } else if (distance < distanceLimit) {
//                            negTLink.add(getRelLine("0", event,args));
//                        }
//
//                        if (isOLink) {
//                            posOLink ++;
//                            String line = getRelLine("1", event, args);
//                            tmpOLink.add(line);
//                        } else if (distance < distanceLimit) {
//                            negOLink.add(getRelLine("0", event,args));
//                        }
//
//                        if (isDLink) {
//                            posTLink ++;
//                            String line = getRelLine("1", event, args);
//                            tmpDLink.add(line);
//                        } else if (distance < distanceLimit) {
//                            negDLink.add(getRelLine("0", event,args));
//                        }


                        Triple<String, String, String> triple = new ImmutableTriple<>(trajector.getId(), trigger.getId(), landmark.getId());
                        if (triples.contains(triple)) continue;
                        if (isDLink || isOLink || isTLink) {
                            tmpAllLink.add(getRelLine("1", event, args));
                            triples.add(triple);
                        } else if (distance < distanceLimit) {
                            allNegLink.add(getRelLine("0", event,args));
                        }
                    }
                }
            }

            int neg_length;

//            if (negTLink.size() > neg_length) {
//                Collections.shuffle(negTLink);
//                negTLink = negTLink.subList(0, neg_length);
//            }
//            TLink.addAll(negTLink);
//
//            if (negOLink.size() > neg_length) {
//                Collections.shuffle(negOLink);
//                negOLink = negOLink.subList(0, neg_length);
//            }
//            OLink.addAll(negOLink);
//
//            if (negDLink.size() > neg_length) {
//                Collections.shuffle(negDLink);
//                negDLink = negDLink.subList(0, neg_length);
//            }
//            DLink.addAll(negDLink);


            neg_length = tmpAllLink.size() * 5;
            if (allNegLink.size() > neg_length) {
                Collections.shuffle(allNegLink);
                allNegLink = allNegLink.subList(0, neg_length);
            }
            tmpAllLink.addAll(allNegLink);
            if (tmpAllLink.size() > 0)
                allLink.add(tmpAllLink);


            for (BratEntity mover: spatialEntities) {
                for (BratEntity trigger: motions) {
                    int distance = calcEntityDis(mover, trigger);
                    if (isMLink(mover.getId(), trigger.getId())) {
                        posTLink ++;
                        String line = getRelLine("1", event, mover.getId(), trigger.getId());
                        tmpMLink.add(line);
                        max_distance = Math.max(max_distance, distance);
                    } else if (distance < distanceLimit) {
                        String line = getRelLine("0", event, mover.getId(), trigger.getId());
                        tmpMLink.add(line);
                    }
                }

            }
            if (tmpMLink.size() > 0)
                MLink.add(tmpMLink);
        }

        return new HashMap<String, List<List<String>>>(){{
            put("MLink", MLink);
            put("OLink", OLink);
            put("DLink", DLink);
            put("TLink", TLink);
            put("All", allLink);
        }};
    }


    public class SRLExample {
        public boolean hasTrigger;
//        List<String> tokens;
//        List<String> roles;
//        List<String> labels;
//        List<String> mergeRoles;
//        List<String> mergeLabels;
//        int predicateStartIdx;
//        int predicateEndIndex;
//
//        Set<String> acceptRoles = new HashSet<>(Arrays.asList("trajector", "landmark", "trigger", "mover"));
//
        BratEvent bratEvent;
        String line;
        public String type;

        SRLExample(BratEvent bratEvent) {
            this.bratEvent = bratEvent;
            this.hasTrigger = false;
            this.line = "";
        }

        SRLExample(BratEvent bratEvent, int predicateStartIdx, int predicateEndIndex, List<String> tokens,
                   List<String> labels, List<String> roles) {
            this.bratEvent = bratEvent;
            this.line =  String.format("%d %d\t%s\t%s\t%s", predicateStartIdx, predicateEndIndex, String.join(" ", tokens)
                    , String.join(" ", labels), String.join(" ", roles));
            line = line.replaceAll("[“”]", "\"");
            this.type = bratEvent.getType();
            this.hasTrigger = bratEvent.isHasTrigger();
        }
        @Override
        public String toString() {
            return this.line;
        }
    }



    private List<BratEntity> mergeAdjacentPlace(Collection<BratEntity> oriEntities, Collection<String> entityIdsInEvent) {
        List<BratEntity> sortedEntities = oriEntities.stream()
                .sorted(Comparator.comparing(BratEntity::getStart))
                .collect(Collectors.toList());

        Set<Pair<String, String>> pairs = this.relationMap.values().stream()
                .filter(r-> r.getTag().equals("locatedIn") || r.getTag().equals("partOf"))
                .map(r -> new ImmutablePair<>(r.getTargetId(), r.getSourceId()))
                .collect(Collectors.toSet());

        List<BratEntity> filteredEntities = new ArrayList<>();
        for (int i=0; i < sortedEntities.size()-1; i++) {
            BratEntity e1 = sortedEntities.get(i), e2 = sortedEntities.get(i + 1);
            if (e1.getEnd() == e2.getStart() && pairs.contains(new ImmutablePair<>(e1.getId(), e2.getId()))
                    && !entityIdsInEvent.contains(e1.getId())) {
                e2.parent_count += e1.parent_count + 1;
                e2.setStart(e1.getStart());
                e2.setText(e1.getText() + e2.getText());
            } else  {
                filteredEntities.add(e1);
            }
            if (i == sortedEntities.size() - 2) filteredEntities.add(e2);
        }
        return filteredEntities;
    }


    private List<String> getBIOLabels(List<Token> tokens, List<Pair<String, BratEntity>> pairs, int offset) {
        List<String> labels = tokens.stream().map(x -> "O").collect(Collectors.toList());
        for (Pair<String, BratEntity> pair: pairs) {
            String label = pair.getLeft();
            BratEntity entity = pair.getRight();
//            if (entity == null) {
//                System.out.println(1);
//            }
            int start = entity.getStart() - offset, end = entity.getEnd() - offset;
            int startIndex = -1, endIndex = -1;
            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                if (start == token.begin) {
                    startIndex = i;
                }
                if (end == token.end) {
                    endIndex = i;
                    break;
                }
            }
            for (int i = startIndex; i <= endIndex; i++) {
                labels.set(i, (i == startIndex) ? "B-" + label : "I-" + label);
            }
        }
        return labels;
    }

    public Map<String, List<List<SRLExample>>> getSRLFormatData(boolean include_conj, boolean mergePlace) {

        Map<String, List<List<SRLExample>>> res = new HashMap<String, List<List<SRLExample>>>() {{
            put("TLINK", new ArrayList<>());
            put("OLINK", new ArrayList<>());
            put("MLINK", new ArrayList<>());
            put("DLINK", new ArrayList<>());
            put("ALL", new ArrayList<>());
        }};

        List<List<SRLExample>> groups = new ArrayList<>();
        Set<String> availableLabels = new HashSet<>(BratUtil.availableLabels);
        if (include_conj) {
            availableLabels.add("SpatialConj");
        }

        List<BratEntity> usedEntities = this.entityMap.values().stream()
                .filter(e -> availableLabels.contains(e.getTag()))
                .peek(e -> e.setTag(subtypeMap.getOrDefault(e.getTag(), e.getTag())))
                .collect(Collectors.toList());
        if (mergePlace) {
            Set<String> entityIdsInEvent = this.eventMap.values().stream().filter(BratEvent::isHasTrigger)
                    .flatMap(o->o.getRoleMap().values().stream()).distinct().collect(Collectors.toSet());
            usedEntities = mergeAdjacentPlace(usedEntities, entityIdsInEvent);
        }
        // 根据实体类型筛选出实际使用的实体
        Map<String, BratEntity> usedEntityMap = usedEntities.stream()
                .collect(Collectors.toMap(BratEntity::getId, e->e, (x, y) -> x));

        Set<String> acceptRoles = new HashSet<>(Arrays.asList("trajector", "landmark", "trigger", "mover"));

        for (BratEvent event : eventMap.values()) {
            boolean hasTriggerOrVal = event.hasRole("val") || event.hasRole("trigger");
            Multimap<String, String> usedRoleMap = HashMultimap.create();
            event.getRoleMap().forEach((role, eid) -> {
                if (role.equals("val") || (!hasTriggerOrVal && role.equals("conj") && include_conj))
                    role = "trigger";
                if (acceptRoles.contains(role))
                    usedRoleMap.put(role, eid);
            });

            SRLExample srlExample;

            if (!usedRoleMap.containsKey("trigger"))
                event.setHasTrigger(false);
            if (!event.isHasTrigger() && mergePlace) {
                srlExample =  new SRLExample(event);
            } else {
                List<Pair<String, BratEntity>> usedRolePair = usedRoleMap.entries().stream()
                        .map(e -> new ImmutablePair<>(e.getKey(), usedEntityMap.get(e.getValue())))
                        .collect(Collectors.toList());
                List<Pair<String, BratEntity>> entitiesPairsInSentence = getEntitiesInRange(usedEntityMap.values(), event.getStart(), event.getEnd())
                        .stream().map(e->new ImmutablePair<>(e.getTag(), e)).collect(Collectors.toList());

//                List<Pair<String, BratEntity>> usedMergedRolePair = usedRoleMap.entries().stream()
//                        .map(e -> new ImmutablePair<>(e.getKey(), mergedEntityMap.get(e.getValue())))
//                        .collect(Collectors.toList());

//                List<Pair<String, BratEntity>> mergedEntitiesPairsInSentence = getEntitiesInRange(mergedEntityMap.values(), event.getStart(), event.getEnd())
//                        .stream().map(e->new ImmutablePair<>(e.getTag(), e)).collect(Collectors.toList());

                List<Token> rawTokens = this.tokens(event.getSentence());
                List<String> tokens = rawTokens.stream().map(x -> x.token).collect(Collectors.toList());
                List<String> roles = getBIOLabels(rawTokens, usedRolePair, event.getStart());
                List<String> labels = getBIOLabels(rawTokens, entitiesPairsInSentence, event.getStart());
//                List<String> mergedRoles =  getBIOLabels(rawTokens, usedMergedRolePair, event.getStart());
//                List<String> mergeLabels =  getBIOLabels(rawTokens, mergedEntitiesPairsInSentence, event.getStart());

                int predicateStartIdx = roles.indexOf("B-trigger");
                int predicateEndIndex = Math.max(roles.lastIndexOf("I-trigger"), roles.lastIndexOf("B-trigger"));
                srlExample = new SRLExample(event, predicateStartIdx, predicateEndIndex, tokens, labels, roles);
            }

            boolean overlap = false;
            for (List<SRLExample> group : groups) {
                for (SRLExample example : group) {
                    if (!(event.getEnd() <= example.bratEvent.getStart() || event.getStart() >= example.bratEvent.getEnd())) {
                        group.add(srlExample);
                        overlap = true;
                        break;
                    }
                }
                if (overlap) break;
            }
            if (!overlap) {
                groups.add(new ArrayList<>());
                groups.get(groups.size() - 1).add(srlExample);
            }
        }
        groups.forEach(group -> {
            Map<String, List<SRLExample>> tmpMap = new HashMap<String, List<SRLExample>>() {{
                put("ALL", new ArrayList<>());
            }};
            group.forEach(o -> {
                tmpMap.putIfAbsent(o.bratEvent.getType(), new ArrayList<>());
                tmpMap.get(o.bratEvent.getType()).add(o);
                tmpMap.get("ALL").add(o);
            });
            tmpMap.forEach((type, examples) -> res.get(type).add(examples));
        });
        return res;
    }

    public static void main(String [] args)  {
        BratDocument bratDocument = new BratDocument("data/annotation/0-49/0046.txt",
                "data/annotation/0-49/0046.ann");
        bratDocument.getSRLFormatData(true,true);
//        bratDocument.getSRLFormatData(true);
//        String [] types = {"Place", "MilitaryPlace", "MilitaryBase", "Country","AdministrativeDivision",
//                "Path", "P_MilitaryPlace", "SpatialEntity", "SpatialSignal", "Motion", "Event",
//                "MilitaryExercise", "Conference"};
//        Set<String> typeSet = new HashSet<>(Arrays.asList(types));
//        List<String []> rows = bratDocument.tokenRows(typeSet, 32);
//
//        List<String> lines = rows.stream().map(x -> x[0] + "\t" + x[1]).collect(Collectors.toList());
//        FileUtil.writeFile("data/processed_data/0001.txt", lines, false);

    }
}
