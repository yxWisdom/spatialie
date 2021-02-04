package edu.nju.ws.spatialie.spaceeval;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.sun.org.apache.bcel.internal.generic.LAND;
import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.utils.CollectionUtils;
import edu.nju.ws.spatialie.utils.FileUtil;
import org.apache.xpath.operations.Bool;

import java.io.File;
import java.util.*;

import static edu.nju.ws.spatialie.spaceeval.SpaceEvalUtils.*;

public class EvaluateXML {
    public static String filename;

    private static String mode = "part";
    private static boolean strict = true;
    private static boolean needTrigger = false;

    final static List<String> qsLinkRoles = new ArrayList<>(Arrays.asList("trigger", "trajector", "landmark"));
    final static List<String> oLinkRoles = new ArrayList<>(Arrays.asList("trigger", "trajector", "landmark"));
    final static List<String> moveLinkCoreRoles = new ArrayList<>(Arrays.asList("trigger", "mover"));
//    final static List<String> moveLinkAllRoles = new ArrayList<>(Arrays.asList(TRIGGER, MOVER, MID_POINT, SOURCE, GOAL, MOTION_SIGNAL_ID, GROUND, PATH_ID));
    final static List<String> moveLinkAllRoles = new ArrayList<>(Arrays.asList(TRIGGER, MOVER, SOURCE, GOAL, GROUND, MID_POINT, PATH_ID, MOTION_SIGNAL_ID));

    static public void evaluate(String predDir,String goldDir){
        List<File> files = FileUtil.listFiles(goldDir);
        Map<String,Integer> gold=new LinkedHashMap<>();
        Map<String,Integer> pred=new LinkedHashMap<>();
        Map<String,Integer> hit=new LinkedHashMap<>();
        for (File file : files){
//            if (file.getName().equals("SanDiego.xml")) {
//                System.out.println(1);
//            }
//            file=new File("data\\SpaceEval2015\\raw_data\\gold++\\CP\\48_N_27_E.xml");
//            System.out.println(file.getName());

            if (file.getAbsolutePath().endsWith("RideForClimateUSA.xml")) {
                System.out.println();
            }
            SpaceEvalDoc goldDoc=new SpaceEvalDoc(file.getAbsolutePath());
            filename=file.getAbsolutePath();
            SpaceEvalDoc predDoc=new SpaceEvalDoc(predDir+File.separator+file.getName());



            Map<String, List<String>> goldLinkMap = getLinkMap(goldDoc);
            Map<String, List<String>> predLinkMap = getLinkMap(predDoc);

            for (String linkType: goldLinkMap.keySet()) {

                Set<String> goldLinks = new HashSet<>(goldLinkMap.get(linkType));
                Set<String> predLinks = new HashSet<>(predLinkMap.getOrDefault(linkType, new ArrayList<>()));
                List<String> hitLinks = CollectionUtils.intersect(predLinks, goldLinks);

//                Multiset<String> goldLinksSet = HashMultiset.create(goldLinks);
//                Multiset<String> preLinksSet = HashMultiset.create(predLinks);
//
//                List<String> hitLinks = new ArrayList<>(Multisets.intersection(goldLinksSet, preLinksSet));


//                List<String> hitLinks = CollectionUtils.intersect(predLinks, goldLinks);

                if (linkType.equals(MOVELINK)) {
                    if (filename.endsWith("RideForClimateUSA.xml")) {
                        List<String> preLinkList = new ArrayList<>(hitLinks);

                        Collections.sort(preLinkList);
                        System.out.println(filename + " " + preLinkList.size());
                        for (String predLink : preLinkList) {
                            System.out.println(predLink);
                        }
                        System.out.println();
                    }

                }

                int goldNum = gold.getOrDefault(linkType, 0);
                int predNum = pred.getOrDefault(linkType, 0);
                int hitNum = hit.getOrDefault(linkType, 0);

                goldNum += goldLinks.size();
                predNum += predLinks.size();
                hitNum += hitLinks.size();

                gold.put(linkType, goldNum);
                pred.put(linkType, predNum);
                hit.put(linkType, hitNum);

//                System.out.println(linkType + " " + predLinks.size() + " " + goldLinks.size());
            }

//            System.out.println(file.getName()+" finished");
//            System.out.println();
        }
        gold.put("Overall", gold.get(QSLINK) + gold.get(OLINK) +gold.get(MOVELINK));
        pred.put("Overall", pred.get(QSLINK) + pred.get(OLINK) +pred.get(MOVELINK));
        hit.put("Overall", hit.get(QSLINK) + hit.get(OLINK) +hit.get(MOVELINK));
        for (String type:gold.keySet()){
            int hitNum=hit.get(type);
            int goldNum=gold.get(type);
            int predNum=pred.get(type);
            Double precision = 1.0*hitNum/predNum;
            Double recall = 1.0*hitNum/goldNum;
            Double f1 = 2 * precision * recall /(precision + recall);
            System.out.printf("%s\t%d\t%d\t%d\t%.4f\t%.4f\t%.4f\n", type, hitNum, predNum, goldNum, precision, recall, f1);
        }
    }



