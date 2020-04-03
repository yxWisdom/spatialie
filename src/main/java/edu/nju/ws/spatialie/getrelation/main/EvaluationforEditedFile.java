package edu.nju.ws.spatialie.getrelation.main;

import edu.nju.ws.spatialie.utils.FileUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EvaluationforEditedFile {
    public static List<String> readLineswithEmptyLine(String path) {
        File file = new File(path);
        InputStream is = null;
        BufferedReader br = null;
        String tmp;
        List<String> rst = new ArrayList<>();
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            br = new BufferedReader(new InputStreamReader(is, "utf-8"));
            while ((tmp = br.readLine()) != null) {
                rst.add(tmp);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rst;
    }

    public static void evel(String PATH){
        List<String> tlines = readLineswithEmptyLine(PATH);
        String[] lines = tlines.toArray(new String[tlines.size()]);
        int gold = 0;
        int predict = 0;
        int countright = 0;
        for (int idx = 0;idx<lines.length;idx++) {
            boolean isright = true;
            Set<String> elements1 = new HashSet<>();
            Set<String> elements2 = new HashSet<>();
            int other = 0;
            String duplicate = "";
            int start = idx;
            int t1 = countright,t2=predict,t3=gold;
            boolean isanswer = false;
            boolean isdlink;
            while (idx < lines.length && !lines[idx].equals("") && !lines[idx].equals("\n")) {
                if (lines[idx].split(" ")[1].contains("Measure")) isdlink = true;
                String right = lines[idx].split(" ")[2];
                String answer = lines[idx].split(" ")[3];
                if (!answer.equals("O")) isanswer = true;
                if (right.startsWith("B")) {
                    if (elements1.contains(right)) {
                        gold++;
                        duplicate = right.substring(2);
                    } else {
                        elements1.add(right);
                    }
                }

                if (answer.startsWith("B")) {
                    if (elements2.contains(answer)) {
                        predict++;
                    } else {
                        elements2.add(answer);
                    }
                }
                if (!right.equals(answer)&&right.startsWith("B")){
                    isright = false;
                }
                idx++;
            }
            gold++;
            if (isanswer) predict++;
            if (duplicate.isEmpty()){
                if (isright&&isanswer) countright++;
            } else {
                int tidx = idx;
                idx = start;
                while (idx < lines.length && !lines[idx].equals("") && !lines[idx].equals("\n")) {
                    String right = lines[idx].split(" ")[2];
                    String answer = lines[idx].split(" ")[3];
                    if (right.length()>2&&right.substring(2).equals(duplicate)){
                        if (right.startsWith("B")){
                            if (right.equals(answer)){
                                other++;
                            }
                        }
                    } else {
                        if (!right.equals(answer)) {
                            if (right.startsWith("B")&&!right.substring(2).equals(duplicate)&&answer.startsWith("B")) {
                                other = 0;
                                break;
                            }
                        }
                    }
                    idx++;
                }
                idx = tidx;
                countright+=other;
            }

//            System.out.println((countright-t1)+"\t"+(extract-t2)+"\t"+(gold-t3));
//            FileUtil.writeFile("data/BIO/all predict/record2.txt",(countright-t1)+"\t"+(extract-t2)+"\t"+(gold-t3),true);
        }
        //！！！输出
        System.out.println(countright+"\t"+predict+"\t"+gold);
        System.out.println(countright*1.0/gold);
        System.out.println(countright*1.0/predict);
    }

    public static void main(String [] args) {
        evel("output/SpaceEval2015/processed_data/SRL/QSNoTrigger/train.txt");
    }
}
