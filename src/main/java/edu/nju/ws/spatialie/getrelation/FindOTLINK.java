package edu.nju.ws.spatialie.getrelation;

import edu.nju.ws.spatialie.Link.DLINK;
import edu.nju.ws.spatialie.Link.OTLINK;
import edu.nju.ws.spatialie.annprocess.BratUtil;
import edu.nju.ws.spatialie.data.BratDocumentwithList;
import edu.nju.ws.spatialie.data.BratEntity;
import edu.nju.ws.spatialie.data.ParseTree;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindOTLINK extends FindLINK {
    public static OTLINK findOTLINK(BratDocumentwithList bratDocument, List<OTLINK> otlinklist, int level, List<DLINK> dlinkList) {
        for (OTLINK otlink : otlinklist) {
            if (isOTLINK(otlink, bratDocument, level, dlinkList) != null) {
                return otlink;
            }
        }
        return null;
    }

    private static OTLINK isOTLINK(OTLINK otlink, BratDocumentwithList bratDocument, int level, List<DLINK> dlinkList) {
        if (otlink.isIscompleted()) return null;
//        if (!bratDocument.getIsCandidate(otlink.getTrigger())) return null;
        List<BratEntity> entityList = bratDocument.getEntityList();
        int index = otlink.getTrigger();
        BratEntity trigger = entityList.get(index);

        int idx_last1 = getLast(index, bratDocument);
        BratEntity last1 = getEntity(idx_last1, entityList);
        DLINK dlink = null;
        if (last1 != null && last1.getTag().equals(BratUtil.MEASURE) && hasNoVPinEntity(bratDocument, idx_last1)) {
            for (DLINK l : dlinkList) {
                if (idx_last1 == l.getVal()) {
                    dlink = l;
                    break;
                }
            }
            idx_last1 = getLast(idx_last1, bratDocument);
            last1 = getEntity(idx_last1, entityList);
        }

        int idx_next1 = getNext(index, bratDocument);
        BratEntity next1 = getEntity(idx_next1, entityList);
        if (next1 != null && next1.getTag().equals(BratUtil.MEASURE)) {
            for (DLINK l : dlinkList) {
                if (idx_next1 == l.getVal()) {
                    dlink = l;
                    break;
                }
            }
            idx_next1 = getNext(idx_next1, bratDocument);
            next1 = getEntity(idx_next1, entityList);
        }

        int idx_last2 = getLast(idx_last1, bratDocument);
        int idx_last3 = getLast(idx_last2, bratDocument);
        int idx_next2 = getNext(idx_next1, bratDocument);
        int idx_next3 = getNext(idx_next2, bratDocument);

        BratEntity last2 = getEntity(idx_last2, entityList);
        BratEntity last3 = getEntity(idx_last3, entityList);
        BratEntity next2 = getEntity(idx_next2, entityList);
        BratEntity next3 = getEntity(idx_next3, entityList);




        if (isPrep(trigger, bratDocument)) {
            //<trigger>-<landmark>there be<trajector>
            //5
            if (level == 5 && next2 != null) {
                if (inSentence(bratDocument, index, idx_next2) && hasNoNV(bratDocument, index, idx_next1)) {
                    String temp_s = getSegmentOrigin(bratDocument, idx_next1, idx_next2);
                    if (temp_s.contains("there be")) {
                        if (JudgeEntity.canbeLandmark(next1) && JudgeEntity.canbeTrajector(next2)) {
                            setLink(idx_next1, idx_next2, otlink,entityList);
                            otlink.setRule_id("OT1");
                            bratDocument.noCandidate(idx_next1);
                            bratDocument.noCandidate(idx_next2);
                            bratDocument.noCandidate(index);
                            setifHasDLINK(idx_next1, idx_next2, dlink, bratDocument, otlink.getRule_id());
                            return otlink;
                        }
                    }
                }
            }

            //记录删除
//            //<landmark><trajector[n]>[v]([n])<trigger>
//            //2
//            if (level == 2 && last2 != null) {
//                if (inSegment_true(bratDocument, idx_last2, index) && countVerb(bratDocument, idx_last1, index) <= 1
//                        && hasNoNV(bratDocument, idx_last2, idx_last1) && !hasPOS("IN", bratDocument, idx_last2, idx_last1) && !hasPOS("P", bratDocument, idx_last2, idx_last1) &&
//                        countNoun(bratDocument, idx_last1, index) <= 1) {
//                    if (JudgeEntity.canbeLandmark(last2) && JudgeEntity.canbeTrajector(last1)) {
//                        setLink(idx_last2, idx_last1, otlink);
//                        otlink.setRule_id("OT2");
//                        bratDocument.noCandidate(idx_last1);
//                        bratDocument.noCandidate(index);
//                        setifHasDLINK(idx_last2, idx_last1, dlink, bratDocument, otlink.getRule_id());
//                        return otlink;
//                    }
//                }
//            }
            //记录删除
            //<landmark>[n]<trajector[v]>-<trigger>
            //2
//            if (level == 2 && last2 != null) {
////                String temp_s = getSegment(bratDocument, idx_last1, index);
//                if (hasNoNV(bratDocument, idx_last1, index)) {
//                    if (inSegment_true(bratDocument, idx_last2, index) && hasPOS("N", bratDocument, idx_last2, idx_last1)
//                            && (countVerb(bratDocument, idx_last2, idx_last1) == 0 || hasPOSinEntity("VBN", bratDocument, last1) || hasPOSinEntity("VBG", bratDocument, last1))) {
//                        if (JudgeEntity.canbeLandmark(last2) && JudgeEntity.isEvent(last1)) {
//                            setLink(idx_last2, idx_last1, otlink);
//                            otlink.setRule_id("OT3");
//                            bratDocument.noCandidate(idx_last1);
//                            bratDocument.noCandidate(index);
//                            setifHasDLINK(idx_last2, idx_last1, dlink, bratDocument, otlink.getRule_id());
//                            return otlink;
//                        }
//                    }
//                }
//            }

            //<landmark><trajector[n]><trajector[v]>-<trigger>
            //2
            if (level == 2 && last3 != null) {
//                String temp_s = getSegment(bratDocument, idx_last1, index);
                if (hasNoNV(bratDocument, idx_last1, index) && hasNoNV(bratDocument, idx_last2, idx_last1) && hasNoNV(bratDocument, idx_last3, idx_last2)) {
                    if (inSegment_true(bratDocument, idx_last3, index)) {
                        if (JudgeEntity.canbeLandmark(last3) && JudgeEntity.isEvent(last1) && JudgeEntity.canbeTrajector(last2)) {
                            setLink(idx_last3, idx_last1, otlink, entityList);
                            otlink.setRule_id("OT4");
//                            otlink.addTrajectors(idx_last2);
                            bratDocument.noCandidate(idx_last1);
                            bratDocument.noCandidate(idx_last2);
                            setifHasDLINK(idx_last3, idx_last1, dlink, bratDocument, otlink.getRule_id());
//                            if (dlink!=null){
//                                dlink.addTrajectors(idx_last2);
//                            }
                            bratDocument.noCandidate(index);
                            return otlink;
                        }
                    }
                }
            }

            //<trajector>that/which/who be<trigger><landmark>
            //2
            if (level == 2 && last1 != null && next1 != null) {
                if (inSegment(bratDocument, idx_last1, idx_next1) && hasNoNV(bratDocument, index, idx_next1)) {
                    String temp_s = getSegment(bratDocument, idx_last1, index);
                    if (temp_s.contains("that") || temp_s.contains("which") || temp_s.contains("who")) {
                        if (JudgeEntity.canbeLandmark(next1) && JudgeEntity.canbeTrajector(last1)) {
                            setLink(idx_next1, idx_last1, otlink, entityList);
                            otlink.setRule_id("OT5");
                            bratDocument.noCandidate(idx_next1);
                            bratDocument.noCandidate(index);
                            setifHasDLINK(idx_next1, idx_last1, dlink, bratDocument, otlink.getRule_id());
                            return otlink;
                        }
                    }
                }
            }

            //<trigger>-<landmark><trajector[n]>(<trajector[v]>)
            //5
            if (level == 5 && next2 != null) {
                if (inSentence(bratDocument, index, idx_next2) && hasNoNV(bratDocument, index, idx_next1) && hasNoNV(bratDocument, idx_next1, idx_next2)) {
                    if (JudgeEntity.canbeLandmark(next1) && JudgeEntity.canbeTrajector(next2)) {
                        setLink(idx_next1, idx_next2, otlink, entityList);
                        otlink.setRule_id("OT6");
                        bratDocument.noCandidate(idx_next1);
//                        bratDocument.noCandidate(idx_next2);
                        bratDocument.noCandidate(index);
                        // 记录删除
//                        setifHasDLINK(idx_next1, idx_next2, dlink, bratDocument, otlink.getRule_id());
                        if (next3 != null && inSegment(bratDocument, idx_next2, idx_next3) && JudgeEntity.isEvent(next3)) {
                            otlink.addTrajectors(idx_next3);
                            otlink.setRule_id("OT7");
                            otlink.removeTrajector(idx_next2);
//                            bratDocument.noCandidate(idx_next3);
                            // 记录删除
//                            if (dlink != null) {
//                                dlink.addTrajectors(idx_next3);
//                                dlink.setRule_id("DOT7");
//                            }
                        }
                        return otlink;
                    }
                }
            }

            //<trigger>-<landmark>[n]<trajector[v]>
            //5
            if (level == 5 && next2 != null) {
                if (inSentence(bratDocument, idx_last1, idx_next1) && hasNoNV(bratDocument, index, idx_next1)) {
                    if (JudgeEntity.canbeLandmark(next1) && JudgeEntity.isEvent(next2) && countNoun(bratDocument, idx_next1, idx_next2) == 1) {
                        setLink(idx_next1, idx_next2, otlink, entityList);
                        otlink.setRule_id("OT8");
                        bratDocument.noCandidate(idx_next1);
                        bratDocument.noCandidate(idx_next2);
                        bratDocument.noCandidate(index);
                        setifHasDLINK(idx_next1, idx_next2, dlink, bratDocument, otlink.getRule_id());
                        return otlink;
                    }
                }
            }

            //<trajector[n]>[v]<trigger>-<landmark>
            //4
            if (level == 4 && last1 != null && next1 != null) {
                int c = countVerb(bratDocument, idx_last1, index);
                int c2 = countWord("be", bratDocument, idx_last1, index);
                boolean hasNoun = hasPOS("N", bratDocument, idx_last1, index);
                if ((c == 1 || c2 == 1) && (!hasNoun || checkOrderVN(bratDocument, idx_last1, index))) {
                    if (inSentence(bratDocument, idx_last1, idx_next1) && hasNoNV(bratDocument, index, idx_next1)) {
                        if (JudgeEntity.canbeLandmark(next1) && JudgeEntity.canbeTrajector(last1)) {
                            setLink(idx_next1, idx_last1, otlink, entityList);
                            otlink.setRule_id("OT9");
//                            bratDocument.noCandidate(idx_last1);
                            bratDocument.noCandidate(idx_next1);
                            bratDocument.noCandidate(index);
                            setifHasDLINK(idx_next1, idx_last1, dlink, bratDocument, otlink.getRule_id());
                            return otlink;
                        }
                    }
                }
            }


            //<trajector[n]><trigger>-<landmark>
            //2
            if ((level == 2 || level == 4) && last1 != null && next1 != null) {
                if (inSegment_true(bratDocument, idx_last1, idx_next1)) {
                    if (!hasPOS("N", bratDocument, idx_last1, index) && !hasPOS("V", bratDocument, idx_last1, index) && hasNoNV(bratDocument, index, idx_next1)) {
                        if (JudgeEntity.canbeLandmark(next1) && JudgeEntity.canbeTrajector(last1)) {
                            setLink(idx_next1, idx_last1, otlink, entityList);
                            otlink.setRule_id("OT10");
                            bratDocument.noCandidate(idx_next1);
                            bratDocument.noCandidate(index);
                            // 记录删除
//                            setifHasDLINK(idx_next1, idx_last1, dlink, bratDocument, otlink.getRule_id());
                            //<trajector[v]><trajector[n]><trigger><landmark>
                            //4
                            if (level == 4 && last2 != null) {
                                if (inSegment(bratDocument, idx_last2, idx_last1) && hasNoNV(bratDocument, idx_last2, idx_last1)) {
                                    if (JudgeEntity.isEvent(last2)) {
                                        otlink.addTrajectors(idx_last2);
                                        otlink.setRule_id("OT11");
                                        bratDocument.noCandidate(idx_last2);
                                        bratDocument.noCandidate(idx_last1);
                                        if (dlink != null) {
                                            dlink.addTrajectors(idx_last2);
                                            dlink.setRule_id("DOT11");
                                        }
//                                        <trajector[n]><trajector[v]><trajector[n]><trigger><landmark>
//                                        if (last3 != null) {
//                                            if (inSentence(bratDocument, idx_last3, idx_last2)) {
//                                                if (JudgeEntity.canbeTrajector(last3)) {
//                                                    otlink.addTrajectors(idx_last3);
////                                                bratDocument.noCandidate(idx_last3);
//                                                    if (dlink != null) dlink.addTrajectors(idx_last3);
//                                                }
//                                            }
//                                        }
                                    }
                                }
                            }
                            return otlink;
                        }
                    }
                }
            }

            //记录删除
//            if ((level == 4) && last1 != null && next1 != null) {
//                if (!inSegment_true(bratDocument, idx_last1, idx_next1)) {
//                    if (!hasPOS("N", bratDocument, idx_last1, index) && !hasPOS("V", bratDocument, idx_last1, index) && hasNoNV(bratDocument, index, idx_next1)) {
//                        if (JudgeEntity.canbeLandmark(next1) && JudgeEntity.canbeTrajector(last1)) {
//                            //<trajector[v]><trajector[n]><trigger><landmark>
//                            //4
//                            if (last2 != null) {
//                                if (inSegment(bratDocument, idx_last2, idx_last1) && hasNoNV(bratDocument, idx_last2, idx_last1)) {
//                                    if (JudgeEntity.isEvent(last2)) {
//                                        setLink(idx_next1, idx_last2, otlink);
//                                        otlink.setRule_id("OT12");
//                                        setifHasDLINK(idx_next1, idx_last2, dlink, bratDocument, otlink.getRule_id());
//                                        bratDocument.noCandidate(idx_last2);
//                                        bratDocument.noCandidate(idx_next1);
//                                        bratDocument.noCandidate(index);
//                                        bratDocument.noCandidate(idx_last1);
//                                        return otlink;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }

            //<trajector[v]><trigger>-<landmark>
            //4
            if (level == 4 && last1 != null && next1 != null) {
                if (inSegment(bratDocument, idx_last1, idx_next1)) {
                    if (countNoun(bratDocument, idx_last1, index) <= 1 && !hasPOS("V", bratDocument, idx_last1, index) && hasNoNV(bratDocument, index, idx_next1)) {
                        if (JudgeEntity.canbeLandmark(next1) && JudgeEntity.isEvent(last1)) {
                            setLink(idx_next1, idx_last1, otlink, entityList);
                            otlink.setRule_id("OT13");
                            bratDocument.noCandidate(idx_next1);
                            bratDocument.noCandidate(idx_last1);
                            bratDocument.noCandidate(index);
                            //记录删除
//                            setifHasDLINK(idx_next1, idx_last1, dlink, bratDocument,otlink.getRule_id());
                            //<trajector[n]><trajector[v]><trigger><landmark>
//                            if (last2 != null) {
//                                if (inSentence(bratDocument, idx_last2, idx_last1)) {
//                                    if (JudgeEntity.canbeTrajector(last2)) {
//                                        otlink.addTrajectors(idx_last2);
////                                    bratDocument.noCandidate(idx_last2);
//                                        if (dlink != null) dlink.addTrajectors(idx_last2);
//                                    }
//                                }
//                            }
                            return otlink;
                        }
                    }
                }
            }
        }

        if (isVerb(trigger, bratDocument)) {
            // <landmark(which/that)><trigger[v]><trajector>
            // 2
            if (level == 2 && last1 != null && next1 != null) {
                if (last1.getText().toLowerCase().equals("which") || last1.getText().toLowerCase().equals("that")) {
                    if (inSentence(bratDocument, idx_last1, idx_next1)) {
                        if (JudgeEntity.canbeLandmark(last1) && JudgeEntity.canbeTrajector(next1)) {
                            setLink(idx_last1, idx_next1, otlink, entityList);
                            otlink.setRule_id("OT14");
//                        bratDocument.noCandidate(idx_last1);
                            bratDocument.noCandidate(idx_next1);
                            bratDocument.noCandidate(index);
                            return otlink;
                        }
                    }
                }
            }

            // <landmark><trigger[v]><trajector>
            // 4
            if (level == 4 && last1 != null && next1 != null) {
                if (inSentence(bratDocument, idx_last1, idx_next1)) {
                    if (JudgeEntity.canbeLandmark(last1) && JudgeEntity.canbeTrajector(next1)) {
                        setLink(idx_last1, idx_next1, otlink, entityList);
                        otlink.setRule_id("OT15");
//                        bratDocument.noCandidate(idx_last1);
                        bratDocument.noCandidate(idx_next1);
                        bratDocument.noCandidate(index);
                        return otlink;
                    }
                }
            }

            //记录删除
//            //<trajector>that/which/who <trigger><landmark>
//            //2
//            if (level == 2 && last1 != null && next1 != null) {
//                if (inSegment(bratDocument, idx_last1, idx_next1)) {
//                    String temp_s = getSegment(bratDocument, idx_last1, index);
//                    if (temp_s.contains("that") || temp_s.contains("which") || temp_s.contains("who")) {
//                        if (JudgeEntity.canbeLandmark(next1) && JudgeEntity.canbeTrajector(last1)) {
//                            setLink(idx_next1, idx_last1, otlink);
//                            otlink.setRule_id("OT16");
//                            bratDocument.noCandidate(idx_next1);
//                            bratDocument.noCandidate(index);
//                            return otlink;
//                        }
//                    }
//                }
//            }

            //记录删除
//            //<landmark>(that/which)<trajector><trigger>
//            //TODO:l和t关系不确定
//            //2
//            if (level == 2 && last2 != null) {
//                if (inSegment(bratDocument, idx_last2, index)) {
//                    String temp_s = getSegment(bratDocument, idx_last2, idx_last1);
//                    if (temp_s.contains("that") || temp_s.contains("which")) {
//                        if (JudgeEntity.canbeLandmark(last2) && JudgeEntity.canbeTrajector(last1)) {
//                            setLink(idx_last2, idx_last1, otlink);
//                            otlink.setRule_id("OT17");
//                            bratDocument.noCandidate(idx_last1);
//                            bratDocument.noCandidate(index);
//                            return otlink;
//                        }
//                    }
//                }
//            }
        }
        // <landmark><trigger(where)><trajector>...
        //2
        if (level == 2 && last1 != null && next1 != null) {
            if (trigger.getText().toLowerCase().endsWith("where")) {
                if (inSegment(bratDocument, idx_last1, idx_next1)) {
                    // // <landmark><trigger(where)>[n]<trajector[v]>
                    if (JudgeEntity.canbeLandmark(last1) && JudgeEntity.isEvent(next1)){
                        if (last1.getEnd()+1==trigger.getStart()&&countNoun(bratDocument,index,idx_next1)==1){
                            setLink(idx_last1, idx_next1, otlink, entityList);
                            otlink.setRule_id("OT18");
                            bratDocument.noCandidate(index);
                            return otlink;
                        }
                    }
                    if (JudgeEntity.canbeLandmark(last1) && JudgeEntity.canbeTrajector(next1)) {
                        //记录删除
//                        setLink(idx_last1, idx_next1, otlink, entityList);
//                        otlink.setRule_id("OT19");
////                        bratDocument.noCandidate(idx_next1);
//                        bratDocument.noCandidate(index);
                        //<landmark><trigger(where)><trajector>-<trajector2[v]>
                        if (next2 != null &&
                                (countNoun(bratDocument, index, idx_next2) == 1 && countVerb(bratDocument, idx_next1, idx_next2) == 0 || hasPOSinEntity("VBN", bratDocument, next2) || hasPOSinEntity("VBG", bratDocument, next2))) {
                            if (inSegment(bratDocument, idx_last1, idx_next2)) {
                                if (JudgeEntity.isEvent(next2)) {
                                    //记录删除
                                    setLink(idx_last1, idx_next1, otlink, entityList);
                                    bratDocument.noCandidate(index);
                                    otlink.setRule_id("OT20");
                                    otlink.addTrajectors(idx_next2);
                                    otlink.removeTrajector(idx_next1);
//                                    bratDocument.noCandidate(idx_next2);
                                    return otlink;
                                }
                            }
                        }

                    }
                }
            }
        }
        return null;
    }


    private static void setifHasDLINK(int idx_l, int idx_t, DLINK dlink, BratDocumentwithList bratDocument, String rule_id) {
        if (dlink != null) {
            dlink.addLandmarks(idx_l);
            dlink.addTrajectors(idx_t);
            dlink.Complete();
            bratDocument.noCandidate(dlink.getVal());
            dlink.setRule_id("D"+rule_id);
        }
    }


    private static void setLink(int idx_l, int idx_t, OTLINK link, List<BratEntity> entityList) {

        String t="";
        if (link.getTrigger()!=-1) {
            t = entityList.get(link.getTrigger()).getText();
        }
        if (NeedChangeLT(t)){
            link.addLandmarks(idx_t);
            link.addTrajectors(idx_l);
        } else {
            link.addLandmarks(idx_l);
            link.addTrajectors(idx_t);
        }
        link.Complete();
    }

    private static boolean NeedChangeLT(String t) {
        List<String> changeList = WordData.getChangelt();
        for (String s:changeList){
            if (t.contains(s+" ")||t.contains(" "+s)||t.equals(s)) return true;
        }
        return false;
    }


    public static OTLINK findOTLINKwithoutTrigger(BratDocumentwithList bratDocument, int level, int i) {
        if (!bratDocument.getIsCandidate(i)) return null;
        List<BratEntity> entityList = bratDocument.getEntityList();
        BratEntity trajector = getEntity(i, entityList);
        assert trajector != null;
        if (!JudgeEntity.canbeTrajector(trajector) && !JudgeEntity.isEvent(trajector)) return null;

        int idx_last1;
        int idx_next1;
        int idx_last2;

        if (level <= 2) {
            idx_last1 = getLast_ignoremeasure(i, bratDocument);
            idx_last2 = getLast_ignoremeasure(idx_last1, bratDocument);
            idx_next1 = getNext_ignoremeasure(i, bratDocument);
        } else {
            idx_last1 = getLast(i, bratDocument);
            idx_last2 = getLast(idx_last1, bratDocument);
            idx_next1 = getNext(i, bratDocument);
        }

        BratEntity last1 = getEntity(idx_last1, entityList);
        BratEntity next1 = getEntity(idx_next1, entityList);
        BratEntity last2 = getEntity(idx_last2, entityList);


//        if (last1!=null&&last1.getText().toLowerCase().equals("s")&&trajector.getText().equals("fountains")){
//            System.out.println();
//        }

//<trajector>(<landmark>)
        //0
        if (level == 0 && next1 != null) {
            String temp_s = getSegment(bratDocument, i, idx_next1);
            if (inSegment(bratDocument, i, idx_next1) && temp_s.contains("(") && !temp_s.contains(")")
                    && hasNoNV(bratDocument, i, idx_next1)&&bratDocument.getContent().contains("( "+next1.getText()+" )")) {
                if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(next1)) {
                    //确保不是翻译
                    Pattern pattern = Pattern.compile("[\\w| ]+");
                    Matcher matcher = pattern.matcher(trajector.getText());
                    Matcher matcher2 = pattern.matcher(next1.getText());
                    if (matcher.matches()&&matcher2.matches()
                            &&!trajector.getText().contains(" de ")&&!trajector.getText().contains(" del ")
                            &&!next1.getText().contains(" de ")&&!next1.getText().contains(" del ")) {
                        if (trajector.getText().charAt(0) < 'a' && next1.getText().charAt(0) < 'a') {
                            OTLINK newlink = new OTLINK(-1);
                            setLink(idx_next1, i, newlink, entityList);
                            newlink.setRule_id("NT1");
                            bratDocument.noCandidate(idx_next1);
                            return newlink;
                        }
                    }
                }
            }
        }

        //<trajector[n]><landmark(there/here)>
        //0
        if (level == 0 && next1 != null) {
            if (inSegment_true(bratDocument,i,idx_next1)&&hasNoNV(bratDocument,i,idx_next1)) {
                if (isRB(next1)) {
                    if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(next1)) {
                        OTLINK newlink = new OTLINK(-1);
                        setLink(idx_next1, i, newlink, entityList);
                        newlink.setRule_id("NT2");
                        bratDocument.noCandidate(idx_next1);
//                        bratDocument.noCandidate(i);
                        return newlink;
                    }
                }
            }
        }

