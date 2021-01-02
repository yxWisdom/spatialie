package edu.nju.ws.spatialie.getrelation;

import edu.nju.ws.spatialie.Link.LINK;
import edu.nju.ws.spatialie.Link.OTLINK;
import edu.nju.ws.spatialie.data.BratUtil;
import edu.nju.ws.spatialie.data.BratDocumentwithList;
import edu.nju.ws.spatialie.data.BratEntity;
import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.data.ParseTree;

import java.util.*;

public class FindOTLINK_notrigger extends FindLINK {

    public static Map<String, BratEntity> flagMap = new HashMap<>();

    private static void setLink(int idx_l, int idx_t, OTLINK link, List<BratEntity> entityList) {

        String t = "";
        if (link.getTrigger() != -1) {
            t = entityList.get(link.getTrigger()).getText();
        }
        if (NeedChangeLT(t)) {
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
        for (String s : changeList) {
            if (t.contains(s + " ") || t.contains(" " + s) || t.equals(s)) return true;
        }
        return false;
    }


    public static List<OTLINK> findOTLINKwithoutTrigger(BratDocumentwithList bratDocument, int i, List<LINK> linkList, int level) {
        List<OTLINK> res = new ArrayList<>();
        List<BratEntity> entityList = bratDocument.getEntityList();
        BratEntity trajector = getEntity(i, entityList);
        if (!bratDocument.getIsCandidate(i)) return res;
        assert trajector != null;
        if (!JudgeEntity.canbeTrajector(trajector) && !JudgeEntity.isEvent(trajector)&&!JudgeEntity.canbeLandmark(trajector)) return null;

        int idx_last1;
        int idx_next1;
        int idx_last2;

        idx_last1 = getLast_ignore(i, bratDocument);
        idx_last2 = getLast_ignore(idx_last1, bratDocument);
        idx_next1 = getNext_ignore(i, bratDocument);

        BratEntity last1 = getEntity(idx_last1, entityList);
        BratEntity next1 = getEntity(idx_next1, entityList);
        BratEntity last2 = getEntity(idx_last2, entityList);


//<trajector>(<landmark>)
        //0
        if (level==0&&next1 != null) {
            String temp_s = getSegment(bratDocument, i, idx_next1);
            if (inSegment(bratDocument, i, idx_next1) && temp_s.contains("(") && !temp_s.contains(")")
                    && hasNoNV(bratDocument, i, idx_next1)&&idx_next1==getNext(i,bratDocument)) {
                if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(next1)) {
                    OTLINK newlink = new OTLINK(-1);
                    setLink(idx_next1, i, newlink, entityList);
                    newlink.setRule_id("NT1");
                    int begin = bratDocument.getContent().substring(trajector.getEnd(), next1.getStart()).indexOf("(") + trajector.getEnd();
                    int end = begin + "(".length();
                    if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(),linkList)) {
                        BratEntity e = new BratEntity(bratDocument, "(", "SpatialFlag", begin, end);
                        newlink.setFlag(bratDocument.getEntityList().size() - 1);
                        // 记录 bratDocument.noCandidate(idx_next1);
                        res.add(newlink);
                    }
                }
            }
        }

        //<trajector[n]><landmark(there/here)>
        //here后一个作为flag
        //0
        if (level==1&&next1 != null) {
            if (inSegment_true(bratDocument, i, idx_next1) && hasNoNV(bratDocument, i, idx_next1)) {
                if (isRB(next1)) {
                    if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(next1)) {
                        OTLINK newlink = new OTLINK(-1);
                        setLink(idx_next1, i, newlink, entityList);
                        if (countVerb_all_strict(bratDocument,i,idx_next1)>0)
                            newlink.setRule_id("NT30");
                        else
                            newlink.setRule_id("NT2");
                        int begin = next1.getEnd() + 1;
                        int end = bratDocument.getContent().substring(begin).indexOf(' ') + begin;
                        if (end < begin) end = bratDocument.getContent().length();
                        if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                            String t = bratDocument.getContent().substring(begin, end);
                            BratEntity e = new BratEntity(bratDocument, t, "SpatialFlag", begin, end);
                            newlink.setFlag(bratDocument.getEntityList().size() - 1);
//                       // 记录 bratDocument.noCandidate(idx_next1);
//                       // 记录 bratDocument.noCandidate(i);
                            res.add(newlink);
                        }
                    }
                }
            }
        }

        //<landmark(there/here)>[v]<trajector[n]>
        //here后一个作为flag
        if (level==1&&last1 != null) {
            if (isRB(last1)) {
                if (countVerb(bratDocument, idx_last1, i) <= 1 && countNoun_position(bratDocument, last1.getEnd(), trajector.getEnd()) == 1
                        && hasNoPrep(bratDocument, idx_last1, i, "")) {
                    if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(last1)) {
                        OTLINK newlink = new OTLINK(-1);
                        if ("there".contains(last1.getText().toLowerCase())&&!"here".contains(last1.getText().toLowerCase())
                                ||"here".contains(last1.getText().toLowerCase())&&"we".contains(trajector.getText().toLowerCase()))
                            setLink( idx_last1,i, newlink, entityList);
                        else
                            setLink(i, idx_last1, newlink, entityList);
                        newlink.setRule_id("NT3");
                        int begin = last1.getEnd() + 1;
                        int end = bratDocument.getContent().substring(begin).indexOf(' ') + begin;
                        if (end < begin) end = bratDocument.getContent().length();
                        //from here we
                        if (begin==trajector.getStart()&&last1.getStart()!=0){
                            end = last1.getStart()-1;
                            begin = bratDocument.getContent().substring(0,end-1).lastIndexOf(' ')+1;
                        }
                        if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                            String t = bratDocument.getContent().substring(begin, end);
                            BratEntity e = new BratEntity(bratDocument, t, "SpatialFlag", begin, end);
                            newlink.setFlag(bratDocument.getEntityList().size() - 1);
                            // 记录 bratDocument.noCandidate(idx_last1);
                            // 记录 bratDocument.noCandidate(i);
                            res.add(newlink);
                        }
                    }
                }
            }
        }

        //<trajector[v]><landmark(there/here)>
        //here后一个作为flag
        if (level==1&&next1 != null&&isRB(next1)) {
            if (inSegment(bratDocument, i, idx_next1) && JudgeEntity.isEvent(trajector)) {
                if (hasNoNV(bratDocument, i, idx_next1)) {

                        if (JudgeEntity.canbeLandmark(next1)) {
                            OTLINK newlink = new OTLINK(-1);
                            setLink(idx_next1, i, newlink, entityList);
                            newlink.setRule_id("NT4");
                            int begin = next1.getEnd() + 1;
                            int end = bratDocument.getContent().substring(begin).indexOf(' ') + begin;
//                            if (end<0||begin<0){
//                                System.out.print("");
//                            }
                            if (end < begin)
                                end = bratDocument.getContent().length();
                            if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                                String t = bratDocument.getContent().substring(begin, end);
                                BratEntity e = new BratEntity(bratDocument, t, "SpatialFlag", begin, end);
                                newlink.setFlag(bratDocument.getEntityList().size() - 1);
                                // 记录 bratDocument.noCandidate(idx_next1);
                                // 记录 bratDocument.noCandidate(i);
                                res.add(newlink);
                            }
                    }
                }
            }
        }

        //TODO:
        //<landmark>(that)[n]<Trajector[v]>
        //0
        if (level==0&&last2 != null) {
            if (last1.getEnd() + 1 == trajector.getStart()) {
                if (getSegment(bratDocument, idx_last2, idx_last1).equals(" ") || getSegment(bratDocument, idx_last2, idx_last1).equals(" that ")) {
                    if (JudgeEntity.canbeMover_NotStrict(last1) && JudgeEntity.isEvent(trajector) && JudgeEntity.canbeLandmark(last2)) {
                        OTLINK newlink = new OTLINK(-1);
                        newlink.setRule_id("NT16");
                        setLink(idx_last2, i, newlink, entityList);
                        int begin = last2.getEnd() + 1;
                        int end = bratDocument.getContent().substring(begin).indexOf(' ') + begin;
                        if (end < begin) end = bratDocument.getContent().length();
                        String t = bratDocument.getContent().substring(begin, end);
                        if (t.equals("that")||t.equals("which")) newlink.setRule_id("NT33");
                        if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                            BratEntity e = new BratEntity(bratDocument, t, "SpatialFlag", begin, end);
                            newlink.setFlag(bratDocument.getEntityList().size() - 1);
                            res.add(newlink);
                        }
                    }
                }
            }
        }

