package edu.nju.ws.spatialie.annprocess;

import edu.nju.ws.spatialie.data.BratDocument;
import edu.nju.ws.spatialie.utils.FileUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class GenerateCorpus {


    // 生成NER语料
    private static void generateNERCorpus(String inDir, String out_dir, boolean lineBased,
                                            boolean merge_path, int seq_len,  boolean shuffle, Long seed) {
        generateNERCorpus(new String [] {inDir},out_dir,lineBased,merge_path,seq_len, shuffle, seed);
    }


    private static void generateNERCorpus(String [] inDirs, String out_dir, boolean lineBased,
                                            boolean merge_path, int seq_len,  boolean shuffle, Long seed) {
        String [] types = {"Place", "MilitaryPlace", "MilitaryBase", "Country","AdministrativeDivision",
                "Path", "P_MilitaryPlace", "SpatialEntity", "SpatialSignal", "Motion", "Event",
                "MilitaryExercise", "Conference"};
        Set<String> typeSet = new HashSet<>(Arrays.asList(types));

        if (!FileUtil.exists(out_dir))
            FileUtil.createDir(out_dir);
        List<File> files = FileUtil.listFiles(inDirs);

        List<Pair<String,List<String []>>> rows_group = new ArrayList<>();
        for (int i=0; i<files.size();i+=2) {
            BratDocument bratDocument = new BratDocument(files.get(i+1).getPath(),files.get(i).getPath());
            try {
                rows_group.addAll(bratDocument.tokenRows(typeSet, seq_len));
            } catch (Exception e) {
                System.out.println(files.get(i).getPath());
                e.printStackTrace();
            }
            System.out.println(files.get(i).getName() + " is finished!");
        }
        if(shuffle) {
            Collections.shuffle(rows_group, new Random(seed));
        }

        List<String> lines = new ArrayList<>();
        int pos = (int) (7 * rows_group.size() / 10.0);
        List<String> train_lines;
        List<String> test_lines;
        if (lineBased){
            for (Pair<String, List<String[]>> rows: rows_group) {
                String line = rows.getLeft();
                List<String> tokens = rows.getRight().stream().map(x->x[0]).collect(Collectors.toList());
                List<String> labels = rows.getRight().stream()
                        .map(x->merge_path?x[1].replaceAll("-Path$", "-Place"):x[1])
                        .collect(Collectors.toList());
                line+="\t"+String.join(" ", tokens) + "\t" + String.join(" ", labels);
                lines.add(line);
            }
            train_lines = lines.subList(0, pos);
            test_lines = lines.subList(pos, lines.size());
        }else{
            List<String []> all_rows = new ArrayList<>();
            int sum = 0, idx = 0;
            for (Pair<String, List<String[]>> rows: rows_group) {
                all_rows.addAll(rows.getRight());
                all_rows.add(new String[]{"", ""});
                if (idx++ < pos) {
                    sum += rows.getRight().size()+1;
                }
            }

            lines = all_rows.stream().map(x -> x[0] + " " + x[1])
                    .map(String::trim)
                    .map(s -> s.replaceAll("[“”]", "\"")) // bert 中文 embedding
                    .collect(Collectors.toList());
            if (merge_path) {
                lines = lines.stream()
                        .map(line -> line.endsWith("-Path")?line.replaceAll("-Path", "-Place"):line)
                        .collect(Collectors.toList());
            }
            train_lines = lines.subList(0, sum);
            test_lines = lines.subList(sum, lines.size());
        }


//        Collections.shuffle(lines);

//        List<String> train_lines = lines.subList(0, pos);
//        List<String> test_lines = lines.subList(pos, lines.size());
        FileUtil.writeFile(out_dir+"/train.txt", train_lines, false);
        FileUtil.writeFile(out_dir+"/test.txt", test_lines, false);
        FileUtil.writeFile(out_dir+"/test.txt", test_lines, false);

//        System.out.println(tags.toString());
    }


//    private static void generateCorpus(String raw_dir, String out_dir, boolean lineBased,
//                                       boolean merge_path, int seq_len,  boolean shuffle, Random random) {
//        String [] types = {"Place", "MilitaryPlace", "MilitaryBase", "Country","AdministrativeDivision",
//                "Path", "P_MilitaryPlace", "SpatialEntity", "SpatialSignal", "Motion", "Event",
//                "MilitaryExercise", "Conference"};
//        Set<String> typeSet = new HashSet<>(Arrays.asList(types));
//
//        File folder = new File(raw_dir);
//        File [] files = folder.listFiles();
//        assert files != null;
//        List<Pair<String,List<String []>>> rows_group = new ArrayList<>();
//        for (int i=0; i<files.length;i+=2) {
//            BratDocument bratDocument = new BratDocument(files[i+1].getPath(),files[i].getPath());
//            rows_group.addAll(bratDocument.tokenRows(typeSet, seq_len));
//            System.out.println(files[i].getName() + " is finished!");
//        }
//        if(shuffle) {
//            Collections.shuffle(rows_group, random);
//        }
//
//        List<String> lines = new ArrayList<>();
//        if (lineBased){
//            for (Pair<String, List<String[]>> rows: rows_group) {
//                String line = rows.getLeft();
//                List<String> tokens = rows.getRight().stream().map(x->x[0]).collect(Collectors.toList());
//                List<String> labels = rows.getRight().stream()
//                        .map(x->merge_path?x[1].replaceAll("-Path$", "-Place"):x[1])
//                        .collect(Collectors.toList());
//                line+="\t"+String.join(" ", tokens) + "\t" + String.join(" ", labels);
//                lines.add(line);
//            }
//        }else{
//            List<String []> all_rows = new ArrayList<>();
//            for (Pair<String, List<String[]>> rows: rows_group) {
//                all_rows.addAll(rows.getRight());
//                all_rows.add(new String[]{"", ""});
//            }
//            lines = all_rows.stream().map(x -> x[0] + " " + x[1])
//                    .map(String::trim).collect(Collectors.toList());
//            if (merge_path) {
//                lines = lines.stream()
//                        .map(line -> line.endsWith("-Path")?line.replaceAll("-Path", "-Place"):line)
//                        .collect(Collectors.toList());
//            }
//        }
////        Collections.shuffle(lines);
////        List<String> train_lines = lines.subList(0, pos);
////        List<String> test_lines = lines.subList(pos, lines.size());
//        FileUtil.writeFile(out_dir, lines, false);
////        System.out.println(tags.toString());
//    }



    private static void partitionCorpus(String dirPath, double proportion, List<String> data) {
        if (!FileUtil.exists(dirPath))
            FileUtil.createDir(dirPath);

        int pos = (int)Math.floor(proportion * data.size());
        List<String> train_data = data.subList(0, pos).stream().filter(o->!o.equals("")).collect(Collectors.toList());
        List<String> test_data = data.subList(pos, data.size()).stream().filter(o->!o.equals("")).collect(Collectors.toList());
        FileUtil.writeFile(dirPath + "/train.txt", train_data, false);
        FileUtil.writeFile(dirPath + "/test.txt", test_data, false);
        FileUtil.writeFile(dirPath + "/test.txt", test_data, false);
    }


    // 生成SRL语义角色标注语料
    private static void generateSRLFormatCorpus(String inDir, String outPath,  boolean includeConj, boolean mergePlace,
                                                boolean shuffle, long seed, double proportion, boolean partition) {
        generateSRLFormatCorpus(new String[]{inDir}, outPath, includeConj, mergePlace, shuffle, seed, proportion, partition);
    }


    private static void generateSRLFormatCorpus(String [] inDirs, String outPath,  boolean includeConj, boolean mergePlace,
                                                boolean shuffle, long seed, double proportion, boolean partition) {

        if (!FileUtil.exists(outPath))
            FileUtil.createDir(outPath);

        List<File> files = new ArrayList<>();
        for (String dir: inDirs) {
            File folder = new File(dir);
            File [] fileArray = folder.listFiles();
            if (fileArray != null)
                files.addAll(Arrays.asList(fileArray));
        }
        Map<String, List<List<BratDocument.SRLExample>>> linkGroupMap = new HashMap<>();
        Map<String, List<String>> linkMap = new HashMap<>();
        Map<String, Pair<Integer, Integer>> linkNoTriggerLineNumMap = new HashMap<>();

        for (int i=0; i<files.size();i+=2) {
            BratDocument bratDocument = new BratDocument(files.get(i+1).getPath(),files.get(i).getPath());
            bratDocument.getSRLFormatData(includeConj, mergePlace).forEach((type, list) -> {
                linkGroupMap.putIfAbsent(type, new ArrayList<>());
                linkGroupMap.get(type).addAll(list);
            });
            System.out.println(files.get(i).getName() + " is finished!");
        }

//        linkGroupMap.put("ALL", new ArrayList<>());
//        linkGroupMap.forEach((k,v)->linkGroupMap.get("ALL").addAll(v));

        if (shuffle) {
            linkGroupMap.forEach((k,v)-> {
                Collections.shuffle(v, new Random(seed));
            });
        }

        List<List<BratDocument.SRLExample>> allGroups = linkGroupMap.get("ALL");
        int pos = (int)Math.floor(proportion * allGroups.size());
        List<BratDocument.SRLExample> allLinks = allGroups.stream().flatMap(Collection::stream)
                .collect(Collectors.toList());
        List<BratDocument.SRLExample> allTrainLinks = allGroups.subList(0, pos).stream().flatMap(Collection::stream)
                .collect(Collectors.toList());
        List<BratDocument.SRLExample> allTestLinks = allGroups.subList(pos, allGroups.size()).stream()
                .flatMap(Collection::stream).collect(Collectors.toList());

        String [] types = {"ALL", "MLINK", "DLINK", "OLINK","TLINK","NoTrigger","TODLINK"};
        for (String type: types) {
            List<String> all_lines, train_lines, test_lines;
            if (type.equals("ALL")) {
                all_lines = allLinks.stream().filter(o->o.hasTrigger).map(BratDocument.SRLExample::toString)
                        .distinct().collect(Collectors.toList());
                train_lines = allTrainLinks.stream().filter(o->o.hasTrigger).map(BratDocument.SRLExample::toString)
                        .distinct().collect(Collectors.toList());
                test_lines = allTestLinks.stream().filter(o->o.hasTrigger).map(BratDocument.SRLExample::toString)
                        .distinct().collect(Collectors.toList());
            } else if (type.equals("NoTrigger")) {
                all_lines = allLinks.stream().filter(o->!o.hasTrigger).map(BratDocument.SRLExample::toString)
                        .distinct().collect(Collectors.toList());
                train_lines = allTrainLinks.stream().filter(o->!o.hasTrigger).map(BratDocument.SRLExample::toString)
                        .distinct().collect(Collectors.toList());
                test_lines = allTestLinks.stream().filter(o->!o.hasTrigger).map(BratDocument.SRLExample::toString)
                        .distinct().collect(Collectors.toList());
            } else if(type.equals("TODLINK")) {
                all_lines= allLinks.stream().filter(o->o.hasTrigger && !o.type.equals("MLINK"))
                        .map(BratDocument.SRLExample::toString).distinct().collect(Collectors.toList());

                train_lines = allTrainLinks.stream().filter(o->o.hasTrigger && !o.type.equals("MLINK"))
                        .map(BratDocument.SRLExample::toString).distinct().collect(Collectors.toList());

                test_lines = allTestLinks.stream().filter(o->o.hasTrigger && !o.type.equals("MLINK"))
                        .map(BratDocument.SRLExample::toString).distinct().collect(Collectors.toList());
            } else {
                all_lines= allLinks.stream().filter(o->o.hasTrigger && o.type.equals(type))
                        .map(BratDocument.SRLExample::toString).distinct().collect(Collectors.toList());
                train_lines = allTrainLinks.stream().filter(o->o.hasTrigger && o.type.equals(type))
                        .map(BratDocument.SRLExample::toString).distinct().collect(Collectors.toList());

                test_lines = allTestLinks.stream().filter(o->o.hasTrigger && o.type.equals(type))
                        .map(BratDocument.SRLExample::toString).distinct().collect(Collectors.toList());
            }

            FileUtil.writeFile(outPath + "/" + type + ".txt", all_lines);
            if (partition) {
                String path = outPath + "/" + type;
                if (!FileUtil.exists(path))
                    FileUtil.createDir(path);
                FileUtil.writeFile(path + "/" + "train.txt", train_lines);
                FileUtil.writeFile(path + "/" + "test.txt", test_lines);
                FileUtil.writeFile(path + "/" + "test.txt", test_lines);
            }
        }




//        int train_num = (int) lines.subList(0, pos).stream().filter(o -> o.equals("")).count();
//        int dev_num = (int) lines.subList(pos, lines.size()).stream().filter(o -> o.equals("")).count();
//
//
//
//
//        for (String type: linkGroupMap.keySet()) {
//            List<String> lines = linkGroupMap.get(type).stream().flatMap(Collection::stream)
//                    .map(BratDocument.SRLExample::toString).collect(Collectors.toList());
//            int pos = (int)Math.floor(proportion * lines.size());
//            int train_num = (int) lines.subList(0, pos).stream().filter(o -> o.equals("")).count();
//            int dev_num = (int) lines.subList(pos, lines.size()).stream().filter(o -> o.equals("")).count();
//            linkNoTriggerLineNumMap.put(type, new ImmutablePair<>(train_num, dev_num));
//            linkMap.put(type, lines.stream().distinct().collect(Collectors.toList()));
//        }

//        linkMap.put("TODLINK", linkMap.get("ALL").stream().filter(line->!line.contains("B-mover"))
//                .collect(Collectors.toList()));

//        if (partition) {
//            linkMap.forEach((k,v)-> partitionCorpus(outPath + "/" + k, proportion, v));
//            partitionCorpus(outPath + "/DLINK", proportion, linkMap.get("DLINK"));
//            partitionCorpus(outPath + "/OLINK", proportion, linkMap.get("OLINK"));
//            partitionCorpus(outPath + "/MLINK", proportion, linkMap.get("MLINK"));
//            partitionCorpus(outPath + "/TLINK", proportion, linkMap.get("TLINK"));
//            partitionCorpus(outPath + "/TODLINK", proportion, linkMap.get("TODLINK"));
//            partitionCorpus(outPath + "/ALL", proportion, linkMap.get("ALL"));
//        }

//        linkMap.forEach((k,v) -> linkMap.put(k, v.stream().filter(o->!o.equals("")).collect(Collectors.toList())));
//        linkMap.forEach((k,v)-> FileUtil.writeFile(outPath+"/" + k +".txt", v, false));

//        FileUtil.writeFile(outPath + "/MLink.txt", linkMap.get("MLINK"), false);
//        FileUtil.writeFile(outPath + "/OLink.txt", linkMap.get("OLINK"), false);
//        FileUtil.writeFile(outPath + "/TLink.txt", linkMap.get("TLINK"), false);
//        FileUtil.writeFile(outPath + "/DLink.txt", linkMap.get("DLINK"), false);
//        FileUtil.writeFile(outPath + "/TODLink.txt", linkMap.get("TODLINK"), false);
//        FileUtil.writeFile(outPath + "/AllLink.txt", linkMap.get("ALL"), false);


//        List<String> noTriggerLinesInfo = linkNoTriggerLineNumMap.entrySet().stream()
//                .map(e -> e.getKey() + ":train" + e.getValue().getLeft()+ " " + e.getValue().getRight())
//                .collect(Collectors.toList());
//
//        FileUtil.writeFile(outPath + "/NoTrigger.txt", noTriggerLinesInfo, false);

    }

    // 生成关系抽取语料
    private static void generateRelationCorpus(String dirPath, boolean shuffle) {

        File folder = new File(dirPath);
        File [] files = folder.listFiles();
        assert files != null;
        List<List<String>>  TLinkGroup = new ArrayList<>();
        List<List<String>> OLinkGroup = new ArrayList<>();
        List<List<String>> MLinkGroup = new ArrayList<>();
        List<List<String>> DLinkGroup = new ArrayList<>();
        List<List<String>> allLinkGroup = new ArrayList<>();
        for (int i=0; i<files.length;i+=2) {
            BratDocument bratDocument = new BratDocument(files[i+1].getPath(),files[i].getPath());
            try {
                Map<String, List<List<String>>> result = bratDocument.getRelation();
                TLinkGroup.addAll(result.get("TLink"));
                OLinkGroup.addAll(result.get("OLink"));
                MLinkGroup.addAll(result.get("MLink"));
                DLinkGroup.addAll(result.get("DLink"));
                allLinkGroup.addAll(result.get("All"));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("ERROR" + files[i].getPath());
            }
            System.out.println(files[i].getName() + " is finished!");
        }
//        for (int i=)
        if (shuffle) {
            Collections.shuffle(TLinkGroup);
            Collections.shuffle(OLinkGroup);
            Collections.shuffle(MLinkGroup);
            Collections.shuffle(DLinkGroup);
            Collections.shuffle(allLinkGroup);
        }
        List<String> TLink = TLinkGroup.stream().flatMap(Collection::stream).collect(Collectors.toList());
        List<String> OLink = OLinkGroup.stream().flatMap(Collection::stream).collect(Collectors.toList());
        List<String> MLink = MLinkGroup.stream().flatMap(Collection::stream).collect(Collectors.toList());
        List<String> DLink = DLinkGroup.stream().flatMap(Collection::stream).collect(Collectors.toList());
        List<String> allLink = allLinkGroup.stream().flatMap(Collection::stream).collect(Collectors.toList());




        FileUtil.writeFile("data/processed_data/relation/MLink.txt", MLink, false);
        FileUtil.writeFile("data/processed_data/relation/OLink.txt", OLink, false);
        FileUtil.writeFile("data/processed_data/relation/TLink.txt", TLink, false);
        FileUtil.writeFile("data/processed_data/relation/DLink.txt", DLink, false);
        FileUtil.writeFile("data/processed_data/relation/AllLink.txt", allLink, false);

        partitionCorpus("data/processed_data/relation/DLINK", 0.8, DLink);
        partitionCorpus("data/processed_data/relation/OLINK", 0.8, OLink);
        partitionCorpus("data/processed_data/relation/MLINK", 0.8, MLink);
        partitionCorpus("data/processed_data/relation/TLINK", 0.8, TLink);
        partitionCorpus("data/processed_data/relation/ALL", 0.8, allLink);
    }



    public static void filterMLINK(String dirPath) {
        List<String> lines=FileUtil.readLines(dirPath);
        lines = lines.stream().filter(line->!line.contains("B-mover")).collect(Collectors.toList());
        FileUtil.writeFile(dirPath,lines);
    }


    public static void removeSpatialEntity(String inPath, String outPath, String mod) {

        List<String> retainedLines = new ArrayList<>();
        List<String> filteredLines = new ArrayList<>();
        if (mod.equals("line")) {
            List<String> lines = FileUtil.readLines(inPath);
            for (String line: lines) {
                if (line.contains("SpatialSignal") || line.contains("Event")) {
                    filteredLines.add(line);
                } else
                    retainedLines.add(line);
            }
        }
        else {
            List<String> lines = FileUtil.readLines(inPath);
            int start = 0;
            boolean tag = true;
            for (int i=0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.trim().length() == 0 || i == lines.size()-1) {
                    if (tag) {
                        retainedLines.addAll(lines.subList(start, i + 1));
                    } else {
                        filteredLines.addAll(lines.subList(start, i + 1));
                    }
                    tag = true;
                    start = i + 1;
                }
                if (line.contains("SpatialSignal") || line.contains("Event"))
                    tag = false;
            }
        }
        FileUtil.writeFile(inPath, retainedLines);
        FileUtil.writeFile(outPath,filteredLines, true);
    }


    public static void generateFinalNerCorpus() {
        long seed = 0;
        String [] dirs = {"data/annotation/0-49", "data/annotation/msra"};
        GenerateCorpus.generateNERCorpus("data/annotation/0-49", "data/processed_data/ner_final/random",
                false, true, 100, true, seed);

        GenerateCorpus.generateNERCorpus("data/annotation/0-49", "data/processed_data/ner_final/joint_random",
                true,  true, 100, true, seed);

        GenerateCorpus.generateNERCorpus("data/annotation/msra", "data/processed_data/msra/ner/random",
                false, true, 100, true, seed);

        GenerateCorpus.generateNERCorpus("data/annotation/msra", "data/processed_data/msra/ner/joint_random",
                true,  true, 100, true, seed);


        GenerateCorpus.removeSpatialEntity("data/processed_data/msra/ner/joint_random/test.txt",
                "data/processed_data/msra/ner/joint_random/train.txt", "line");

        GenerateCorpus.removeSpatialEntity("data/processed_data/msra/ner/random/test.txt",
                "data/processed_data/msra/ner/random/train.txt", "");

        FileUtil.mergeFile("data/processed_data/msra/ner/random/train.txt", "data/processed_data/ner_final/random/train.txt");
        FileUtil.mergeFile("data/processed_data/msra/ner/random/test.txt", "data/processed_data/ner_final/random/test.txt");
        FileUtil.mergeFile("data/processed_data/msra/ner/random/test.txt", "data/processed_data/ner_final/random/test.txt");


        FileUtil.mergeFile("data/processed_data/msra/ner/joint_random/train.txt", "data/processed_data/ner_final/joint_random/train.txt");
        FileUtil.mergeFile("data/processed_data/msra/ner/joint_random/test.txt", "data/processed_data/ner_final/joint_random/test.txt");
        FileUtil.mergeFile("data/processed_data/msra/ner/joint_random/test.txt", "data/processed_data/ner_final/joint_random/test.txt");

    }

    public static void main(String [] args) {
//        GenerateCorpus.generateNERCorpus("data/annotation/0-49", "data/processed_data/ner/normal",
//                false, false, false, 100);
//        GenerateCorpus.generateNERCorpus("data/annotation/0-49", "data/processed_data/ner/random",
//                false, true,false, 100);
//
//        GenerateCorpus.generateNERCorpus("data/annotation/0-49", "data/processed_data/ner/joint_normal",
//                true, false, false, 100);
//        GenerateCorpus.generateNERCorpus("data/annotation/0-49", "data/processed_data/ner/joint_random",
//                true, true, false, 100);

//        long seed =  System.currentTimeMillis();
        long seed = 7;
        Random random_1 = new Random(seed), random_2= new Random(seed);
//        GenerateCorpus.generateNERCorpus("data/annotation/0-49", "data/processed_data/ner_v2/normal",
//                false, true, 100,false, null);
//        GenerateCorpus.generateNERCorpus("data/annotation/0-49", "data/processed_data/ner_v2/random",
//                false, true, 100, true, random_1);
//
//        GenerateCorpus.generateNERCorpus("data/annotation/0-49", "data/processed_data/ner_v2/joint_normal",
//                true,  true, 100, false, null);
//        GenerateCorpus.generateNERCorpus("data/annotation/0-49", "data/processed_data/ner_v2/joint_random",
//                true,  true, 100, true, random_2);

//        GenerateCorpus.generateCorpus("data/annotation/0-49", "data/processed_data/ner_v3/joint/train.txt",
//                true, false, 100, true, random_1);
//        GenerateCorpus.generateCorpus("data/annotation/msra", "data/processed_data/ner_v3/joint/test.txt",
//                true, false, 100, false, null);
//        GenerateCorpus.generateCorpus("data/annotation/msra", "data/processed_data/ner_v3/joint/test.txt",
//                true, false, 100, false, null);
//
//
//        GenerateCorpus.generateCorpus("data/annotation/0-49", "data/processed_data/ner_v3/normal/train.txt",
//                false, false, 100, true, random_1);
//        GenerateCorpus.generateCorpus("data/annotation/msra", "data/processed_data/ner_v3/normal/test.txt",
//                false, false, 100, false, null);
//        GenerateCorpus.generateCorpus("data/annotation/msra", "data/processed_data/ner_v3/normal/test.txt",
//                false, false, 100, false, null);

//        GenerateCorpus.generateRelationCorpus("data/annotation/0-49", true);

//        long seed = 1000;

//        GenerateCorpus.generateSRLFormatCorpus("data/annotation/0-49", "data/processed_data/srl_link/military",
//                false, false, true, seed, 0.8, true);
//        GenerateCorpus.generateSRLFormatCorpus("data/annotation/0-49", "data/processed_data/srl_link/military_conj",
//                true, false, true, seed, 0.8, true);
//        GenerateCorpus.generateSRLFormatCorpus("data/annotation/0-49", "data/processed_data/srl_link/military_merge",
//                false, true, true, seed, 0.8, true);
//        GenerateCorpus.generateSRLFormatCorpus("data/annotation/0-49", "data/processed_data/srl_link/military_conj_merge",
//                true, true, true, seed, 0.8, true);

//        String [] dirs = {"data/annotation/0-49", "data/annotation/msra"};
//        GenerateCorpus.generateSRLFormatCorpus(dirs, "data/processed_data/srl_link/all/all",
//                false, false, true, seed, 0.8, true);
//        GenerateCorpus.generateSRLFormatCorpus(dirs, "data/processed_data/srl_link/all/all_conj",
//                true, false, true, seed, 0.8, true);
//        GenerateCorpus.generateSRLFormatCorpus(dirs, "data/processed_data/srl_link/all/all_merge",
//                false, true, true, seed, 0.8, true);
//        GenerateCorpus.generateSRLFormatCorpus(dirs, "data/processed_data/srl_link/all/all_conj_merge",
//                true, true, true, seed, 0.8, true);


//        String [] dirs = {"data/annotation/0-49", "data/annotation/msra"};
//        GenerateCorpus.generateSRLFormatCorpus(dirs, "data/processed_data/srl_link/all/all",
//                false, false, true, seed, 0.8, true);
//        GenerateCorpus.generateSRLFormatCorpus(dirs, "data/processed_data/srl_link/all/all_conj",
//                true, false, true, seed, 0.8, true);
//        GenerateCorpus.generateSRLFormatCorpus(dirs, "data/processed_data/srl_link/all/all_merge",
//                false, true, true, seed, 0.8, true);
//        GenerateCorpus.generateSRLFormatCorpus(dirs, "data/processed_data/srl_link/all/all_conj_merge",
//                true, true, true, seed, 0.8, true);



        String [] dirs = {"data/annotation/0-49", "data/annotation/msra", "data/annotation/reason_v2"};
        GenerateCorpus.generateSRLFormatCorpus(dirs, "data/processed_data/srl_link/all+reason_seed_7_new/all",
                false, false, true, seed, 0.8, true);
        GenerateCorpus.generateSRLFormatCorpus(dirs, "data/processed_data/srl_link/all+reason_seed_7_new/all_conj",
                true, false, true, seed, 0.8, true);
        GenerateCorpus.generateSRLFormatCorpus(dirs, "data/processed_data/srl_link/all+reason_seed_7_new/all_merge",
                false, true, true, seed, 0.8, true);
        GenerateCorpus.generateSRLFormatCorpus(dirs, "data/processed_data/srl_link/all+reason_seed_7_new/all_conj_merge",
                true, true, true, seed, 0.8, true);



//        GenerateCorpus.generateTokenCorpus("data/annotation/0-49", "data/processed_data/ner_v2/normal",
//                false, true, 100,false, null);

//        GenerateCorpus.generateTokenCorpus(dirs, "data/processed_data/ner_all/random",
//                false, true, 100, true, seed);
//
//        GenerateCorpus.generateTokenCorpus(dirs, "data/processed_data/ner_all/joint_random",
//                true,  true, 100, true, seed);




//        GenerateCorpus.generateSRLFormatCorpus("data/annotation/0-49", "data/processed_data/srl_link/with_conj",
//                true, 0.8, true, true);
//
//        GenerateCorpus.generateSRLFormatCorpus("data/annotation/msra", "data/processed_data/srl_link/msra_with_conj",
//                false, 0, true, false);

//        GenerateCorpus.filterMLINK("data/processed_data/srl_link/with_conj/TOMLINK/train.txt");
//        GenerateCorpus.filterMLINK("data/processed_data/srl_link/with_conj/TOMLINK/test.txt");
//        GenerateCorpus.filterMLINK("data/processed_data/srl_link/with_conj/TOMLINK/test.txt");


//        GenerateCorpus.filterMLINK("data/processed_data/srl_link/without_conj/TOMLINK/train.txt");
//        GenerateCorpus.filterMLINK("data/processed_data/srl_link/without_conj/TOMLINK/test.txt");
//        GenerateCorpus.filterMLINK("data/processed_data/srl_link/without_conj/TOMLINK/test.txt");
//        GenerateCorpus.filterMLINK("data/processed_data/srl_link/without_conj/TOMLINK/test.txt");

//        GenerateCorpus.generateFinalNerCorpus();
    }
}