//        // 记录删除
//        //<landmark(there/here)>[v]<trajector[n]>
//        //5
//        if (level == 5 && last1 != null) {
//            if (isRB(last1)) {
//                if (countVerb(bratDocument, idx_last1, i) <= 1 && countNoun_position(bratDocument, last1.getEnd(), trajector.getEnd()) == 1
//                        &&hasNoPrep(bratDocument,idx_last1,i,"" )) {
//                    if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(last1)) {
//                        OTLINK newlink = new OTLINK(-1);
//                        //TODO:先换一下顺序
//                        setLink( i, idx_last1,newlink,entityList);
//                        newlink.setRule_id("NT3");
//                        bratDocument.noCandidate(idx_last1);
//                        bratDocument.noCandidate(i);
//                        return newlink;
//                    }
//                }
//            }
//        }

        //<trajector[v]><landmark(there/here)>
        //4
        if (level == 4 && next1 != null) {
            if (inSegment(bratDocument, i, idx_next1) && JudgeEntity.isEvent(trajector)) {
                if (hasNoNV(bratDocument, i, idx_next1)) {
                    if (isRB(next1)) {
                        if (JudgeEntity.canbeLandmark(next1)) {
                            OTLINK newlink = new OTLINK(-1);
                            setLink(idx_next1, i, newlink, entityList);
                            newlink.setRule_id("NT4");
                            bratDocument.noCandidate(idx_next1);
                            bratDocument.noCandidate(i);
                            return newlink;
                        }
                    }
                }
            }
        }

