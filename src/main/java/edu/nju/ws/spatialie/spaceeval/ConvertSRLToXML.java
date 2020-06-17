package edu.nju.ws.spatialie.spaceeval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import edu.nju.ws.spatialie.utils.FileUtil;
import edu.nju.ws.spatialie.utils.XmlUtil;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;


public class ConvertSRLToXML {

    //xml文件目录
    private static String originxmldir = "data/SpaceEval2015/raw_xml";

    //inputpath中文件对应的生成的带xml文件信息的SRL格式的输入文件
    private static String info_srl="output/predict_hasnotrigger/srl_info.txt";

    //输出目录
    private static String outputdir="output/predict_hasnotrigger/xml";

    //需要转换的文件
    private static String inputpath="output/predict_hasnotrigger/predict.txt";
    
    private static Map<String,List<String>> linkattrMap = initLinkAttrMap();
    private static Map<String,List<String>> essentialattrMap = initEssentialAttrMap();
    private static Map<String,Map<String,Integer>> countelementMap = new HashMap<>();
    private static Map<String,String> linkabbrMap = initLinkAbbrMap();

    private static Map<String, String> initLinkAbbrMap() {
        Map<String,String> res = new HashMap<>();
        res.put("QSLINK", "qs");
        res.put("OLINK", "o");
        res.put("MOVELINK", "mv");
        res.put("MEASURELINK", "ml");
        return res;
    }

    private static Map<String, List<String>> initEssentialAttrMap() {
        Map<String,List<String>> res = new HashMap<>();
        res.put("QSLINK", Arrays.asList("trajector landmark".split(" ")));
        res.put("OLINK", Arrays.asList("trajector landmark".split(" ")));
        res.put("MOVELINK", Arrays.asList("mover".split(" ")));
        res.put("MEASURELINK", Arrays.asList("trajector landmark".split(" ")));
        return res;
    }

    private static Map<String, List<String>> initLinkAttrMap() {
        String QSLINK = "comment fromID fromText id landmark relType toID toText trajector trigger";
        String OLINK="comment frame_type fromID fromText id landmark projective referencePt relType toID toText trajector trigger";
        String MOVELINK="comment fromID fromText goal goal_reached id landmark midPoint motion_signalID mover pathID source toID toText trigger";
        String MEASURELINK="comment endPoint1 endPoint2 fromID fromText id landmark relType toID toText trajector val";
        Map<String,List<String>> res = new HashMap<>();
        res.put("QSLINK", Arrays.asList(QSLINK.split(" ")));
        res.put("OLINK", Arrays.asList(OLINK.split(" ")));
        res.put("MOVELINK", Arrays.asList(MOVELINK.split(" ")));
        res.put("MEASURELINK", Arrays.asList(MEASURELINK.split(" ")));
        return res;


    }

