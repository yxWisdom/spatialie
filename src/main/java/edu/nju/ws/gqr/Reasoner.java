package edu.nju.ws.gqr;

import edu.nju.ws.spatialie.data.BratDocument;
import edu.nju.ws.spatialie.data.BratEntity;
import edu.nju.ws.spatialie.data.BratEvent;
import edu.nju.ws.spatialie.data.BratRelation;
import edu.nju.ws.spatialie.utils.FileUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

//class ReasonExample {
//    public int sourceId;
//    public int targetId;
//    public Set<String> topologies = new HashSet<>();
//    public Set<String> directions = new HashSet<>();
//    String tToString() {
//        return String.format("%d %d ( %s )", sourceId, targetId, String.join(" ", topologies));
//    }
//    String dToString() {
//        return String.format("%d %d ( %s )", sourceId, targetId, String.join(" ", directions));
//    }
//}


public class Reasoner {



    public static Map<String, String> loadConvertTable(String path) {
        List<String> lines = FileUtil.readLines(path);
        Map<String, String> table = new HashMap<>();
        for (String line: lines) {
            if (line.trim().length() == 0)
                continue;
            String [] pair = line.split("::");
            table.put(pair[0].trim(), pair[1].trim());
        }
        return table;
    }

    public static Pair<Pair<Integer, Integer>, Set<String>> parseLine(String line) {
        Scanner scanner = new Scanner(line);
        int a = scanner.nextInt();
        int b = scanner.nextInt();

        String s = scanner.nextLine().trim();
        s = s.substring(1, s.length()-1).trim();
        String [] relations = s.split("\\s");
        return new ImmutablePair<>(new ImmutablePair<>(a, b), new HashSet<>(Arrays.asList(relations)));
    }

    public static  Set<String> converse(Set<String> set, Map<String, String> converseTable) {
        return set.stream().map(converseTable::get).collect(Collectors.toSet());
    }


    public static void extractGoldResult(String baseDir, String mode) throws CloneNotSupportedException {

        File folder = new File(baseDir);
        List<File> files = new ArrayList<>(Arrays.asList(Objects.requireNonNull(folder.listFiles())));

        for (int j=0; j<files.size();j+=2) {
            String textPath = files.get(j + 1).getPath(), annPath = files.get(j).getPath();
            String baseName = textPath.substring(textPath.lastIndexOf("\\") + 1, textPath.lastIndexOf("."));
            BratDocument bratDocument = new BratDocument(textPath, annPath);
            Map<String,BratRelation> relationMap = bratDocument.getRelationMap();
            Map<String, BratEntity> entityMap = bratDocument.getEntityMap();
            Map<String, BratEvent> eventMap = bratDocument.getEventMap();


            Map<Pair<String, String>, Set<String>> directionMap = new HashMap<>();
            Map<Pair<String, String>, Set<String>> topologyMap = new HashMap<>();

            if (mode.equals("gold")) {
                for (BratRelation r: relationMap.values()) {
                    if (r.getTag().equals("coreference")) {
                        if (r.getTargetId().startsWith("E")) continue;
                        String e1 = entityMap.get(r.getTargetId()).getText();
                        String e2 = entityMap.get(r.getSourceId()).getText();
                        Pair<String, String > key = new ImmutablePair<>(e1, e2);
                        directionMap.putIfAbsent(key, new HashSet<>());
                        directionMap.get(key).add("Eq");
                        topologyMap.putIfAbsent(key, new HashSet<>());
                        topologyMap.get(key).add("EQ");
                    }
                }
            }


            Set<String> topologies = new HashSet<>(Arrays.asList("EQ", "EC", "DC", "PO", "TPP", "NTPP"));
            Set<String> directions = new HashSet<>(Arrays.asList("E","W","N","S", "SE", "SW", "NE", "NW"));
            for (BratEvent e: eventMap.values()) {
                List<BratEvent> events = e.parse();
                for (BratEvent event: events) {


                    if (!event.getType().equals("TLINK") && !event.getType().equals("OLINK")) continue;
                    if (event.getRoleId("landmark").equals("")) {
                        System.out.println(1);
                    }
                    if (event.getRoleId("trajector").equals("")) {
                        System.out.println(1);
                    }

                    String e1 = entityMap.get(event.getRoleId("trajector")).getText();
                    String e2 = entityMap.get(event.getRoleId("landmark")).getText();
                    Pair<String, String > key = new ImmutablePair<>(e1, e2);
                    if (event.getType().equals("TLINK")) {
                        String type = event.getAttribute("QS_type");
                        if (type.equals("IN"))
                            type = "NTPP";
                        if (!topologies.contains(type))
                            continue;
                        topologyMap.putIfAbsent(key, new HashSet<>());
                        topologyMap.get(key).add(type);
                    }
                    else if(event.getType().equals("OLINK")) {
                        String type = event.getAttribute("O_type");
                        if (!directions.contains(type))
                            continue;
                        directionMap.putIfAbsent(key, new HashSet<>());
                        directionMap.get(key).add(type);
                    }
                }
            }
            List<String> directionEntityNames = directionMap.keySet().stream()
                    .map(k -> new String [] {k.getLeft(), k.getRight()}).flatMap(Arrays::stream)
                    .distinct().sorted().collect(Collectors.toList());

            Map<String, Integer> directionEntityNamesToId = new HashMap<>();
            List<String> directionIdLines = new ArrayList<>();

            for (int i = 0; i < directionEntityNames.size(); i++) {
                directionEntityNamesToId.put(directionEntityNames.get(i), i);
                directionIdLines.add(i + " " + directionEntityNames.get(i));
            }


            List<String> directionLines = directionMap.entrySet().stream()
                    .map(e -> {
                        int id1 = directionEntityNamesToId.get(e.getKey().getLeft());
                        int id2 = directionEntityNamesToId.get(e.getKey().getRight());
                        return String.format("%d %d ( %s )", id1, id2, String.join(" ", e.getValue()));
                    }).collect(Collectors.toList());

            directionLines.add(0, String.format("%d # No.%s", directionEntityNames.size()-1, baseName));
            directionLines.add(".");


            FileUtil.writeFile("data/Reason/direction/" + mode +  "/"+ baseName + ".map", directionIdLines);
            FileUtil.writeFile("data/Reason/direction/" + mode +  "/"+ baseName + ".csp", directionLines);


            List<String> topologyEntityNames =  topologyMap.keySet().stream()
                    .map(k -> new String [] {k.getLeft(), k.getRight()}).flatMap(Arrays::stream)
                    .distinct().sorted().collect(Collectors.toList());

            Map<String, Integer> topologyEntityNamesToId = new HashMap<>();
            List<String> topologyIdLines = new ArrayList<>();

            for (int i = 0; i < topologyEntityNames.size(); i++) {
                topologyEntityNamesToId.put(topologyEntityNames.get(i), i);
                topologyIdLines.add(i + " " + topologyEntityNames.get(i));
            }

            List<String> topologyLines = topologyMap.entrySet().stream()
                    .map(e -> {
                        int id1 = topologyEntityNamesToId.get(e.getKey().getLeft());
                        int id2 = topologyEntityNamesToId.get(e.getKey().getRight());
                        return String.format("%d %d ( %s )", id1, id2, String.join(" ", e.getValue()));
                    }).collect(Collectors.toList());
            topologyLines.add(0, String.format("%d # No.%s", topologyEntityNames.size()-1, baseName));
            topologyLines.add(".");

            FileUtil.writeFile("data/Reason/topology/" + mode +  "/" + baseName + ".map", topologyIdLines);
            FileUtil.writeFile("data/Reason/topology/" + mode +  "/"+ baseName + ".csp", topologyLines);
        }
    }


