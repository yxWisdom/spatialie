package edu.nju.ws.spatialie.spaceeval;

import edu.nju.ws.spatialie.utils.CollectionUtils;
import edu.nju.ws.spatialie.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenerateSRLRuleCorpus {

    static void run(String rawPath, String ruledPath, String outputPath) {
        List<List<String>> groupLinesList = CollectionUtils.split(FileUtil.readLines(ruledPath), "");

        Map<String, String> sentenceToRuleTagsMap = new HashMap<>();
        Map<String, List<String>> sentenceToRuleLinesMap = new HashMap<>();
        groupLinesList.forEach(groupLines -> {
            StringBuilder tokensSb = new StringBuilder();
            StringBuilder tagsSb = new StringBuilder();
            StringBuilder labelsSb = new StringBuilder();
            StringBuilder ruleTagsSb = new StringBuilder();
            groupLines.forEach(line-> {
                String [] fields = line.trim().split(" ");
                if (fields[2].endsWith("trigger"))
                    fields[3] = fields[2];

                tokensSb.append(fields[0]).append(" ");
                tagsSb.append(fields[1]).append(" ");
                labelsSb.append(fields[2]).append(" ");
                ruleTagsSb.append(fields[3]).append(" ");
            });
            tokensSb.setCharAt(tokensSb.length()-1, '\t');
            tagsSb.setCharAt(tagsSb.length()-1, '\t');
            labelsSb.deleteCharAt(labelsSb.length()-1);
            ruleTagsSb.setCharAt(ruleTagsSb.length()-1, '\t');
            String key = tokensSb.append(tagsSb).append(labelsSb).toString();
            sentenceToRuleLinesMap.put(key, groupLines);
            sentenceToRuleTagsMap.put(key, ruleTagsSb.toString());
        });

        List<String> orderedRuleLines = new ArrayList<>();
        List<String> outputLines = new ArrayList<>();

        for (String line: FileUtil.readLines(rawPath)) {
            StringBuilder sb = new StringBuilder(line.trim());
            String key = sb.substring(sb.indexOf("\t") + 1);
            sb.insert(sb.lastIndexOf("\t") + 1, sentenceToRuleTagsMap.get(key));
            outputLines.add(sb.toString());
            try {
                orderedRuleLines.addAll(sentenceToRuleLinesMap.get(key));
            } catch (NullPointerException e){
                System.out.println(e);
            }

        }


        String orderRulePath = rawPath.substring(0, rawPath.lastIndexOf("\\") + 1) + "rule.txt";
        FileUtil.writeFile(orderRulePath, orderedRuleLines);
        FileUtil.writeFile(outputPath, outputLines);
    }

    public static void main(String [] args) {

//        String [] baseDirList = {
//                "data/SpaceEval2015/processed_data/SRL",
//                "data/SpaceEval2015/processed_data/SRL_subset"
//        };
//
//        for (String baseDir: baseDirList) {
//            List<File> fileList = FileUtil.listFiles(baseDir);
//            for (File file : fileList) {
//                String filePath = file.getPath().replaceAll("\\\\","/");
//                int idx = filePath.lastIndexOf("/");
//                inputdir = filePath.substring(0, idx+1);
//                outputdir = inputdir.replaceFirst("data", "output");
//                filename = filePath.substring(idx + 1);
//
//                if (filename.equals("rule.txt")) continue;
//
//                FileUtil.createDir(outputdir);
//                generateCorpus(inputdir + filename);
//            }
//        }



        String baseDir = "data/SpaceEval2015/processed_data/SRL_subset";
        List<File> fileList = FileUtil.listFiles(baseDir);

        for (File file : fileList) {
            String fileDir = file.getParent();
            String fileName = file.getName();

            if (fileName.equals("rule.txt")) continue;

            String rawPath = file.getPath();
            String rulePath = rawPath.replaceFirst("data", "output");
            String outputPath =fileDir.replaceFirst("SRL_subset", "SRL_subset_rule");


            FileUtil.createDir(outputPath);
            run(rawPath, rulePath, outputPath+ "/" + fileName);
        }


        baseDir = "data/SpaceEval2015/processed_data/SRL";
        fileList = FileUtil.listFiles(baseDir);

        for (File file : fileList) {
            String fileDir = file.getParent();
            String fileName = file.getName();

            if (fileName.equals("rule.txt")) continue;

            String rawPath = file.getPath();
            String rulePath = rawPath.replaceFirst("data", "output");
            String outputPath =fileDir.replaceFirst("SRL", "SRL_rule");

            FileUtil.createDir(outputPath);
            run(rawPath, rulePath, outputPath + "/" + fileName);
        }
    }
}