//        //<trajector>from<landmark>
//        //1
//        if (level == 1 && next1 != null) {
//            if (inSegment(bratDocument, i, idx_next1) && getSegment(bratDocument, i, idx_next1).contains(" from ")
//                    && hasNoNV(bratDocument, i, idx_next1)&&hasNoPrep(bratDocument,i,idx_next1, "from")) {
//                if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(next1)) {
//                    OTLINK newlink = new OTLINK(-1);
//                    setLink(idx_next1, i, newlink,entityList);
//                    bratDocument.noCandidate(idx_next1);
//                    return newlink;
//                }
//            }
//        }

        //<trajector>of<landmark>
        //1
        if (level == 1 && next1 != null) {
            if (inSegment_true(bratDocument, i, idx_next1) &&
                    (getSegment(bratDocument, i, idx_next1).contains(" of "))
                    && hasNoNV(bratDocument, i, idx_next1)&&hasNoPrep(bratDocument,i,idx_next1, "of")
                    &&countNoun(bratDocument,i,idx_next1)==0 &&!getSegment(bratDocument, i, idx_next1).contains("out of")) {
                if (!trajector.getText().equals("one")&&!trajector.getText().startsWith("one ")) {
                    if (!WordData.getNot_notrigger_of().contains(bratDocument.getParseTree().getLemma(trajector.getStart()).toLowerCase())) {
                        if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(next1)) {
                            OTLINK newlink = new OTLINK(-1);
                            newlink.setRule_id("NT5");
                            setLink(idx_next1, i, newlink, entityList);
                            bratDocument.noCandidate(idx_next1);
                            return newlink;
                        }
                    }
                }
            }
        }