    public static void reason(String baseDir, String calculus) {
        File folder = new File(baseDir);
        List<File> files = new ArrayList<>(Arrays.asList(Objects.requireNonNull(folder.listFiles())));

        Map<String, String> ConvertTable;
        if (calculus.endsWith("rcc8")) {
            ConvertTable = loadConvertTable("resource/gqr/data/rcc8/calculus/rcc8.conv");
        } else {
            ConvertTable = loadConvertTable("resource/gqr/data/cd/calculus/cd.conv");
        }

        for (int i=0; i<files.size();i+=1) {
            String filepath = files.get(i).getPath();
            String baseName = filepath.substring(filepath.lastIndexOf("\\")+1, filepath.lastIndexOf("."));
            String outPath = baseDir + "/" + baseName + ".rst";
            String premisePath = baseDir + "/" + baseName + ".csp";
            if (!filepath.endsWith("csp"))
                continue;

            List<String> premiseLines = FileUtil.readLines(premisePath);
            premiseLines = premiseLines.subList(1, premiseLines.size()-1).stream().map(Reasoner::parseLine).map(p -> {
                int a = p.getLeft().getLeft(), b = p.getLeft().getRight();
                if (a > b )
                    return new ImmutablePair<>(new ImmutablePair<>(b, a), converse(p.getRight(), ConvertTable));
                else return p;
            }).map(p -> {
                int a = p.getLeft().getLeft(), b = p.getLeft().getRight();
                return String.format("%d %d ( %s )", a, b, String.join(" ", p.getRight()));
            }).collect(Collectors.toList());

            PathConsistency pathConsistency = new PathConsistency(calculus, false, false, false);
            List<String> outLines  = pathConsistency.getReasoningResult(filepath);
            outLines.removeAll(premiseLines);
//            outLines = outLines.stream().filter(o -> {
//                Pair<Pair<Integer, Integer>, Set<String>> p = parseLine(o);
//                return p.getValue().size() == 1;
//            }).collect(Collectors.toList());
            FileUtil.writeFile(outPath, outLines);

        }
    }