//        //TODO:
        //<landmark>'s<trajector>
        //1
        //no trigger
        if (last1 != null) {
            if (inSegment_true(bratDocument, idx_last1, i) && (getSegment(bratDocument, idx_last1, i).contains("'s") || getSegment(bratDocument, idx_last1, i).contains("’s"))
                    && countVerb(bratDocument, idx_last1, i) == 0 && countNoun_position(bratDocument, last1.getEnd(), trajector.getEnd()) == 1) {
                if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(last1)) {
                    if (" area madrid ".contains(" "+last1.getText().toLowerCase()+" ")) {
                        OTLINK newlink = new OTLINK(-1);
                        newlink.setRule_id("NT7");
                        setLink(idx_last1, i, newlink, entityList);
                        String t = "’s";
                        if (getSegment(bratDocument, idx_last1, i).contains("'s"))
                            t = "'s";
                        int begin = bratDocument.getContent().substring(last1.getEnd(), trajector.getStart()).indexOf(t) + last1.getEnd();
                        int end = begin + t.length();
                        if (!hasBuildRelation(bratDocument.getEntityList(), begin, newlink.getRule_id(), linkList)) {
                            BratEntity e = new BratEntity(bratDocument, t, "SpatialFlag", begin, end);
                            newlink.setFlag(bratDocument.getEntityList().size() - 1);
                            // 记录 if (!JudgeEntity.isEvent(trajector)) bratDocument.noCandidate(i);
//                   // 记录 bratDocument.noCandidate(idx_next1);
                            res.add(newlink);
                        }
                    }
                }
            }
        }

        //<trajector>which be<landmark>
        //1
        //no trigger
        if (level==1&&last1 != null) {
            if (inSegment_true(bratDocument, idx_last1, i) &&
                    (getSegmentOrigin(bratDocument, idx_last1, i).contains("which be "))
                    && countNoun_position(bratDocument, last1.getStart(), trajector.getEnd()) == 2 && hasNoPrep(bratDocument, idx_last1, i, "")) {
                if (JudgeEntity.canbeLandmark(last1)) {
                    OTLINK newlink = new OTLINK(-1);
                    newlink.setRule_id("NT14");
                    setLink(i, idx_last1, newlink, entityList);
                    int begin = bratDocument.getContent().substring(last1.getEnd(), trajector.getStart()).indexOf("which") + last1.getEnd();
                    int end = begin + "which".length();
                    if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                        BratEntity e = new BratEntity(bratDocument, "which", "SpatialFlag", begin, end);
                        newlink.setFlag(bratDocument.getEntityList().size() - 1);
                        // 记录 if (!JudgeEntity.isEvent(trajector)) bratDocument.noCandidate(i);
//                   // 记录 bratDocument.noCandidate(idx_next1);
                        res.add(newlink);
                    }
                }
            }
        }


        //TODO:
        //<landmark[n]>-<trajector[n]>
        //下一个
        //no trigger
        if (level==0&&last1 != null) {
            if (last1.getEnd() + 1 == trajector.getStart()
                    && countNoun_position(bratDocument, last1.getStart(), trajector.getEnd()) == 1
                    && countNoun_position(bratDocument, trajector.getStart(), trajector.getEnd()) == 1
                    && countNoun_position(bratDocument, last1.getStart(), last1.getEnd()) == 1) {
                if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(last1)) {
                    OTLINK newlink = new OTLINK(-1);
                    setLink(idx_last1, i, newlink, entityList);
                    bratDocument.noCandidate(i);//valley X
                    newlink.setRule_id("NT21");
                    int begin = trajector.getEnd() + 1;
                    int end = bratDocument.getContent().substring(begin).indexOf(' ') + begin;
                    if (end < begin) end = bratDocument.getContent().length();
                    if (!hasBuildRelation(bratDocument.getEntityList(),begin,newlink.getRule_id(), linkList)) {
                        String t = bratDocument.getContent().substring(begin, end);
                        BratEntity e = new BratEntity(bratDocument, t, "SpatialFlag", begin, end);
                        newlink.setFlag(bratDocument.getEntityList().size() - 1);
                        res.add(newlink);
                    }
                }
            }
        }

        //<landmark> - <trajector>
        //1
        //no trigger
        if (level==1&&last1 != null) {
            if ((getSegment(bratDocument, idx_last1, i).contains(" -") || getSegment(bratDocument, idx_last1, i).contains(" –"))
                    && hasNoNV(bratDocument, idx_last1, i) && hasNoPrep(bratDocument, idx_last1, i, "-")) {
                if (JudgeEntity.canbeLandmark(last1)) {
                    OTLINK newlink = new OTLINK(-1);
                    setLink(idx_last1, i, newlink, entityList);
                    if ((getSegment(bratDocument, idx_last1, i).equals(" - ") || getSegment(bratDocument, idx_last1, i).contains(" – "))||
                            (getSegment(bratDocument, idx_last1, i).equals(" - the") || getSegment(bratDocument, idx_last1, i).contains(" – the")))
                        newlink.setRule_id("NT29");
                    else
                        newlink.setRule_id("NT23");
                    String t = "–";
                    if (getSegment(bratDocument, idx_last1, i).contains(" -"))
                        t = "-";
                    int begin = getSegment(bratDocument, idx_last1, i).indexOf(t) + last1.getEnd();
                    int end = begin + t.length();
                    if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                        BratEntity e = new BratEntity(bratDocument, t, "SpatialFlag", begin, end);
                        newlink.setFlag(bratDocument.getEntityList().size() - 1);
                        // 记录 if (!JudgeEntity.isEvent(trajector)) bratDocument.noCandidate(i);
                        res.add(newlink);
                    }
                }
            }
        }

        //记录删除
        //<landmark>, <trajector[EVENT]>
        //5
        //no trigger
        if (level==1&&last1 != null) {
            if ((getSegment(bratDocument, idx_last1, i).equals(" , ")||getSegment(bratDocument, idx_last1, i).equals(" , the "))) {
                if (JudgeEntity.canbeTrajector(last1)) {
//                    //landmark, [ving] [n]
                    OTLINK newlink = new OTLINK(-1);
                    if (" terrain garden bogota ".contains(" "+trajector.getText().toLowerCase()+" ")){
                        setLink( i,idx_last1, newlink,entityList);
                    } else {
                        if (next1!=null&&JudgeEntity.isEvent(next1)&&trajector.getEnd()+1==next1.getStart()){
                            String lema = bratDocument.getParseTree().getLemma(next1.getStart());
                            if (!lema.contains("be")&&!lema.equals("do")){
                                setLink(idx_last1, idx_next1, newlink, entityList);
                            } else
                                setLink(idx_last1, i, newlink, entityList);
                        } else
                            setLink(idx_last1, i, newlink, entityList);
                    }
                    // 记录 bratDocument.noCandidate(i);
                    newlink.setRule_id("NT28");
                    int begin = bratDocument.getContent().substring(last1.getEnd(), trajector.getStart()).indexOf(",") + last1.getEnd();
                    int end = begin + ",".length();
                    int lastend = last1.getStart()-1;
                    if (lastend!=-1) {
                        int lastbegin = bratDocument.getContent().substring(0, lastend - 1).lastIndexOf(' ') + 1;
                        //from Pero, I
                        while (!bratDocument.getParseTree().getPOS(lastbegin).startsWith("V")
                                && !bratDocument.getParseTree().getPOS(lastbegin).startsWith("N")
                                && !bratDocument.getParseTree().getPOS(lastbegin).startsWith("IN")) {
                            lastend = lastbegin - 1;
                            if (lastend == -1) break;
                            lastbegin = bratDocument.getContent().substring(0, lastend - 1).lastIndexOf(' ') + 1;
                        }
                        if (lastend != -1) {
                            if (bratDocument.getParseTree().getPOS(lastbegin).startsWith("IN")
                                    &&"from with".contains(bratDocument.getContent().substring(lastbegin,lastend).toLowerCase())) {
                                begin = lastbegin;
                                end = lastend;
                                newlink.setRule_id("NT35");
                            }
                        }
                    }
                    if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                        String t = bratDocument.getContent().substring(begin,end);
                        BratEntity e = new BratEntity(bratDocument, t, "SpatialFlag", begin, end);
                        newlink.setFlag(bratDocument.getEntityList().size() - 1);
                        res.add(newlink);
                    }
                }
            }
        }


        //<trajector>from<landmark>
        //1
        if (level==1&&next1 != null) {
            if (inSegment(bratDocument, i, idx_next1) && (getSegment(bratDocument, i, idx_next1)).contains(" from ")
                    && !(getSegment(bratDocument, i, idx_next1)).contains(" and ")
                    && hasNoNV(bratDocument, i, idx_next1) && hasNoPrep(bratDocument, i, idx_next1, "from")) {
                if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(next1)) {
                    OTLINK newlink = new OTLINK(-1);
                    setLink(idx_next1, i, newlink, entityList);
                    newlink.setRule_id("NT17");
                    int begin = bratDocument.getContent().substring(trajector.getEnd(), next1.getStart()).indexOf("from") + trajector.getEnd();
                    int end = begin + "from".length();
                    if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                        BratEntity e = new BratEntity(bratDocument, "from", "SpatialFlag", begin, end);
                        newlink.setFlag(bratDocument.getEntityList().size() - 1);
                        // 记录 bratDocument.noCandidate(idx_next1);
                        res.add(newlink);
                    }
                }
            }
        }

        //<trajector>into<landmark>
        //1
        if (level==1&&next1 != null) {
            if (inSegment(bratDocument, i, idx_next1) && (getSegment(bratDocument, i, idx_next1)).contains(" into ")
                    && !(getSegment(bratDocument, i, idx_next1)).contains(" and ")
                    && countNoun_position(bratDocument,trajector.getStart(),next1.getEnd())<=2
                    && countVerb_all(bratDocument,i,idx_next1)==0
                    && hasNoPrep(bratDocument, i, idx_next1, "into")) {
                if (JudgeEntity.isEvent(trajector) && JudgeEntity.canbeLandmark(next1)) {
                    OTLINK newlink = new OTLINK(-1);
                    setLink(idx_next1, i, newlink, entityList);
                    newlink.setRule_id("NT40");
                    int begin = bratDocument.getContent().substring(trajector.getEnd(), next1.getStart()).indexOf("into") + trajector.getEnd();
                    int end = begin + "into".length();
                    if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                        BratEntity e = new BratEntity(bratDocument, "into", "SpatialFlag", begin, end);
                        newlink.setFlag(bratDocument.getEntityList().size() - 1);
                        // 记录 bratDocument.noCandidate(idx_next1);
                        res.add(newlink);
                    }
                }
            }
        }

        //<trajector>over<landmark>
        //1
        if (level==1&&next1 != null) {
            if (inSegment_true(bratDocument, i, idx_next1) && (getSegment(bratDocument, i, idx_next1)).contains(" over ")
                    && !(getSegment(bratDocument, i, idx_next1)).contains(" and ")
                    && hasNoNV(bratDocument, i, idx_next1) && hasNoPrep(bratDocument, i, idx_next1, "over")) {
                if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(next1)) {
                    OTLINK newlink = new OTLINK(-1);
                    setLink(idx_next1, i, newlink, entityList);
                    newlink.setRule_id("NT31");
                    int begin = bratDocument.getContent().substring(trajector.getEnd(), next1.getStart()).indexOf("over") + trajector.getEnd();
                    int end = begin + "over".length();
                    if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                        BratEntity e = new BratEntity(bratDocument, "over", "SpatialFlag", begin, end);
                        newlink.setFlag(bratDocument.getEntityList().size() - 1);
                        // 记录 bratDocument.noCandidate(idx_next1);
                        res.add(newlink);
                    }
                }
            }
        }

        //<trajector>of<landmark>
        //1
        if (level==1&&next1 != null) {
            if (inSegment_true(bratDocument, i, idx_next1) &&
                    (getSegment(bratDocument, i, idx_next1)).contains(" of ")
                    && !(getSegment(bratDocument, i, idx_next1)).contains(" and ")
                    && (hasNoNV(bratDocument, i, idx_next1)  && countNoun(bratDocument, i, idx_next1) == 0
                    ||entityList.get(idx_next1-1).getTag().equals(BratUtil.MEASURE)||countNoun_position(bratDocument,trajector.getStart(),next1.getEnd())==2)
                    && hasNoPrep(bratDocument, i, idx_next1, "of")
                   && !getSegment(bratDocument, i, idx_next1).contains("out of")) {
                if (!trajector.getText().equals("one") && !trajector.getText().startsWith("one ")
                        &&!"shantytowns shantytown city town banner banners cities towns section sections region regions".contains(trajector.getText().toLowerCase())) {
                    if (JudgeEntity.canbeTrajector(trajector) && (JudgeEntity.canbeLandmark(next1)||JudgeEntity.canbeTrajector(next1))) {
                        OTLINK newlink = new OTLINK(-1);
                        newlink.setRule_id("NT5");
                        if (" vicinity succession scene ".contains(" "+trajector.getText().toLowerCase()+" ")
                                ||"valley".equals(trajector.getText().toLowerCase())&&"creek".equals(next1.getText().toLowerCase())){
                            setLink(i, idx_next1, newlink, entityList);
                        } else
                            setLink(idx_next1, i, newlink, entityList);
                        int begin = bratDocument.getContent().substring(trajector.getEnd(), next1.getStart()).indexOf("of") + trajector.getEnd();
                        int end = begin + "of".length();
                        if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                            BratEntity e = new BratEntity(bratDocument, "of", "SpatialFlag", begin, end);
                            newlink.setFlag(bratDocument.getEntityList().size() - 1);
                            // 记录 bratDocument.noCandidate(idx_next1);
                            res.add(newlink);
                        }
                    }
                } else {
//                   // 记录 bratDocument.noCandidate(idx_next1);
                }
            }
        }

        //<l>to<t>
        //1
        if (level==1&&next1 != null) {
            if (inSegment(bratDocument, i, idx_next1) && (getSegment(bratDocument, i, idx_next1)).contains(" to ")
                    && !(getSegment(bratDocument, i, idx_next1)).contains(" and ")
                    && hasNoNV(bratDocument, i, idx_next1) && hasNoPrep(bratDocument, i, idx_next1, "to")
                    && getSegment(bratDocument, i, idx_next1).split(" ").length <= 3) {
                if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(next1)) {
                    OTLINK newlink = new OTLINK(-1);
                    if (JudgeEntity.isEvent(trajector))
                        setLink( idx_next1,i, newlink, entityList);
                    else
                        setLink( i,idx_next1, newlink, entityList);
                    // 记录 bratDocument.noCandidate(idx_next1);
                    newlink.setRule_id("NT18");
                    int begin = bratDocument.getContent().substring(trajector.getEnd(), next1.getStart()).indexOf("to") + trajector.getEnd();
                    int end = begin + "to".length();
                    if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                        BratEntity e = new BratEntity(bratDocument, "to", "SpatialFlag", begin, end);
                        newlink.setFlag(bratDocument.getEntityList().size() - 1);
                        res.add(newlink);
                    }
                }
            }
        }


        //<landmark>to<trajector[v]>
        //1
        //no trigger
        if (level==1&&last1 != null) {
            if (inSegment_true(bratDocument, idx_last1, i) && (getSegment(bratDocument, idx_last1, i)).equals(" to ")
                    && !(getSegment(bratDocument, idx_last1, i)).contains(" and ")) {
//                        && countNoun(bratDocument, idx_last1, i) <= 1 && countVerb(bratDocument, idx_last1, i) == 0
//                        &&hasNoPrep(bratDocument,idx_last1, i, "to")) {
                if (JudgeEntity.isEvent(trajector) && JudgeEntity.canbeLandmark(last1)) {
                    OTLINK newlink = new OTLINK(-1);
                    setLink(idx_last1, i, newlink, entityList);
//                   // 记录 bratDocument.noCandidate(idx_next1);
                    newlink.setRule_id("NT19");
                    int begin = bratDocument.getContent().substring(last1.getEnd(), trajector.getStart()).indexOf("to") + last1.getEnd();
                    int end = begin + "to".length();
                    if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                        BratEntity e = new BratEntity(bratDocument, "to", "SpatialFlag", begin, end);
                        newlink.setFlag(bratDocument.getEntityList().size() - 1);
                        res.add(newlink);
                    }
                }
            }
        }

        //<landmark>at<trajector>
        //1
        if (level==1&&next1 != null) {
            if (inSegment(bratDocument, i, idx_next1) &&
                    (getSegment(bratDocument, i, idx_next1)).contains(" at ")&& !(getSegment(bratDocument, i, idx_next1)).contains(" and ")
                    && (countNoun_position(bratDocument,trajector.getStart(),next1.getEnd())==2
                    || countNoun(bratDocument, i, idx_next1) == 0)
                    &&countVerb_all(bratDocument,i,idx_next1)==0) {
                if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(next1)) {
                    OTLINK newlink = new OTLINK(-1);
                    newlink.setRule_id("NT6");
                    setLink( i,idx_next1, newlink, entityList);
                    int begin = bratDocument.getContent().substring(trajector.getEnd(), next1.getStart()).indexOf("at") + trajector.getEnd();
                    int end = begin + "at".length();
                    if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                        BratEntity e = new BratEntity(bratDocument, "at", "SpatialFlag", begin, end);
                        newlink.setFlag(bratDocument.getEntityList().size() - 1);
                        // 记录 bratDocument.noCandidate(idx_next1);
                        res.add(newlink);
                    }
                }
            }
        }

        //<landmark>with<trajector>
        //1
        //no trigger
        if (level==1&&last1 != null) {
            if (inSegment(bratDocument, idx_last1, i) &&
                    (getSegment(bratDocument, idx_last1, i)).contains(" with ")
                    && !(getSegment(bratDocument, idx_last1, i)).contains(" and ")
                    && countNoun_position(bratDocument, last1.getStart(), trajector.getEnd()) <= 2 && countVerb_all(bratDocument, idx_last1, i) == 0 && hasNoPrep(bratDocument, idx_last1, i, "with")) {
                if (JudgeEntity.canbeLandmark(last1)) {
                    OTLINK newlink = new OTLINK(-1);
                    newlink.setRule_id("NT15");
                    setLink(idx_last1, i, newlink, entityList);
                    int begin = bratDocument.getContent().substring(last1.getEnd(), trajector.getStart()).indexOf("with") + last1.getEnd();
                    int end = begin + "with".length();
                    if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                        BratEntity e = new BratEntity(bratDocument, "with", "SpatialFlag", begin, end);
                        newlink.setFlag(bratDocument.getEntityList().size() - 1);
                        // 记录   if (!JudgeEntity.isEvent(trajector)) bratDocument.noCandidate(i);
//                   // 记录 bratDocument.noCandidate(idx_next1);
                        res.add(newlink);
                    }
                }
            }
        }

        //记录删除
        //<landmark>for<trajector>
        //1
        //no trigger
        if (level==1&&last1 != null) {
            if (inSegment_true(bratDocument, idx_last1, i) && (getSegment(bratDocument, idx_last1, i)).contains(" for ")
                    && !(getSegment(bratDocument, idx_last1, i)).contains(" and ")
                    &&countNoun_position(bratDocument,last1.getStart(),trajector.getEnd())==2 && hasNoPrep(bratDocument, idx_last1, i, "for")) {
                if (JudgeEntity.canbeLandmark(last1)) {
                    OTLINK newlink = new OTLINK(-1);
                    setLink(idx_last1, i, newlink, entityList);
                    newlink.setRule_id("NT22");
                    int begin = bratDocument.getContent().substring(last1.getEnd(), trajector.getStart()).indexOf("for") + last1.getEnd();
                    int end = begin + "for".length();
                    if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                        BratEntity e = new BratEntity(bratDocument, "for", "SpatialFlag", begin, end);
                        newlink.setFlag(bratDocument.getEntityList().size() - 1);
                        // 记录 if (!JudgeEntity.isEvent(trajector)) bratDocument.noCandidate(i);
//                   // 记录 bratDocument.noCandidate(idx_next1);
                        res.add(newlink);
                    }
                }
            }
        }

        //<trajector>on<landmark>
        //1
        if (level==1&&next1 != null) {
            if (inSegment(bratDocument, i, idx_next1) &&
                    (getSegment(bratDocument, i, idx_next1)).contains(" on ")&& !(getSegment(bratDocument, i, idx_next1)).contains(" and ")
                    && (countNoun_position(bratDocument,trajector.getStart(),next1.getEnd())<=2
                    || countNoun(bratDocument, i, idx_next1) == 0)
                    &&countVerb_all(bratDocument,i,idx_next1)==0) {
                if ((JudgeEntity.canbeTrajector(trajector)||JudgeEntity.canbeLandmark(trajector)) && JudgeEntity.canbeLandmark(next1)) {
                    OTLINK newlink = new OTLINK(-1);
                    newlink.setRule_id("NT32");
                    setLink(idx_next1, i, newlink, entityList);
                    int begin = bratDocument.getContent().substring(trajector.getEnd(), next1.getStart()).indexOf("on") + trajector.getEnd();
                    int end = begin + "on".length();
                    if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                        BratEntity e = new BratEntity(bratDocument, "on", "SpatialFlag", begin, end);
                        newlink.setFlag(bratDocument.getEntityList().size() - 1);
                        // 记录 bratDocument.noCandidate(idx_next1);
                        res.add(newlink);
                    }
                }
            }
        }

        //<trajector>near<landmark>
        //1
        if (level==1&&next1 != null) {
            if (inSegment(bratDocument, i, idx_next1) &&
                    (getSegment(bratDocument, i, idx_next1)).contains(" near ")&& !(getSegment(bratDocument, i, idx_next1)).contains(" and ")
                    && (countNoun_position(bratDocument,trajector.getStart(),next1.getEnd())<=2
                    || countNoun(bratDocument, i, idx_next1) == 0)
                    &&countVerb_all(bratDocument,i,idx_next1)==0) {
                if ((JudgeEntity.canbeTrajector(trajector)||JudgeEntity.canbeLandmark(trajector)) && JudgeEntity.canbeLandmark(next1)) {
                    OTLINK newlink = new OTLINK(-1);
                    newlink.setRule_id("NT37");
                    setLink(idx_next1, i, newlink, entityList);
                    int begin = bratDocument.getContent().substring(trajector.getEnd(), next1.getStart()).indexOf("near") + trajector.getEnd();
                    int end = begin + "near".length();
                    if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                        BratEntity e = new BratEntity(bratDocument, "near", "SpatialFlag", begin, end);
                        newlink.setFlag(bratDocument.getEntityList().size() - 1);
                        // 记录 bratDocument.noCandidate(idx_next1);
                        res.add(newlink);
                    }
                }
            }
        }



        //记录删除
        //<landmark>of<trajector[EVENT]>
        //1
        //no trigger
        if (level==1&&last1 != null) {
            if (inSegment_true(bratDocument, idx_last1, i) && getSegment(bratDocument, idx_last1, i).contains(" of ")
                    && !(getSegment(bratDocument, idx_last1, i)).contains(" and ")
                    && hasNoNV(bratDocument, idx_last1, i) && hasNoPrep(bratDocument, idx_last1, i, "of")) {
                if (JudgeEntity.canbeLandmark(last1) && JudgeEntity.isEvent(trajector)) {
                    OTLINK newlink = new OTLINK(-1);
                    newlink.setRule_id("NT8");
                    setLink(idx_last1, i, newlink, entityList);
                    int begin = bratDocument.getContent().substring(last1.getEnd(), trajector.getStart()).indexOf("of") + last1.getEnd();
                    int end = begin + "of".length();
                    if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                        BratEntity e = new BratEntity(bratDocument, "of", "SpatialFla", begin, end);
                        newlink.setFlag(bratDocument.getEntityList().size() - 1);
//                   // 记录 bratDocument.noCandidate(idx_next1);
                        res.add(newlink);
                    }
                }
            }
        }



        //<landmark>is<trajector[ved]>
        //5
        //no trigger
        if (level==1&&last1 != null) {
            if (inSegment(bratDocument, idx_last1, i)
                    && (getSegmentOrigin(bratDocument, idx_last1, i).startsWith("be ") || (getSegmentOrigin(bratDocument, idx_last1, i)+" ").contains(" be "))
                    && countNoun(bratDocument, idx_last1, i) == 0
                    && countVerb(bratDocument, idx_last1, i) == 0) {
                if (JudgeEntity.canbeLandmark(last1) && JudgeEntity.isEvent(trajector)
                        && isVerb(trajector, bratDocument) && trajector.getText().toLowerCase().endsWith("ed")) {
                    OTLINK newlink = new OTLINK(-1);
                    newlink.setRule_id("NT9");
                    setLink(idx_last1, i, newlink, entityList);

                    int begin = getPhrasePosition(getSegment(bratDocument, idx_last1, i), getSegmentOrigin(bratDocument, idx_last1, i), "be") + last1.getEnd() + 1;
                    int end = bratDocument.getContent().substring(begin).indexOf(' ') + begin;
                    if (end < begin) end = bratDocument.getContent().length();
                    if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                        String t = bratDocument.getContent().substring(begin, end);
                        BratEntity e = new BratEntity(bratDocument, t, "SpatialFlag", begin, end);
                        newlink.setFlag(bratDocument.getEntityList().size() - 1);

                        // 记录 bratDocument.noCandidate(i);
                        res.add(newlink);
                    }
                }
            }
        }

        if (level==1) {

            //NT10 NT11 NT12
            String flagbes = "rich in\tpart of\thome to";
            //<trajector>is part of<landmark>
            int idx = 0;
            for (String flagbe : flagbes.split("\t")) {

                if (next1 != null) {
                    if (inSegment(bratDocument, i, idx_next1) && getSegmentOrigin(bratDocument, i, idx_next1).contains("be " + flagbe)) {
                        if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(next1)) {
                            OTLINK newlink = new OTLINK(-1);
                            newlink.setRule_id("NT" + (10 + idx));
                            if (flagbe.equals("part of"))
                                setLink(idx_next1, i, newlink, entityList);
                            else
                                setLink(i,idx_next1,  newlink, entityList);

                            int begin = getPhrasePosition(getSegment(bratDocument, i, idx_next1), getSegmentOrigin(bratDocument, i, idx_next1), "be") + trajector.getEnd() + 1;
                            int end = bratDocument.getContent().substring(begin).indexOf(' ') + begin + flagbe.length() + 1;
                            if (!hasBuildRelation(bratDocument.getEntityList(), begin, newlink.getRule_id(), linkList)) {
                                String t = bratDocument.getContent().substring(begin, end);
                                BratEntity e = new BratEntity(bratDocument, t, "SpatialFlag", begin, end);
                                newlink.setFlag(bratDocument.getEntityList().size() - 1);

                                // 记录 bratDocument.noCandidate(idx_next1);
                                // 记录 bratDocument.noCandidate(i);
                                res.add(newlink);
                            }
                        }
                    }
                }
                idx++;
            }
        }

        if (next1 != null) {
            if (inSegment(bratDocument, i, idx_next1) && getSegmentOrigin(bratDocument, i, idx_next1).contains("turn out to be")) {
                if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark(next1)) {
                    OTLINK newlink = new OTLINK(-1);
                    newlink.setRule_id("NT34");
                    setLink(idx_next1, i, newlink, entityList);

                    int begin = getPhrasePosition(getSegment(bratDocument, i, idx_next1), getSegmentOrigin(bratDocument, i, idx_next1), "turn out to be") + trajector.getEnd() + 1;
                    int end = bratDocument.getContent().substring(begin).indexOf(' ') + begin;
                    end = bratDocument.getContent().substring(end+1).indexOf(' ') + end+1;
                    end = bratDocument.getContent().substring(end+1).indexOf(' ') + end+1;
                    end = bratDocument.getContent().substring(end+1).indexOf(' ') + end+1;
                    if (!hasBuildRelation(bratDocument.getEntityList(), begin, newlink.getRule_id(), linkList)) {
                        String t = bratDocument.getContent().substring(begin, end);
                        BratEntity e = new BratEntity(bratDocument, t, "SpatialFlag", begin, end);
                        newlink.setFlag(bratDocument.getEntityList().size() - 1);

                        // 记录 bratDocument.noCandidate(idx_next1);
                        // 记录 bratDocument.noCandidate(i);
                        res.add(newlink);
                    }
                }
            }
        }

        //记录删除
        //<trajector>is<landmark>
        //5
        //no trigger
        if (level==1&&next1 != null) {
            if (inSegment(bratDocument, i, idx_next1)
                    && (getSegmentOrigin(bratDocument, i, idx_next1).startsWith("be ") || (getSegmentOrigin(bratDocument, i, idx_next1)+" ").contains(" be "))
                    && countVerb_all_strict(bratDocument, i, idx_next1) <= 1
                    && (countNoun_position(bratDocument, trajector.getStart(), next1.getEnd()) <= 2 || countNoun(bratDocument, i, idx_next1) == 0)
                    && !getSegment(bratDocument, i, idx_next1).contains(",")
                    && !getSegment(bratDocument, i, idx_next1).contains("which")
                    &&hasNoPrep(bratDocument,i,idx_next1,"")) {
                if ((!next1.getText().equals("place")||getSegment(bratDocument, i, idx_next1).contains("a nicer")) && !next1.getText().equals("avenue")) {
                    if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeLandmark_strict(next1)) {
                        OTLINK newlink = new OTLINK(-1);
                        newlink.setRule_id("NT13");
                        if (" here it building ".contains(" "+trajector.getText().toLowerCase()+" "))
                            setLink(i, idx_next1, newlink, entityList);
                        else
                            setLink(idx_next1, i, newlink, entityList);

                        int begin = getPhrasePosition(getSegment(bratDocument, i, idx_next1), getSegmentOrigin(bratDocument, i, idx_next1), "be") + trajector.getEnd() + 1;
                        int end = bratDocument.getContent().substring(begin).indexOf(' ') + begin;

                        int lastend = trajector.getStart()-1;
                        if (lastend!=-1) {
                            int lastbegin = bratDocument.getContent().substring(0,lastend-1).lastIndexOf(' ')+1;
                            //with it s trio of art museums

                            if (bratDocument.getParseTree().getPOS(lastbegin).startsWith("IN")) {
                                begin = lastbegin;
                                end = lastend;
                            }
                        }

                        if (!hasBuildRelation(bratDocument.getEntityList(),begin, newlink.getRule_id(), linkList)) {
                            if (bratDocument.getContent().substring(begin).indexOf(' ') == -1)
                                end = bratDocument.getContent().length();
                            String t = bratDocument.getContent().substring(begin, end);
                            BratEntity e = new BratEntity(bratDocument, t, "SpatialFlag", begin, end);
                            newlink.setFlag(bratDocument.getEntityList().size() - 1);

                            // 记录 bratDocument.noCandidate(idx_next1);
                            res.add(newlink);
                        }
                    }
                }
            }
        }

        //see locate designate suffer have
        //NT24NT25NT26NT27NT38
        if (level==1) {
            int idx = 0;
            for (String flagverb : WordData.getFlagverbList()) {
                if (next1 != null) {
                    if (inSegment(bratDocument, i, idx_next1) && (" " + getSegmentOrigin(bratDocument, i, idx_next1)).contains(" " + flagverb + " ")
                            && (countVerb_all(bratDocument, i, idx_next1) <= 1|| getSegmentOrigin(bratDocument, i, idx_next1).contains("be able to"))
                            && (countNoun_position(bratDocument, trajector.getStart(), next1.getEnd()) == 2 || countNoun(bratDocument, i, idx_next1) == 0)) {
                        if (JudgeEntity.canbeTrajector(trajector) && JudgeEntity.canbeTrajector(next1)) {
                            OTLINK newlink = new OTLINK(-1);
                            if (idx!=1) {
                                setLink(i, idx_next1, newlink, entityList);
                            } else {
                                //<t>locate<l>
                                setLink( idx_next1,i, newlink, entityList);
                            }
                            // 记录 bratDocument.noCandidate(idx_next1);
                            if (idx>=4)
                                newlink.setRule_id("NT" + (34 + idx));
                            else
                                newlink.setRule_id("NT" + (24 + idx));

                            int begin = getPhrasePosition(getSegment(bratDocument, i, idx_next1), getSegmentOrigin(bratDocument, i, idx_next1), flagverb) + trajector.getEnd() + 1;
                            int end = bratDocument.getContent().substring(begin).indexOf(' ') + begin;
                            if (end < begin) end = bratDocument.getContent().length();
                            String t = bratDocument.getContent().substring(begin, end);
                            if (t.equals("seen")) newlink.setRule_id("NT36");
                            if (!hasBuildRelation(bratDocument.getEntityList(), begin, newlink.getRule_id(), linkList)) {
                                BratEntity e = new BratEntity(bratDocument, t, "SpatialFlag", begin, end);
                                newlink.setFlag(bratDocument.getEntityList().size() - 1);

                                res.add(newlink);
                            }
                        }
                    }
                }
                idx++;
            }
        }

        return res;
    }

    private static boolean hasBuildRelation(List<BratEntity> entityList, int begin, String rule_id, List<LINK> linkList) {
        for (BratEntity e:entityList){
            if (e.getStart()==begin&&e.getTag().equals("SpatialFlag")) {
                for (LINK l:linkList){
                    if (l.getRule_id().equals(rule_id)) {
                        OTLINK otl = (OTLINK) l;
                        if (entityList.get(otl.getFlag()).getStart()==begin)
                            return true;
                    }
                }
            }
        }
        return false;
    }

    private static int getPhrasePosition(String segment, String seg_origin, String phrase) {
        int pos = 0;
        int i = 0;
        if (segment.startsWith(" ")) segment = segment.substring(1);
        while (true) {
            if (seg_origin.substring(pos).startsWith(phrase)) break;
            pos = seg_origin.indexOf(' ', pos) + 1;
            if (pos == 0) break;
            i++;
        }
        pos = 0;
        while (true) {
            if (i == 0) break;
            pos = segment.indexOf(' ', pos) + 1;
            i--;
        }
        return pos;
    }

    private static int countVerb_all(BratDocumentwithList bratDocument, int idx1, int idx2) {
        int p1 = bratDocument.getEntityList().get(idx1).getEnd();
        int p2 = bratDocument.getEntityList().get(idx2).getStart();
        ParseTree t = bratDocument.getParseTree();
        int count = 0;
        for (int p = p1; p < p2; p++) {
            String POS = t.getPOS(p);
            if (POS != null && POS.startsWith("V") && !POS.equals("VBN") && !POS.equals("VBG")) count++;
            if (POS!=null&&POS.equals("VBD")&&bratDocument.getContent().substring(p,p+6).equals("capped")) count--;
        }
        return count;
    }

    private static int countVerb_all_strict(BratDocumentwithList bratDocument, int idx1, int idx2) {
        int p1 = bratDocument.getEntityList().get(idx1).getEnd();
        int p2 = bratDocument.getEntityList().get(idx2).getStart();
        ParseTree t = bratDocument.getParseTree();
        int count = 0;
        for (int p = p1; p < p2; p++) {
            String POS = t.getPOS(p);
            if (POS != null && POS.startsWith("V")) count++;
        }
        return count;
    }

    private static boolean hasNoPrep(BratDocumentwithList bratDocument, int i, int idx_next1, String except) {
        String seg = getSegmentOrigin(bratDocument, i, idx_next1);
        for (String word : WordData.getPrepList()) {
            if ((seg.equals(word) || seg.startsWith(word + " ") || seg.endsWith(" " + word) || seg.contains(" " + word + " ")) && !word.equals(except))
                return false;
        }
        return true;
    }

    private static boolean isRB(BratEntity next1) {
        return next1.getText().toLowerCase().equals("there") || next1.getText().toLowerCase().equals("here")
                || next1.getText().toLowerCase().equals("somewhere") || next1.getText().toLowerCase().equals("downstairs");
    }

    public static List<BratEvent> getTop(int k, List<BratEvent> eventList) {
        NotriggerEventComparator comparator = new NotriggerEventComparator();
        eventList.sort(comparator);
        BratEvent NT33 = null;
        for (BratEvent e:eventList){
            if (e.getRuleid().equals("NT33")){
                NT33=e;break;
            }
        }
        if (NT33!=null){
            for (BratEvent e:eventList){
                if (e.getRuleid().equals("NT35")){
                    eventList.remove(NT33);
                    break;
                }
            }
        }
        if (eventList.size() >= k)
            return eventList.subList(0, k);
        else
            return eventList;
    }

    public static List<BratEvent> findPossibleFlags(BratDocumentwithList bratDocument, List<BratEvent> eventList, int k) {
        Collections.sort(bratDocument.getEntityList(), Comparator.comparingInt(BratEntity::getStart));
        for (BratEvent e:eventList){
            for (BratEntity entity:e.getEntities().values()){
                for (int i  =0;i<bratDocument.getEntityList().size();i++){
                    if (entity.getText().equals(bratDocument.getEntityList().get(i).getText())&&entity.getStart()==bratDocument.getEntityList().get(i).getStart()){
                        bratDocument.noCandidate(i);
                    }
                }
            }
        }
        List<BratEvent> neweventList = new ArrayList<>();
        int size = bratDocument.getEntityList().size();
       for(int i = 0;i<size;i++){
           BratEntity e =bratDocument.getEntityList().get(i);
           BratEntity last = i==0?null:bratDocument.getEntityList().get(i-1);
           BratEntity next = i==bratDocument.getEntityList().size()-1?null:bratDocument.getEntityList().get(i+1);
           int nexti = i==bratDocument.getEntityList().size()-1?i:i+1;
           if (next!=null&&(next.getTag().equals(BratUtil.MEASURE)||next.getTag().equals(BratUtil.SPATIAL_SIGNAL))){
               next = i+1==bratDocument.getEntityList().size()-1?null:bratDocument.getEntityList().get(i+2);
               nexti = i+1==bratDocument.getEntityList().size()-1?i+1:i+2;
           }
           if (next!=null&&(next.getTag().equals(BratUtil.MEASURE)||next.getTag().equals(BratUtil.SPATIAL_SIGNAL))){
               next = i+2==bratDocument.getEntityList().size()-1?null:bratDocument.getEntityList().get(i+3);
               nexti = i+2==bratDocument.getEntityList().size()-1?i+2:i+3;
           }
//           if (e.getText().toLowerCase().equals("zipaquira")){
//               System.out.print("");
//           }
           char precedence = 'a';
           for (String extraflag:WordData.getFlagextraList()){
               switch (extraflag){
                   case "be":
                       break;
                   case "visit": case "stay":
                       if (bratDocument.getIsCandidate(i)&&(last!=null&&JudgeEntity.canbeMover_NotStrict(last)||next!=null&&JudgeEntity.canbeMover_NotStrict(next))&&e.getText().toLowerCase().contains(extraflag)){
                           int begin = e.getEnd() + 1;
                           int end = bratDocument.getContent().substring(begin).indexOf(' ') + begin;
                           if (end < begin) end = bratDocument.getContent().length();
                           if (next!=null&&begin==next.getStart()){
                               begin = next.getEnd()+1;
                               end = bratDocument.getContent().substring(begin).indexOf(' ') + begin;
                               if (end < begin) end = bratDocument.getContent().length();
                           }
                           BratEntity nnext = nexti==bratDocument.getEntityList().size()-1?null:bratDocument.getEntityList().get(nexti+1);
                           if (nnext!=null&&begin==nnext.getStart()){break;}
                           String t = bratDocument.getContent().substring(begin, end);
                           BratEntity newe = new BratEntity(bratDocument, t, "SpatialFlag", begin, end);
                           BratEvent event = new BratEvent();
                           event.addRole("flag", newe.getId());
                           event.setRuleid("EX" + precedence);
                           event.addEntity(newe);
                           neweventList.add(event);
                           event.addRole("trajector",e.getId());
                           event.addEntity(e);
                       }
                       break;
                   case "MEASURE":
                       if (bratDocument.getIsCandidate(i)&&next!=null&&last!=null&&e.getTag().equals(BratUtil.MEASURE)&&JudgeEntity.canbeMover_NotStrict(last)&&JudgeEntity.canbeMover_NotStrict(next)){
                           BratEvent event = new BratEvent();
                           event.addRole("flag",e.getId());
                           event.setRuleid("EX"+precedence);
                           event.addEntity(e);
                           neweventList.add(event);
                        }
                       break;
                   case "there": case "here": case "elsewhere": case "everywhere": case "upstairs": case "downstairs":
                       if (bratDocument.getIsCandidate(i)&&e.getText().toLowerCase().equals(extraflag)){
                               //flag是下一个
                               int begin = e.getEnd() + 1;
                               int end = bratDocument.getContent().substring(begin).indexOf(' ') + begin;
                               if (end < begin) end = bratDocument.getContent().length();
                               String t = bratDocument.getContent().substring(begin, end);
                               BratEntity newe = new BratEntity(bratDocument, t, "SpatialFlag", begin, end);
//                               bratDocument.getEntityList().add(newe);
//                               bratDocument.getIsCandidate().add(false);
                               BratEvent event = new BratEvent();
                               event.addRole("flag", newe.getId());
                               event.setRuleid("EX" + precedence);
                               event.addEntity(newe);
                               event.addRole("landmark",e.getId());
                               event.addEntity(e);
                               neweventList.add(event);
                       }
                       break;
                   case ",": case "，":
                       if (next!=null&&JudgeEntity.canbeMover_NotStrict(e)&&JudgeEntity.canbeMover_NotStrict(next)&&getSegment(bratDocument,i,nexti).contains(extraflag)){
                           if (countWordDist(bratDocument,i,nexti)<=2){
                               int begin = getSegment(bratDocument,i,nexti).indexOf(extraflag) + e.getEnd();
                               int end = begin + extraflag.length();
                               BratEntity newe = new BratEntity(bratDocument, extraflag, "SpatialFlag", begin, end);
//                               bratDocument.getEntityList().add(newe);
//                               bratDocument.getIsCandidate().add(false);
                               BratEvent event = new BratEvent();
                               event.addRole("flag",newe.getId());
                               event.setRuleid("EX"+precedence);
                               event.addEntity(newe);
                               neweventList.add(event);
                           }
                       }
                       break;
                   case "(":
                       if (next!=null&&JudgeEntity.canbeMover_NotStrict(e)&&JudgeEntity.canbeMover_NotStrict(next)&&getSegment(bratDocument,i,nexti).contains("(")&&!getSegment(bratDocument,i,nexti).contains(")")){
                           int begin = getSegment(bratDocument,i,nexti).indexOf(extraflag) + e.getEnd();
                           int end = begin + extraflag.length();
                           BratEntity newe = new BratEntity(bratDocument, extraflag, "SpatialFlag", begin, end);
//                           bratDocument.getEntityList().add(newe);
//                           bratDocument.getIsCandidate().add(false);
                           BratEvent event = new BratEvent();
                           event.addRole("flag",newe.getId());
                           event.setRuleid("EX"+precedence);
                           event.addEntity(newe);
                           neweventList.add(event);
                       }
                       break;
                   default:
                       if (i==0){
                           if (JudgeEntity.canbeMover_NotStrict(e)&&bratDocument.getContent().substring(0,e.getStart()).toLowerCase().contains(" "+extraflag+" ")){
                               int begin = bratDocument.getContent().substring(0,e.getStart()).toLowerCase().indexOf(extraflag);
                               int end = begin + extraflag.length();
                               BratEntity newe = new BratEntity(bratDocument, extraflag, "SpatialFlag", begin, end);
                               BratEvent event = new BratEvent();
                               event.addRole("flag",newe.getId());
                               event.setRuleid("EX"+precedence);
                               event.addEntity(newe);
                               neweventList.add(event);
                           }
                       }
                       if (next!=null&&JudgeEntity.canbeMover_NotStrict(e)&&JudgeEntity.canbeMover_NotStrict(next)&&getSegment(bratDocument,i,nexti).contains(" "+extraflag+" ")){
                           int begin = getSegment(bratDocument,i,nexti).indexOf(extraflag) + e.getEnd();
                           int end = begin + extraflag.length();
                           BratEntity newe = new BratEntity(bratDocument, extraflag, "SpatialFlag", begin, end);
//                           bratDocument.getEntityList().add(newe);
//                           bratDocument.getIsCandidate().add(false);
                           BratEvent event = new BratEvent();
                           event.addRole("flag",newe.getId());
                           event.setRuleid("EX"+precedence);
                           event.addEntity(newe);
                           neweventList.add(event);
                       }
               }
               precedence++;
           }
       }

//       if (neweventList.size()<=k){
//           eventList.addAll(neweventList);
//       } else {
//           Collections.sort(neweventList, Comparator.comparing(BratEvent::getRuleid));
//           eventList.addAll(neweventList.subList(0,k));
//       }

        eventList.addAll(neweventList);
        return eventList;
    }

    private static int countWordDist(BratDocumentwithList bratDocument, int i1, int i2) {
        String seg = getSegment(bratDocument,i1,i2);
        int count = 0;
        for (char c:seg.toCharArray()){
            if (c==' ') count++;
        }
        return count-1;
    }

    public static List<BratEvent> randomFlags(BratDocumentwithList bratDocument, List<BratEvent> eventList, int k) {
        Collections.sort(bratDocument.getEntityList(), Comparator.comparingInt(BratEntity::getStart));
        List<BratEvent> neweventList = new ArrayList<>();
        while (true) {
            for (int i = 0; i < bratDocument.getEntityList().size() - 1; i++) {
                if (!JudgeEntity.canbeLandmark(bratDocument.getEntityList().get(i))) continue;
                int j = i + 1;
                while (true) {
                    if (JudgeEntity.canbeLandmark(bratDocument.getEntityList().get(j))) break;
                    j++;
                    if (j == bratDocument.getEntityList().size()) break;
                }
                if (j == bratDocument.getEntityList().size()) break;
                int d = countWordDist(bratDocument, i, j);
                int randomflagidx = (int) (Math.random() * (d - 1));
                int pos = getPosbyIdxinSeg(bratDocument, i, j, randomflagidx) + bratDocument.getEntityList().get(i).getEnd();
                boolean flag = false;
                for (int j2 = i + 1; j2 < j; j2++)
                    if (bratDocument.getEntityList().get(j2).getStart() == pos) {
                        flag = true;
                    }
                if (flag) continue;
                int begin = pos;
                int end = bratDocument.getContent().substring(begin).indexOf(' ') + begin;
                if (end < begin) end = bratDocument.getContent().length();
                String t = bratDocument.getContent().substring(begin, end);
                BratEntity newe = new BratEntity(bratDocument, t, "SpatialFlag", begin, end);
                bratDocument.getEntityList().add(newe);
                BratEvent event = new BratEvent();
                event.addRole("flag", newe.getId());
                event.setRuleid("RD");
                event.addEntity(newe);
                neweventList.add(event);
                if (neweventList.size() == k) break;
            }
            if (neweventList.size() == k) break;
        }
        eventList.addAll(neweventList);
        return eventList;
    }

    private static int getPosbyIdxinSeg(BratDocumentwithList bratDocument, int i, int j, int idx) {
        String seg = getSegment(bratDocument,i,j);
        int count =-1;
        int pos = 0;
        for (char c:seg.toCharArray()){
            if (c==' ') {
                count++;
                if (count==idx) return pos+1;
            }
            pos++;
        }
        return -1;
    }

    public static class NotriggerEventComparator implements Comparator<BratEvent> {
        @Override
        public int compare(BratEvent e1, BratEvent e2) {
            Integer w1 = WordData.getNoTriggerRuleWeight(e1.getRuleid());
            Integer w2 = WordData.getNoTriggerRuleWeight(e2.getRuleid());
            if (w1==w2&&e1.getRuleid().equals("NT5")){
                String role2 = e2.getRoleId("trajector");
                BratEntity entity2 = e2.getEntities().get(role2);
                String role1 = e1.getRoleId("trajector");
                BratEntity entity1 = e1.getEntities().get(role1);
                if (entity2.getText().toLowerCase().equals("top")) return 1;
                if (entity1.getText().toLowerCase().equals("top")) return -1;
            }
            return w2.compareTo(w1);
        }
    }

    private static int getNext_ignore(int index, BratDocumentwithList bratDocument) {
        if (index == -1) return -1;
        int i = index + 1;
        while (i < bratDocument.getEntityList().size()) {
            if (bratDocument.getIsCandidate(i) && !bratDocument.getEntityList().get(i).getTag().equals(BratUtil.MEASURE)
                    && !bratDocument.getEntityList().get(i).getTag().equals(BratUtil.SPATIAL_SIGNAL))
                break;
            i++;
        }
        if (i >= bratDocument.getEntityList().size()) return -1;
        else return i;
    }

    private static int getLast_ignore(int index, BratDocumentwithList bratDocument) {
        if (index == -1) return -1;
        int i = index - 1;
        while (i >= 0) {
            if (bratDocument.getIsCandidate(i) && !bratDocument.getEntityList().get(i).getTag().equals(BratUtil.MEASURE)
                    && !bratDocument.getEntityList().get(i).getTag().equals(BratUtil.SPATIAL_SIGNAL))
                break;
            i--;
        }
        if (i < 0) return -1;
        else return i;
    }
}