    private static Map<String, List<String>> getLinkMap(SpaceEvalDoc doc) {
        Map<String, List<String>> linkMap = new HashMap<>();
        linkMap.put("NoTrigger", new ArrayList<>());
        List<BratEvent> links=doc.getAllLinks();
        for (BratEvent link: links) {
            String linkType = link.getType();
            if (linkType.equals(MEASURELINK)) continue;
            linkMap.putIfAbsent(linkType, new ArrayList<>());
            String linkString = "";
            if (linkType.equals(QSLINK) || linkType.equals(OLINK)) {
//                if (mode2.equals("strict") && !(link.hasRole(TRAJECTOR) && link.hasRole(LANDMARK) && link.hasRole(TRIGGER))) {
//                    continue;
//                }
                linkString = getQSLinkAndOLinkString(link);
            } else if (linkType.equals(MOVELINK)) {
//                if (mode2.equals("strict") && !(link.hasRole(MOVER) && link.hasRole(TRIGGER))) {
//                    continue;
//                }
                linkString = getMoveLinkString(link);
            }
            if (linkString == null) continue;

            linkMap.get(linkType).add(linkString);
            if (linkString.endsWith("NoTrigger")) {
                linkMap.get("NoTrigger").add(linkString);
            }
        }
        return linkMap;
    }

    private static String getQSLinkAndOLinkString(BratEvent link) {
        String linkType = link.getType();

        if (strict && (!link.hasRole(TRAJECTOR) || !link.hasRole(LANDMARK))) return null;
        if (needTrigger && !link.hasRole(TRIGGER)) return null;


        StringBuilder str = new StringBuilder(linkType).append("\t");
        for (String roleType: qsLinkRoles) {
            String role = link.hasRole(roleType) ? link.getRoleId(roleType): "Null";
            str.append(role).append("\t");
        }
        if (!link.hasRole(TRIGGER)) {
            str.append("NoTrigger");
        }
        return str.toString();
    }

    private static String getMoveLinkString(BratEvent link) {
        String linkType = link.getType();

        if (strict && (!link.hasRole(MOVER) || !link.hasRole(TRIGGER))) return null;

        StringBuilder str = new StringBuilder(linkType).append("\t");
        List<String> moveLinkRoles = mode.equals("part") ? moveLinkCoreRoles : moveLinkAllRoles;
        for (String roleType: moveLinkRoles) {
            List<String> roles = new ArrayList<>(link.getRoleIds(roleType));
            Collections.sort(roles);
            String roleString = String.join(";", roles);
            str.append(roleString).append("\t");
        }
        return str.toString();
    }