    public static void convert(File file) {
        try {
            List<String> info = FileUtil.readLines(info_srl);
            List<String> words = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            List<String> tags = new ArrayList<>();
            List<String> lines = FileUtil.readLines(file.getAbsolutePath());
            if (!lines.get(lines.size()-1).equals("")) lines.add("");
            for (String line: lines){
                if (line.equals("")){
                    String sentence = String.join(" ",words);
                    String xmlfilename = null;
                    List<String> originids = null;
                    for (String infoline:info){
                        if (infoline.contains(sentence)){
                            xmlfilename  = infoline.split("\t")[4];
                            originids = Arrays.asList(infoline.split("\t")[5].split(" "));
                            break;
                        }
                    }
                    Element root = null;
                    if (!FileUtil.exists(outputdir+File.separator+xmlfilename)){
                        root = XmlUtil.getRootElement(originxmldir+File.separator+xmlfilename);
                        Element tage = root.element("TAGS");
                        List<Element> tobedel = new ArrayList<>();
                        for(Iterator it = tage.elementIterator(); it.hasNext();) {
                            Element element = (Element) it.next();
                            String elementTag = element.getName();
                            switch (elementTag) {
                                case SpaceEvalUtils.QSLINK:
                                case SpaceEvalUtils.OLINK:
                                case SpaceEvalUtils.MOVELINK:
                                case SpaceEvalUtils.MEASURELINK:
                                case SpaceEvalUtils.METALINK:tobedel.add(element);
                            }
                        }
                        for (Element element:tobedel)
                            tage.remove(element);
                        Map<String,Integer> countMap = countElementInit(tage);
                        countelementMap.put(xmlfilename,countMap);
                    } else {
                        root = XmlUtil.getRootElement(outputdir+File.separator+xmlfilename);
                    }
                    Map<String,String> attrMap = new HashMap<>();
                    String linktype = null;
                    for (int i = 0;i<words.size();i++){
                        if (labels.get(i).startsWith("B-")){
                            String role = labels.get(i).substring(2);
                            if (role.toLowerCase().equals("trigger")||role.toLowerCase().equals("flag")){
                                if (role.toLowerCase().equals("flag")){
                                    // no trigger link
                                    linktype = "QSLINK";
                                } else {
                                    String tag = tags.get(i).substring(2);
                                    if (tag.toLowerCase().equals("spatial_signal")){
                                        //otlink
                                        int j = i+1;
                                        StringBuilder triggerwords = new StringBuilder(words.get(i));
                                        while (j<words.size()&&labels.get(j).startsWith("I-")) {
                                            j++;
                                            triggerwords.append(" ").append(words.get(j));
                                        }
                                        if (triggerwords.toString().toLowerCase().contains("east")|| triggerwords.toString().toLowerCase().contains("west")
                                                || triggerwords.toString().toLowerCase().contains("south")|| triggerwords.toString().toLowerCase().contains("north")){
                                            linktype="OLINK";
                                        } else {
                                            linktype="QSLINK";
                                        }
                                    } else if (tag.toLowerCase().equals("measure")){
                                        //dlink
                                        role = "val";
                                        linktype="MEASURELINK";
                                    } else{
                                        //mlink
                                        linktype="MOVELINK";
                                    }
                                }
                            }
                            if (!role.toLowerCase().equals("flag")) {
                                assert originids != null;
                                attrMap.put(role,originids.get(i).substring(2));
                            }
                        }
                    }
                    Element tage = root.element("TAGS");
                    System.out.println(sentence);
                    System.out.println(xmlfilename);

//                    if (linktype.equals("MOVELINK")){
//                        System.out.println(linktype);
//                    }
                    Element link = tage.addElement(linktype);
                    for (String essentialattr:essentialattrMap.get(linktype)){
                        if (!attrMap.containsKey(essentialattr)){
                            int count = countelementMap.get(xmlfilename).get("PLACE");
                            Element newplace = tage.addElement("PLACE");
                            newplace = constructNewPlace(count,newplace);
                            attrMap.put(essentialattr,"pl"+count);
                            countelementMap.get(xmlfilename).put("PLACE",count+1);
                        }
                    }
                    for (String attr:linkattrMap.get(linktype)){
                        if (attrMap.containsKey(attr)){
                            link.addAttribute(attr,attrMap.get(attr));
                        } else {
                            if (attr.toLowerCase().equals("id")){
                                int count = countelementMap.get(xmlfilename).get(linktype);
                                link.addAttribute(attr,linkabbrMap.get(linktype)+count);
                                countelementMap.get(xmlfilename).put(linktype,count+1);
                            } else
                                link.addAttribute(attr,"");
                        }
                    }
                    OutputFormat format = OutputFormat.createPrettyPrint();
                    XMLWriter writer = new XMLWriter(new FileOutputStream(new File(outputdir+File.separator+xmlfilename)), format);
                    writer.write(root);
                    words.clear();
                    tags.clear();
                    labels.clear();
                } else {
                    words.add(line.split(" ")[0]);
                    tags.add(line.split(" ")[1]);
                    labels.add(line.split(" ")[3]);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Element constructNewPlace(int count, Element newplace) {
        newplace.addAttribute("id","pl"+count)
                .addAttribute("start","-1")
                .addAttribute("end","-1");
        String attrs = "comment continent countable country ctv dcl dimensionality domain elevation form gazref gquant latLong mod scopes state text type";
        for (String attr:attrs.split(" ")){
            newplace.addAttribute(attr,"");
        }
        return newplace;
    }

    private static Map<String, Integer> countElementInit(Element tage) {
        Map<String,Integer> res = new HashMap<>();
        res.put("QSLINK",0);
        res.put("OLINK",0);
        res.put("MEASURELINK",0);
        res.put("MOVELINK",0);
        int placec = 0;
        for(Iterator it = tage.elementIterator(); it.hasNext();){
            Element element = (Element) it.next();
            if (element.getQName().getName().toLowerCase().equals("place")){
                placec++;
            }
        }
        res.put("PLACE",placec);
        return res;
    }

    public static void main(String[] args) {
        // 执行dom4j生成xml方法
        convert(new File(inputpath));
    }
}