//        if (level == 1 && next1 != null) {
//            if (inSegment_true(bratDocument, i, idx_next1) &&
//                    (getSegment(bratDocument, i, idx_next1).contains(" of "))
//                    && hasNoNV(bratDocument, i, idx_next1)&&hasNoPrep(bratDocument,i,idx_next1, "of")
//                    &&countNoun(bratDocument,i,idx_next1)==0 &&!getSegment(bratDocument, i, idx_next1).contains("out of")) {
//                if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(next1)) {
//                    if (trajector.getText().equals("one") || trajector.getText().startsWith("one ")
//                    ||WordData.getNot_notrigger_of().contains(bratDocument.getParseTree().getLemma(trajector.getStart()).toLowerCase())) {
//                        bratDocument.noCandidate(i);
//                    }
//                }
//            }
//        }

//        //<trajector>to<landmark>
//        //1
//        if (level == 1 && next1 != null) {
//            if (inSegment(bratDocument, i, idx_next1) && getSegment(bratDocument, i, idx_next1).contains(" to ")
//                    && hasNoNV(bratDocument, i, idx_next1)&&hasNoPrep(bratDocument,i,idx_next1, "to")
//                    &&getSegment(bratDocument, i, idx_next1).split(" ").length<=3) {
//                if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(next1)) {
//                    OTLINK newlink = new OTLINK(-1);
//                    setLink(idx_next1, i, newlink,entityList);
//                    bratDocument.noCandidate(idx_next1);
//                    return newlink;
//                }
//            }
//        }


        if (bratDocument.getTrigger()==null){

            //<landmark>to<trajector[v]>
            //1
            //no trigger
            if (level == 1 && last1 != null) {
                if (inSegment_true(bratDocument, idx_last1, i) && getSegment(bratDocument, idx_last1, i).equals(" to ")){
//                        && countNoun(bratDocument, idx_last1, i) <= 1 && countVerb(bratDocument, idx_last1, i) == 0
//                        &&hasNoPrep(bratDocument,idx_last1, i, "to")) {
                    if (JudgeEntity.isEvent(trajector) && JudgeEntity.canbeLandmark(last1)) {
                        OTLINK newlink = new OTLINK(-1);
                        setLink(idx_last1, i, newlink,entityList);
//                    bratDocument.noCandidate(idx_next1);
                        return newlink;
                    }
                }
            }


            //<landmark>(that)[n]<Trajector[v]>
            //1
            if (level==1&&last2!=null){
                if (last1.getEnd()+1==trajector.getStart()){
                    if (getSegment(bratDocument,idx_last2,idx_last1).equals(" ")||getSegment(bratDocument,idx_last2,idx_last1).equals(" that ")){
                        if (JudgeEntity.canbeMover_NotStrict(last1)&&JudgeEntity.isEvent(trajector)&&JudgeEntity.canbeLandmark(last2)){
                            OTLINK newlink = new OTLINK(-1);
                            newlink.setRule_id("NT16");
                            setLink(idx_last2, i, newlink, entityList);
                            return newlink;
                        }
                    }
                }
            }

            //<trajector>at<landmark>
            //1
            if (level == 1 && next1 != null) {
                if (inSegment(bratDocument, i, idx_next1) &&
                        (getSegment(bratDocument, i, idx_next1).contains(" at "))
                        && hasNoNV(bratDocument, i, idx_next1)&&hasNoPrep(bratDocument,i,idx_next1,"at")
                        &&countNoun(bratDocument,i,idx_next1)==0) {
                    if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(next1)) {
                        OTLINK newlink = new OTLINK(-1);
                        newlink.setRule_id("NT6");
                        setLink(idx_next1, i, newlink, entityList);
                        bratDocument.noCandidate(idx_next1);
                        return newlink;
                    }
                }
            }

            //<landmark>with<trajector>
            //1
            //no trigger
            if (level == 1 && last1 != null) {
                if (inSegment_true(bratDocument, idx_last1, i) &&
                        (getSegment(bratDocument, idx_last1, i).contains(" with ")||getSegment(bratDocument, idx_last1, i).startsWith("with "))
                        && hasNoNV(bratDocument, idx_last1, i)&&hasNoPrep(bratDocument,idx_last1, i, "with")) {
                    if (JudgeEntity.canbeLandmark(last1)) {
                        OTLINK newlink = new OTLINK(-1);
                        newlink.setRule_id("NT15");
                        setLink(idx_last1, i, newlink,entityList);
                        if (!JudgeEntity.isEvent(trajector)) bratDocument.noCandidate(i);
//                    bratDocument.noCandidate(idx_next1);
                        return newlink;
                    }
                }
            }

            //<trajector>which be<landmark>
            //1
            //no trigger
            if (level == 1 && last1 != null) {
                if (inSegment_true(bratDocument, idx_last1, i) &&
                        (getSegmentOrigin(bratDocument, idx_last1, i).contains("which be "))
                        && countNoun_position(bratDocument,last1.getStart(),trajector.getEnd())==2&&hasNoPrep(bratDocument,idx_last1, i, "")) {
                    if (JudgeEntity.canbeLandmark(last1)) {
                        OTLINK newlink = new OTLINK(-1);
                        newlink.setRule_id("NT14");
                        setLink(i,idx_last1, newlink,entityList);
                        if (!JudgeEntity.isEvent(trajector)) bratDocument.noCandidate(i);
//                    bratDocument.noCandidate(idx_next1);
                        return newlink;
                    }
                }
            }