    private static void sieve_result() {
        System.out.println("Sieve result:");
        String goldPath = "data\\SpaceEval2015\\raw_data\\gold++";
        String sievePath = "D:\\项目\\军事问答\\project\\SpatialRelEx\\src\\output";
//        String sieve2Path = "F:\\垃圾场\\SpatialRelEx\\output";
        strict = true;
        needTrigger = false;
        mode = "full";
        evaluate(sievePath, goldPath);
        System.out.println();
//        evaluate(sieve2Path, goldPath);
        System.out.println();
        mode = "part";
        needTrigger = true;
        evaluate(sievePath, goldPath);
        System.out.println();
//        evaluate(sieve2Path, goldPath);
    }

    private static void mhs_result() {
        String goldPath = "data\\SpaceEval2015\\raw_data\\gold++";
        String fullPath1 = "D:\\项目\\空间关系识别\\repo\\spatialie\\data\\SpaceEval2015\\predict_result\\MHS\\configuration3\\full\\XML";
//        String fullPath2 = "D:\\项目\\空间关系识别\\repo\\spatialie\\data\\SpaceEval2015\\predict_result\\MHS\\configuration3\\full\\XML_v2";
//        String fullPath3 = "D:\\项目\\空间关系识别\\repo\\spatialie\\data\\SpaceEval2015\\predict_result\\MHS\\configuration3\\full\\XML_v3";
//        String partPath1 = "D:\\项目\\空间关系识别\\repo\\spatialie\\data\\SpaceEval2015\\predict_result\\MHS\\configuration3\\part\\XML";
//        String partPath2 = "D:\\项目\\空间关系识别\\repo\\spatialie\\data\\SpaceEval2015\\predict_result\\MHS\\configuration3\\part\\XML_v2";
        strict = true;
        mode = "full";
        evaluate(fullPath1, goldPath);
        System.out.println();
        mode= "part";
        needTrigger = true;
        evaluate(fullPath1, goldPath);
    }

    private static void srl_result() {
        System.out.println("SeqTag result: ");
        String  goldPath= "data\\SpaceEval2015\\raw_data\\gold++";
        String  predPath = "D:\\项目\\空间关系识别\\repo\\spatialie\\data\\SpaceEval2015\\predict_result\\SpRL\\configuration3\\full\\XML";
        strict = true;
        mode = "full";
        needTrigger = false;
        evaluate(predPath, goldPath);
        System.out.println();
        mode = "part";
        needTrigger = true;
        evaluate(predPath, goldPath);
    }

    public static void main(String [] args) {
//        sieve_result();
//        srl_result();
        mhs_result();
//        srl_result();
//        String goldPath = "data\\SpaceEval2015\\raw_data\\gold++";
//        String sievePath = "D:\\项目\\军事问答\\project\\SpatialRelEx\\src\\output";
//        String SRLPath = "D:\\项目\\空间关系识别\\repo\\spatialie\\data\\SpaceEval2015\\predict_result\\SpRL\\configuration3\\full\\XML";
//        String MHSPath_full = "D:\\项目\\空间关系识别\\repo\\spatialie\\data\\SpaceEval2015\\predict_result\\MHS\\configuration3\\full\\XML";
//        String MHSPath_part = "D:\\项目\\空间关系识别\\repo\\spatialie\\data\\SpaceEval2015\\predict_result\\MHS\\configuration3\\part\\XML";
//
//        strict = true;
//        mode = "full";
//        System.out.println("Sieve");
//        evaluate(sievePath, goldPath);
//        System.out.println("SRL");
//        evaluate(SRLPath, goldPath);
//        System.out.println("Ours");
//        evaluate(MHSPath_full, goldPath);
//        System.out.println();
//        mode = "part";
//        System.out.println("Sieve");
//        evaluate(sievePath, goldPath);
//        System.out.println("SRL");
//        evaluate(SRLPath, goldPath);
//        System.out.println("Ours");
//        evaluate(MHSPath_part, goldPath);
//        System.out.println();
//        evaluate(MHSPath_full, goldPath);
    }
}