    static void evaluation() {
        File dGoldFolder = new File("data/Reason/direction/gold");
        File dTestFolder = new File("data/Reason/direction/test");
        List<String> dGoldFiles = new ArrayList<>(Arrays.asList(Objects.requireNonNull(dGoldFolder.listFiles()))).stream()
                .map(File::getPath).filter(f -> f.endsWith(".rst")).collect(Collectors.toList());
        List<String> dTestFiles = new ArrayList<>(Arrays.asList(Objects.requireNonNull(dTestFolder.listFiles()))).stream()
                .map(File::getPath).filter(f -> f.endsWith(".rst")).collect(Collectors.toList());
        int d_correct=0, d_gold=0, d_test=0;

        for (int i = 0; i < dGoldFiles.size();i++) {
            List<String> dGoldLines = FileUtil.readLines(dGoldFiles.get(i))
                    .stream().filter(l -> l.trim().length()!=0).collect(Collectors.toList());
            List<String> dTestLines = FileUtil.readLines(dTestFiles.get(i))
                    .stream().filter(l -> l.trim().length()!=0).collect(Collectors.toList());
            d_gold += dGoldLines.size();
            d_test += dTestLines.size();

            dTestLines.retainAll(dGoldLines);
            d_correct +=dTestLines.size();
            if (dTestLines.size() != dGoldLines.size())
                System.out.println("direction" + dGoldFiles.get(i));
        }

        float d_p = d_correct * 1.0f / d_test;
        float d_r = d_correct * 1.0f / d_gold;
        float d_f1 = 2 * d_p * d_r / (d_p + d_r);


        File tGoldFolder = new File("data/Reason/topology/gold");
        File tTestFolder = new File("data/Reason/topology/test");
        List<String> tGoldFiles = new ArrayList<>(Arrays.asList(Objects.requireNonNull(tGoldFolder.listFiles()))).stream()
                .map(File::getPath).filter(f -> f.endsWith(".rst")).collect(Collectors.toList());
        List<String> tTestFiles = new ArrayList<>(Arrays.asList(Objects.requireNonNull(tTestFolder.listFiles()))).stream()
                .map(File::getPath).filter(f -> f.endsWith(".rst")).collect(Collectors.toList());
        int t_correct=0, t_gold=0, t_test=0;
        for (int i = 0; i < tGoldFiles.size();i++) {
            List<String> tGoldLines = FileUtil.readLines(tGoldFiles.get(i)).stream()
                    .filter(l -> !l.trim().equals("")).collect(Collectors.toList());
            List<String> tTestLines = FileUtil.readLines(tTestFiles.get(i)).stream()
                    .filter(l -> !l.trim().equals("")).collect(Collectors.toList());
            t_gold += tGoldLines.size();
            t_test += tTestLines.size();
            tTestLines.retainAll(tGoldLines);
            t_correct +=tTestLines.size();
            if (tTestLines.size() != tGoldLines.size())
                System.out.println("topology" + tGoldFiles.get(i));
        }


        float t_p = t_correct * 1.0f / t_test;
        float t_r = t_correct * 1.0f / t_gold;
        float t_f1 = 2 * t_p * t_r / (t_p + t_r);


        int correct=t_correct + d_correct;
        int gold=t_gold + d_gold;
        int test = t_test + d_test;

        float p = correct * 1.0f / test;
        float r = correct * 1.0f / gold;
        float f1 = 2 * p * r / (p + r);

        System.out.println(String.format("direction correct:%d test:%d gold:%d p:%f r:%f f-1:%f",
                d_correct,d_test,d_gold,d_p,d_r,d_f1));
        System.out.println(String.format("topology correct:%d test:%d gold:%d p:%f r:%f f-1:%f",
                t_correct,t_test,t_gold,t_p,t_r,t_f1));
        System.out.println(String.format("total correct:%d test:%d gold:%d p:%f r:%f f-1:%f",
                correct,test,gold,p,r,f1));

    }

    public static void statistics(String baseDir) {
        File folder = new File(baseDir);
        List<File> files = new ArrayList<>(Arrays.asList(Objects.requireNonNull(folder.listFiles())));

        int mlink = 0, tlink = 0, word = 0;

        for (int j=0; j<files.size();j+=2) {
            String textPath = files.get(j + 1).getPath(), annPath = files.get(j).getPath();
            String baseName = textPath.substring(textPath.lastIndexOf("\\") + 1, textPath.lastIndexOf("."));
            BratDocument bratDocument = new BratDocument(textPath, annPath);
            word += bratDocument.getContent().length();
            Map<String, BratEvent> eventMap = bratDocument.getEventMap();
            for (BratEvent event: eventMap.values()) {
                if (event.getType().equals("OLINK")) {
                    mlink ++;
                }
                if (event.getType().equals("TLINK"))
                    tlink ++;
            }
        }
        System.out.println(String.format("Olnk:%d tlink:%d all:%d word:%d", mlink,tlink,mlink + tlink, word));
    }

    public static void main(String [] args) throws CloneNotSupportedException {
        Reasoner.extractGoldResult("data/Reason/annotation/reason", "gold");
        Reasoner.extractGoldResult("data/Reason/annotation/reason", "test");
        reason("data/Reason/direction/gold", "cd");
        reason("data/Reason/topology/gold", "rcc8");
        reason("data/Reason/direction/test", "cd");
        reason("data/Reason/topology/test", "rcc8");
        evaluation();
        statistics("data/Reason/annotation/reason");
    }
}