//
//            //<landmark>with<trajector>
//            //5
//            //no trigger
//            if (level ==5 && last1 != null) {
//                if (inSegment(bratDocument, idx_last1, i) && getSegment(bratDocument, idx_last1, i).contains(" with ")
//                        && hasNoNV(bratDocument, idx_last1, i)&&hasNoPrep(bratDocument,idx_last1, i, "with")) {
//                    if (JudgeEntity.canbeLandmark(last1)) {
//                        OTLINK newlink = new OTLINK(-1);
//                        setLink(idx_last1, i, newlink);
//                        if (!JudgeEntity.isEvent(trajector)) bratDocument.noCandidate(i);
////                    bratDocument.noCandidate(idx_next1);
//                        return newlink;
//                    }
//                }
//            }

            //<landmark>'s<trajector>
            //1
            //no trigger
            if (level == 1 && last1 != null) {
                if (inSegment_true(bratDocument, idx_last1, i) && (getSegment(bratDocument, idx_last1, i).contains("'s") || getSegment(bratDocument, idx_last1, i).contains("’s"))
                        && countVerb(bratDocument, idx_last1, i) == 0 && countNoun_position(bratDocument, last1.getEnd(), trajector.getEnd()) == 1) {
                    if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(last1)) {
                        OTLINK newlink = new OTLINK(-1);
                        newlink.setRule_id("NT7");
                        setLink(idx_last1, i, newlink, entityList);
                        bratDocument.noCandidate(idx_last1);
                        return newlink;
                    }
                }
            }

