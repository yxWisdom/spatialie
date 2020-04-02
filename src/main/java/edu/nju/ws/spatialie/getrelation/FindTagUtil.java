package edu.nju.ws.spatialie.getrelation;

import com.alibaba.fastjson.JSONObject;
import edu.nju.ws.spatialie.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FindTagUtil {
    static String dir = "data/relation/findtag";
    public static String findtag(String content) {
//        content = content.substring(0,60);
//        content = content.replaceAll("approx ~","approx .");
        List<File> files;
        File folder = new File(dir);
        File[] fileArray = folder.listFiles();
        files = Arrays.asList(fileArray);
        for (File file:files){
            List<String> lines = FileUtil.readLines(file.getPath());
            for (String line:lines){
                if (line.split("\t")[1].startsWith(content)) return line;
            }
        }
        return null;
    }

    public static void dealwithNREfile(String path,String outdir){
        List<String> lines = FileUtil.readLines(path);
        List<JSONObject> sametextList = new ArrayList<>();
        String lastline = null;
        List<String> outputlines = new ArrayList<>();
        boolean deleteall = false;
        for (String line:lines){
            JSONObject obj = JSONObject.parseObject(line);
            JSONObject objt = obj.getJSONObject("t");
            JSONObject objh = obj.getJSONObject("h");
            if (lastline!=null&&lastline.substring(0,lastline.indexOf("]")).equals(line.substring(0,line.indexOf("]")))){
                boolean f = false;
                boolean hassame = false;
                JSONObject objtobedelete = null;
                for (JSONObject obj_last:sametextList){
                    JSONObject objt_l = obj_last.getJSONObject("t");
                    JSONObject objh_l = obj_last.getJSONObject("h");
                    if (objh.equals(objh_l)&&objt.equals(objt_l)){
                        hassame = true;
                        String relation = obj.getString("relation");
                        String relation_l = obj_last.getString("relation");
                        if (relation.equals("None")){
                            f = true;
                        } else {
                            if (relation_l.equals("None")) {
                                f = false;
                                objtobedelete = obj_last;
                            } else {
                                deleteall = true;
                            }
                        }
                        break;
                    }
                }
                objh = changeobj(objh);
                objt = changeobj(objt);
                obj.put("h",objh);
                obj.put("t",objt);
                if (hassame) {
                    System.out.println(line);
                    if (!f) {
                        sametextList.add(obj);
                        sametextList.remove(objtobedelete);
                    }
                } else {
                    sametextList.add(obj);
                }
            } else {
                if (!deleteall) {
                    for (JSONObject obj_last : sametextList) {
                        outputlines.add(obj_last.toJSONString());
                    }
                }
                sametextList.clear();
                sametextList.add(obj);
                lastline = line;
                deleteall = false;
            }
        }
        FileUtil.writeFile(outdir,outputlines);
    }

    private static JSONObject changeobj(JSONObject objh) {
        String name = objh.getString("name");
        name = name.replaceAll("([a-z]),","$1 ,");
        name = name.replaceAll(" c/"," c /");
        name = name.replaceAll("c/","c /");
        name = name.replaceAll(" {2,}"," ");
        objh.put("name",name);
        return objh;
    }
    public static void main(String[] args){
        dealwithNREfile("data/SpaceEval2015/processed_data/openNRE/OTMDLink_12_4/train.txt","data/SpaceEval2015/processed_data/openNRE/OTMDLink_12_4/train_edited.txt");
    }
}
