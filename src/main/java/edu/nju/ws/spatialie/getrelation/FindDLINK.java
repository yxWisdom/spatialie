package edu.nju.ws.spatialie.getrelation;

import edu.nju.ws.spatialie.Link.DLINK;
import edu.nju.ws.spatialie.data.BratDocumentwithList;
import edu.nju.ws.spatialie.data.BratEntity;

import java.util.List;

public class FindDLINK extends FindLINK {
    public static DLINK findDLINK(BratDocumentwithList bratDocument, List<DLINK> dlinklist, int level) {
        for (DLINK dlink : dlinklist) {
            if (isDLINK(dlink, bratDocument, level) != null) {
                return dlink;
            }
        }
        return null;
    }

    private static DLINK isDLINK(DLINK dlink, BratDocumentwithList bratDocument, int level) {
        if (dlink.isIscompleted()) return null;
        List<BratEntity> entityList = bratDocument.getEntityList();
        int index = dlink.getVal();
        BratEntity trigger = entityList.get(index);
        int idx_last1 = getLast(index, bratDocument);
        int idx_last2 = getLast(idx_last1, bratDocument);
        int idx_next1 = getNext(index, bratDocument);
        int idx_next2 = getNext(idx_next1, bratDocument);
        int idx_next3 = getNext(idx_next2, bratDocument);
        BratEntity last1 = getEntity(idx_last1, entityList);
        BratEntity last2 = getEntity(idx_last2, entityList);
        BratEntity next1 = getEntity(idx_next1, entityList);
        BratEntity next2 = getEntity(idx_next2, entityList);
        BratEntity next3 = getEntity(idx_next3, entityList);

//        if (trigger.getText().equals("near")&&level==2){
//            System.out.println();
//        }


//        // TODO：out of等问题
//        if (isPrep(trigger, bratDocument)||bratDocument.getContent().contains(trigger.getText()+" away from")
//                ||bratDocument.getContent().contains(trigger.getText()+" out of")) {
//            //<trigger>-<landmark>there be<trajector>
//            //5
//            if (level == 5 && next2 != null) {
//                if (inSentence(bratDocument, index, idx_next2) && hasNoNV(bratDocument, index, idx_next1)) {
//                    String temp_s = getSegmentOrigin(bratDocument, idx_next1, idx_next2);
//                    if (temp_s.contains("there be")) {
//                        if (JudgeEntity.canbeLandmark(next1) && JudgeEntity.canbeTrajector(next2)) {
//                            setLink(idx_next1, idx_next2, dlink);
//                            bratDocument.noCandidate(idx_next1);
//                            bratDocument.noCandidate(idx_next2);
//                            bratDocument.noCandidate(index);
//                            return dlink;
//                        }
//                    }
//                }
//            }
//
//            //<trajector>that/which/who be<trigger><landmark>
//            //2
//            if (level == 2 && last1 != null && next1 != null) {
//                if (inSegment(bratDocument, idx_last1, idx_next1) && hasNoNV(bratDocument, index, idx_next1)) {
//                    String temp_s = getSegment(bratDocument, idx_last1, index);
//                    if (temp_s.contains("that") || temp_s.contains("which") || temp_s.contains("who")) {
//                        if (JudgeEntity.canbeLandmark(next1) && JudgeEntity.canbeTrajector(last1)) {
//                            setLink(idx_next1, idx_last1, dlink);
//                            bratDocument.noCandidate(idx_next1);
//                            bratDocument.noCandidate(index);
//                            return dlink;
//                        }
//                    }
//                }
//            }
//
//            //<trigger>-<landmark><trajector[n]>(<trajector[v]>)
//            //5
//            if (level == 5 && next2 != null) {
//                if (inSentence(bratDocument, index, idx_next2) && hasNoNV(bratDocument, index, idx_next1) && hasNoNV(bratDocument, idx_next1, idx_next2)) {
//                    if (JudgeEntity.canbeLandmark(next1) && JudgeEntity.canbeTrajector(next2)) {
//                        setLink(idx_next1, idx_next2, dlink);
//                        bratDocument.noCandidate(idx_next1);
////                        bratDocument.noCandidate(idx_next2);
//                        bratDocument.noCandidate(index);
//                        if (next3 != null && inSegment(bratDocument, idx_next2, idx_next3) && JudgeEntity.isEvent(next3)) {
//                            dlink.addTrajectors(idx_next3);
//                            dlink.removeTrajector(idx_next2);
////                            bratDocument.noCandidate(idx_next3);
//                            if (dlink != null) dlink.addTrajectors(idx_next3);
//                        }
//                        return dlink;
//                    }
//                }
//            }
//
//            //<trigger>-<landmark>[n]<trajector[v]>
//            //5
//            if (level == 5 && next2 != null) {
//                if (inSentence(bratDocument, idx_last1, idx_next1) && hasNoNV(bratDocument, index, idx_next1)) {
//                    if (JudgeEntity.canbeLandmark(next1) && JudgeEntity.isEvent(next2) && countNoun(bratDocument, idx_next1, idx_next2) == 1) {
//                        setLink(idx_next1, idx_next2, dlink);
//                        bratDocument.noCandidate(idx_next1);
//                        bratDocument.noCandidate(idx_next2);
//                        bratDocument.noCandidate(index);
//                        return dlink;
//                    }
//                }
//            }
//
//            //<trajector[n]>[v]<trigger>-<landmark>
//            //4
//            if (level == 4 && last1 != null && next1 != null) {
//                int c = countVerb(bratDocument, idx_last1, index);
//                int c2 = countWord("be", bratDocument, idx_last1, index);
//                boolean hasNoun = hasPOS("N", bratDocument, idx_last1, index);
//                if ((c == 1 || c2 == 1) && (!hasNoun || checkOrderVN(bratDocument, idx_last1, index))) {
//                    if (inSentence(bratDocument, idx_last1, idx_next1) && hasNoNV(bratDocument, index, idx_next1)) {
//                        if (JudgeEntity.canbeLandmark(next1) && JudgeEntity.canbeTrajector(last1)) {
//                            setLink(idx_next1, idx_last1, dlink);
////                            bratDocument.noCandidate(idx_last1);
//                            bratDocument.noCandidate(idx_next1);
//                            bratDocument.noCandidate(index);
//                            return dlink;
//                        }
//                    }
//                }
//            }
//
//
//            //<trajector[n]><trigger>-<landmark>
//            //2
//            if ((level == 2 || level == 4) && last1 != null && next1 != null) {
//                if (inSegment_true(bratDocument, idx_last1, idx_next1)) {
//                    if (!hasPOS("N", bratDocument, idx_last1, index) && !hasPOS("V", bratDocument, idx_last1, index) && hasNoNV(bratDocument, index, idx_next1)) {
//                        if (JudgeEntity.canbeLandmark(next1) && JudgeEntity.canbeTrajector(last1)) {
//                            setLink(idx_next1, idx_last1, dlink);
//                            bratDocument.noCandidate(idx_next1);
//                            bratDocument.noCandidate(index);
//                            //<trajector[v]><trajector[n]><trigger><landmark>
//                            //4
//                            if (level == 4 && last2 != null) {
//                                if (inSegment(bratDocument, idx_last2, idx_last1) && hasNoNV(bratDocument, idx_last2, idx_last1)) {
//                                    if (JudgeEntity.isEvent(last2)) {
//                                        dlink.addTrajectors(idx_last2);
//                                        bratDocument.noCandidate(idx_last2);
//                                        bratDocument.noCandidate(idx_last1);
//                                        if (dlink != null) dlink.addTrajectors(idx_last2);
////                                        <trajector[n]><trajector[v]><trajector[n]><trigger><landmark>
////                                        if (last3 != null) {
////                                            if (inSentence(bratDocument, idx_last3, idx_last2)) {
////                                                if (JudgeEntity.canbeTrajector(last3)) {
////                                                    dlink.addTrajectors(idx_last3);
//////                                                bratDocument.noCandidate(idx_last3);
////                                                    if (dlink != null) dlink.addTrajectors(idx_last3);
////                                                }
////                                            }
////                                        }
//                                    }
//                                }
//                            }
//                            return dlink;
//                        }
//                    }
//                }
//            }
//
//            if ((level == 4) && last1 != null && next1 != null) {
//                if (!inSegment_true(bratDocument, idx_last1, idx_next1)) {
//                    if (!hasPOS("N", bratDocument, idx_last1, index) && !hasPOS("V", bratDocument, idx_last1, index) && hasNoNV(bratDocument, index, idx_next1)) {
//                        if (JudgeEntity.canbeLandmark(next1) && JudgeEntity.canbeTrajector(last1)) {
//                            //<trajector[v]><trajector[n]><trigger><landmark>
//                            //4
//                            if (last2 != null) {
//                                if (inSegment(bratDocument, idx_last2, idx_last1) && hasNoNV(bratDocument, idx_last2, idx_last1)) {
//                                    if (JudgeEntity.isEvent(last2)) {
//                                        setLink(idx_next1, idx_last2, dlink);
//                                        bratDocument.noCandidate(idx_last2);
//                                        bratDocument.noCandidate(idx_next1);
//                                        bratDocument.noCandidate(index);
//                                        bratDocument.noCandidate(idx_last1);
//                                        return dlink;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            //<trajector[v]><trigger>-<landmark>
//            //4
//            if (level == 4 && last1 != null && next1 != null) {
//                if (inSegment(bratDocument, idx_last1, idx_next1)) {
//                    if (countNoun(bratDocument, idx_last1, index) <= 1 && !hasPOS("V", bratDocument, idx_last1, index) && hasNoNV(bratDocument, index, idx_next1)) {
//                        if (JudgeEntity.canbeLandmark(next1) && JudgeEntity.isEvent(last1)) {
//                            setLink(idx_next1, idx_last1, dlink);
//                            bratDocument.noCandidate(idx_next1);
//                            bratDocument.noCandidate(idx_last1);
//                            bratDocument.noCandidate(index);
//                            return dlink;
//                        }
//                    }
//                }
//            }
//        }

        //<trajector>is<val>[prep]<landmark>
        //4
        if (level==4&&last1!=null&&next1!=null){
            if (inSentence(bratDocument,idx_last1,idx_next1)){
                String temp_s = getSegmentOrigin(bratDocument,idx_last1,index);
                String temp_s2 = getSegment(bratDocument,index,idx_next1);
                //trigger 有2km这种，也有near这种
                if (temp_s.contains("be")&&(endwithPrep(temp_s2)||endwithPrep(trigger.getText()))){
                    if (JudgeEntity.canbeLandmark(next1)&&JudgeEntity.canbeTrajector(last1)){
                        setLink(idx_next1,idx_last1,dlink);
                        dlink.setRule_id("DL1");
                        bratDocument.noCandidate(index);
                        bratDocument.noCandidate(idx_next1);
                        bratDocument.noCandidate(idx_last1);
                        return dlink;
                    }
                }
            }
        }

        //<trajector[v]>(n)<val>[prep]<landmark>
        //4
        if (level==4&&last1!=null&&next1!=null){
            if (inSentence(bratDocument,idx_last1,idx_next1)){
//                String temp_s = getSegmentOrigin(bratDocument,idx_last1,index);
                String temp_s2 = getSegment(bratDocument,index,idx_next1);
                if (countNoun(bratDocument,idx_last1,index)<=1&&(endwithPrep(temp_s2)||endwithPrep(trigger.getText()))){
                    if (JudgeEntity.canbeLandmark(next1)&&JudgeEntity.isEvent(last1)){
                        setLink(idx_next1,idx_last1,dlink);
                        dlink.setRule_id("DL2");
                        bratDocument.noCandidate(index);
                        bratDocument.noCandidate(idx_next1);
                        bratDocument.noCandidate(idx_last1);
                        return dlink;
                    }
                }
            }
        }

        //<val>[prep]<landmark>[n]<trajector[v]>(n)
        //5
        if (level==5&&next2!=null){
            if (inSentence(bratDocument,index,idx_next2)){
//                String temp_s = getSegmentOrigin(bratDocument,idx_last1,index);
                String temp_s2 = getSegment(bratDocument,index,idx_next1);
                if (countNoun(bratDocument,idx_next1,idx_next2)==1&&(endwithPrep(temp_s2)||endwithPrep(trigger.getText()))){
                    if (JudgeEntity.canbeLandmark(next1)&&JudgeEntity.isEvent(next2)){
                        setLink(idx_next1,idx_next2,dlink);
                        dlink.setRule_id("DL3");
                        bratDocument.noCandidate(index);
                        bratDocument.noCandidate(idx_next1);
                        bratDocument.noCandidate(idx_next2);
                        return dlink;
                    }
                }
            }
        }

        //<val>[prep]<landmark>is<trajector>
        //5
        if (level==5&&next2!=null){
            if (inSentence(bratDocument,index,idx_next2)){
                String temp_s = getSegmentOrigin(bratDocument,idx_next1,idx_next2);
                String temp_s2 = getSegment(bratDocument,index,idx_next1);
                if (temp_s.contains("be")&&(endwithPrep(temp_s2)||endwithPrep(trigger.getText()))){
                    if (JudgeEntity.canbeLandmark(next1)&&JudgeEntity.canbeTrajector(next2)){
                        setLink(idx_next1,idx_next2,dlink);
                        dlink.setRule_id("DL4");
                        bratDocument.noCandidate(index);
                        bratDocument.noCandidate(idx_next1);
                        bratDocument.noCandidate(idx_next2);
                        return dlink;
                    }
                }
            }
        }

        //<trajector><val>[prep]<landmark>
        //2
        if (level==2&&last1!=null&&next1!=null){
            if (inSentence(bratDocument,idx_last1,idx_next1)){
                String temp_s = getSegment(bratDocument,index,idx_next1);
                if ((endwithPrep(temp_s)||endwithPrep(trigger.getText()))){
                    if (JudgeEntity.canbeLandmark(next1)&&JudgeEntity.canbeTrajector(last1)){
                        setLink(idx_next1,idx_last1,dlink);
                        dlink.setRule_id("DL5");
                        bratDocument.noCandidate(index);
                        bratDocument.noCandidate(idx_next1);
                        return dlink;
                    }
                }
            }
        }

        //<trajector[here]>to[landmark][n]be/have<val>
        //4
        if (level==4&&last2!=null){
            if (inSentence(bratDocument,idx_last2,index)){
                String temp_s = getSegment(bratDocument,idx_last2,idx_last1);
                String temp_s2 = getSegmentOrigin(bratDocument,idx_last1,index);
                if (temp_s.contains("to")&&(temp_s2.contains("be")||temp_s2.contains("have"))){
                    if (JudgeEntity.canbeLandmark(last1)&&JudgeEntity.canbeTrajector(last2)){
                        setLink(idx_last1,idx_last2,dlink);
                        dlink.setRule_id("DL6");
                        bratDocument.noCandidate(index);
                        bratDocument.noCandidate(idx_last2);
                        bratDocument.noCandidate(idx_last1);
                        return dlink;
                    }
                }
            }
        }

        //<trajector>have/is<val>to<landmark>
        //4
        if (level==4&&last1!=null&&next1!=null){
            if (inSentence(bratDocument,idx_last1,idx_last2)){
                String temp_s = getSegment(bratDocument,index,idx_next1);
                if (temp_s.contains("to")){
                    if (JudgeEntity.canbeLandmark(next1)&&JudgeEntity.canbeTrajector(last1)){
                        setLink(idx_next1,idx_last1,dlink);
                        dlink.setRule_id("DL7");
                        bratDocument.noCandidate(index);
                        bratDocument.noCandidate(idx_next1);
                        bratDocument.noCandidate(idx_last1);
                        return dlink;
                    }
                }
            }
        }
        //<trajector>,the distance to [landmark] is <val>
        //5
        if (level==5&&last2!=null){
            if (inSentence(bratDocument,idx_last2,index)){
                String temp_s = getSegment(bratDocument,idx_last2,idx_last1);
                String temp_s2 = getSegmentOrigin(bratDocument,idx_last1,index);
                if (temp_s.contains("to")&&temp_s.contains("distance")&&(temp_s2.contains("be"))){
                    if (JudgeEntity.canbeLandmark(last1)&&JudgeEntity.canbeTrajector(last2)){
                        setLink(idx_last1,idx_last2,dlink);
                        dlink.setRule_id("DL8");
                        bratDocument.noCandidate(index);
                        bratDocument.noCandidate(idx_last2);
                        bratDocument.noCandidate(idx_last1);
                        return dlink;
                    }
                }
            }
        }
        return null;
    }



    private static boolean hasPrep(String temp_s) {
        for (String s:WordData.getPrepList()){
            if (temp_s.contains(s)) return true;
        }
         return false;
    }

    private static void setLink(int idx_l, int idx_t, DLINK link) {
        link.addLandmarks(idx_l);
        link.addTrajectors(idx_t);
        link.Complete();
    }
}