//            //<landmark[n]>-<trajector[n]>
//            //0
//            //no trigger
//            if (level == 1 && last1 != null) {
//                if (last1.getEnd() + 1 == trajector.getStart()
//                        && countNoun_position(bratDocument, last1.getStart(), trajector.getEnd()) == 1
//                        && countNoun_position(bratDocument, trajector.getStart(), trajector.getEnd()) == 1
//                        && countNoun_position(bratDocument, last1.getStart(), last1.getEnd()) == 1) {
//                    if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(last1)) {
//                        OTLINK newlink = new OTLINK(-1);
//                        setLink(idx_last1, i, newlink,entityList);
//                        bratDocument.noCandidate(idx_last1);
//                        return newlink;
//                    }
//                }
//            }

//            //<landmark>for<trajector>
//            //1
//            //no trigger
//            if (level == 1 && last1 != null) {
//                if (inSegment_true(bratDocument, idx_last1, i) && getSegment(bratDocument, idx_last1, i).contains(" for ")
//                        && hasNoNV(bratDocument, idx_last1, i)&&hasNoPrep(bratDocument,idx_last1, i, "for")) {
//                    if (JudgeEntity.canbeLandmark(last1)) {
//                        OTLINK newlink = new OTLINK(-1);
//                        setLink(idx_last1, i, newlink,entityList);
//                        if (!JudgeEntity.isEvent(trajector)) bratDocument.noCandidate(i);
////                    bratDocument.noCandidate(idx_next1);
//                        return newlink;
//                    }
//                }
//            }

            //记录删除
//            //<landmark>of<trajector[EVENT]>
//            //1
//            //no trigger
//            if (level == 1 && last1 != null) {
//                if (inSegment_true(bratDocument, idx_last1, i) && getSegment(bratDocument, idx_last1, i).contains(" of ")
//                        && hasNoNV(bratDocument, idx_last1, i)&&hasNoPrep(bratDocument,idx_last1, i, "of")) {
//                    if (JudgeEntity.canbeLandmark(last1) && JudgeEntity.isEvent(trajector)) {
//                        OTLINK newlink = new OTLINK(-1);
//                        newlink.setRule_id("NT8");
//                        setLink(idx_last1, i, newlink);
////                    bratDocument.noCandidate(idx_next1);
//                        return newlink;
//                    }
//                }
//            }

//            //<landmark>, <trajector[EVENT]>
//            //5
//            //no trigger
//            //TODO:好多问题
//            if (level == 5 && last1 != null) {
//                if (inSegment(bratDocument, idx_last1, i)
//                        && (getSegment(bratDocument, idx_last1, i).equals(" , ")||getSegment(bratDocument, idx_last1, i).equals(" , the "))) {
//                    if (JudgeEntity.canbeLandmark(last1)) {
////                    //here, [n] [v] [n]
////                    boolean ok;
////                    if (last1.getText().toLowerCase().toLowerCase().equals("here")||last1.getText().toLowerCase().toLowerCase().equals("there")){
////                        ok = c
////                    }
////                    //landmark, [ving] [n]
//                        OTLINK newlink = new OTLINK(-1);
//                        setLink(idx_last1, i, newlink);
//                        bratDocument.noCandidate(i);
//                        return newlink;
//                    }
//                }
//            }

//            //<trajector>is<landmark>
//            //5
//            //no trigger
//            if (level == 5 && next1 != null) {
//                if (inSegment(bratDocument, i, idx_next1)
//                        && (getSegmentOrigin(bratDocument, i, idx_next1).startsWith("be ")||getSegmentOrigin(bratDocument, i, idx_next1).contains(" be "))
//                        && countVerb_all_strict(bratDocument, i, idx_next1) <= 1
//                        && (countNoun_position(bratDocument, trajector.getStart(), next1.getEnd()) <= 2||countNoun(bratDocument,i,idx_next1)==0)
//                        &&!getSegment(bratDocument,i,idx_next1).contains(",")
//                        &&!getSegment(bratDocument,i,idx_next1).contains("which")) {
//                    if (!next1.getText().equals("place")&&!next1.getText().equals("avenue")) {
//                        if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark_strict(next1)) {
//                            OTLINK newlink = new OTLINK(-1);
//                            newlink.setRule_id("NT13");
//                            if (trajector.getText().toLowerCase().contains("here"))
//                                setLink(i,idx_next1, newlink, entityList);
//                            else
//                                setLink(idx_next1, i, newlink, entityList);
//                            bratDocument.noCandidate(idx_next1);
//                            return newlink;
//                        }
//                    }
//                }
//            }

//            //<trajector>have<landmark>
//            //5
//            //no trigger
//            if (level == 5 && next1 != null) {
//                if (inSegment(bratDocument, i, idx_next1) && (" "+getSegmentOrigin(bratDocument, i, idx_next1)).contains(" have ")
//                        && countVerb_all(bratDocument, i, idx_next1) <= 1
//                        && (countNoun_position(bratDocument, trajector.getStart(), next1.getEnd()) == 2||countNoun(bratDocument,i,idx_next1)==0)) {
//                    if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(next1)) {
//                        OTLINK newlink = new OTLINK(-1);
//                        setLink(i,idx_next1, newlink,entityList);
//                        bratDocument.noCandidate(idx_next1);
//                        return newlink;
//                    }
//                }
//            }

            //<landmark>is<trajector[ved]>
            //5
            //no trigger
            if (level == 5 && last1 != null) {
                if (inSegment(bratDocument, idx_last1, i)
                        && (getSegmentOrigin(bratDocument, idx_last1,i).startsWith("be ")||getSegmentOrigin(bratDocument, idx_last1,i).contains(" be "))
                        && countNoun(bratDocument, idx_last1, i) == 0
                        && countVerb(bratDocument, idx_last1, i) == 0) {
                    if (JudgeEntity.canbeLandmark(last1) && JudgeEntity.isEvent(trajector)
                            && isVerb(trajector, bratDocument) && trajector.getText().toLowerCase().endsWith("ed")) {
                        OTLINK newlink = new OTLINK(-1);
                        newlink.setRule_id("NT9");
                        setLink(idx_last1, i, newlink, entityList);
                        bratDocument.noCandidate(i);
                        return newlink;
                    }
                }
            }
        }



        //<trajector>is part of<landmark>
        //4
        if (level == 4 && next1 != null) {
            if (inSegment(bratDocument, i, idx_next1) && getSegmentOrigin(bratDocument, i, idx_next1).contains("be part of ")) {
                if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(next1)) {
                    OTLINK newlink = new OTLINK(-1);
                    newlink.setRule_id("NT10");
                    setLink(idx_next1, i, newlink, entityList);
                    bratDocument.noCandidate(idx_next1);
                    bratDocument.noCandidate(i);
                    return newlink;
                }
            }
        }

        //<landmark>is home to<trajector>
        //4
        if (level == 4 && last1 != null) {
            if (inSegment(bratDocument, idx_last1, i) && getSegmentOrigin(bratDocument, idx_last1, i).contains("be home to ")) {
                if (countNoun_position(bratDocument,last1.getStart(),trajector.getEnd())==3||countNoun(bratDocument,idx_last1,i)==1) {
                    if (countVerb_all(bratDocument,idx_last1,i)==1) {
                        if (JudgeEntity.canbeLandmark(last1) && JudgeEntity.canbeTrajector(trajector)) {
                            OTLINK newlink = new OTLINK(-1);
                            setLink(idx_last1, i, newlink, entityList);
                            newlink.setRule_id("NT11");
                            bratDocument.noCandidate(idx_last1);
//                    bratDocument.noCandidate(i);
                            return newlink;
                        }
                    }
                }
            }
        }

        //<landmark>is rich in<trajector>
        //4
        if (level == 4 && last1 != null) {
            if (inSegment(bratDocument, idx_last1, i) && getSegmentOrigin(bratDocument, idx_last1, i).contains("be rich in ")) {
                if (countNoun_position(bratDocument,last1.getStart(),trajector.getEnd())==2||countNoun(bratDocument,idx_last1,i)==0) {
                    if (countVerb_all(bratDocument,idx_last1,i)==1) {
                        if (JudgeEntity.canbeLandmark(last1) && JudgeEntity.canbeTrajector(trajector)) {
                            OTLINK newlink = new OTLINK(-1);
                            setLink(idx_last1, i, newlink, entityList);
                            newlink.setRule_id("NT12");
                            bratDocument.noCandidate(idx_last1);
//                    bratDocument.noCandidate(i);
                            return newlink;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static int countVerb_all(BratDocumentwithList bratDocument, int idx1, int idx2) {
        int p1 = bratDocument.getEntityList().get(idx1).getEnd();
        int p2 = bratDocument.getEntityList().get(idx2).getStart();
        ParseTree t = bratDocument.getParseTree();
        int count = 0;
        for (int p=p1;p<p2;p++){
            String POS = t.getPOS(p);
            if (POS!=null&&POS.startsWith("V")&&!POS.equals("VBN")&&!POS.equals("VBG")) count++;
        }
        return count;
    }

    private static int countVerb_all_strict(BratDocumentwithList bratDocument, int idx1, int idx2) {
        int p1 = bratDocument.getEntityList().get(idx1).getEnd();
        int p2 = bratDocument.getEntityList().get(idx2).getStart();
        ParseTree t = bratDocument.getParseTree();
        int count = 0;
        for (int p=p1;p<p2;p++){
            String POS = t.getPOS(p);
            if (POS!=null&&POS.startsWith("V")) count++;
        }
        return count;
    }

    private static boolean hasNoPrep(BratDocumentwithList bratDocument, int i, int idx_next1, String except) {
        String seg = getSegmentOrigin(bratDocument,i,idx_next1);
        for (String word: WordData.getPrepList()){
            if ((seg.equals(word)||seg.startsWith(word+" ")||seg.endsWith(" "+word)||seg.contains(" "+word+" "))&&!word.equals(except)) return false;
        }
        return true;
    }

    private static boolean isRB(BratEntity next1) {
        return next1.getText().toLowerCase().equals("there") || next1.getText().toLowerCase().equals("here")
                || next1.getText().toLowerCase().equals("somewhere")||next1.getText().toLowerCase().equals("downstairs");
    }

    private static int getNext_ignoremeasure(int index, BratDocumentwithList bratDocument) {
        if (index == -1) return -1;
        int i = index + 1;
        while (i < bratDocument.getEntityList().size()) {
            if (bratDocument.getIsCandidate(i) && !bratDocument.getEntityList().get(i).getTag().equals(BratUtil.MEASURE))
                break;
            i++;
        }
        if (i >= bratDocument.getEntityList().size()) return -1;
        else return i;
    }

    private static int getLast_ignoremeasure(int index, BratDocumentwithList bratDocument) {
        if (index == -1) return -1;
        int i = index - 1;
        while (i >= 0) {
            if (bratDocument.getIsCandidate(i) && !bratDocument.getEntityList().get(i).getTag().equals(BratUtil.MEASURE))
                break;
            i--;
        }
        if (i < 0) return -1;
        else return i;
    }
}
