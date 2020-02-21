package edu.nju.ws.spatialie.annprocess;

import com.alibaba.fastjson.JSON;
import edu.nju.ws.spatialie.data.*;
import edu.nju.ws.spatialie.utils.*;
import org.jfree.ui.RefineryUtilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//import org.apache.commons.lang3.StringUtils;


public class DocSelection {
    private List<Article> articleList = new ArrayList<>();
    private Map<String, Integer> vocabulary = new HashMap<>();
    private Set<Integer> savedArticleId=null;
    public DocSelection() {
        loadArticles();
        loadSpatialWords();
    }
    public void loadArticles() {
        String path = "data/corpus/new_Les_MMRC.json";
        String text = FileUtil.readFile(path);
        articleList = JSON.parseArray(text, Article.class);
        assert articleList != null;
        articleList=articleList.stream()
                .filter(x->!x.getArticleContent().equals(""))
                .sorted(Comparator.comparing(Article::length))
                .collect(Collectors.toList());
    }
    private void loadSpatialWords(){
        List<String> absoluteDirection = FileUtil.readFileByLine("data/spatial relation/absolute_direction.txt", " ",0);
        List<String> noun = FileUtil.readFileByLine("data/spatial relation/noun.txt", " ", 0);
        List<String> predicate = FileUtil.readFileByLine("data/spatial relation/predicate.txt", " ", 0);
        List<String> relativeDirection = FileUtil.readFileByLine("data/spatial relation/relative_direction.txt", " ", 0);

        absoluteDirection.stream().filter(x -> x.length() > 1).forEach(x -> vocabulary.put(x, 3));
        noun.stream().filter(x -> x.length() > 1).forEach(x -> vocabulary.put(x, 3));
        predicate.stream().filter(x -> x.length() > 1).forEach(x -> vocabulary.put(x, 3));
        relativeDirection.stream().filter(x -> x.length() > 1).forEach(x -> vocabulary.put(x, 1));
    }


//    public void loadSavedArticle() {
//        List<String> lines = FileUtil.readFileByLine("data/corpus/statistics.txt", "\t", 0);
//        savedArticleId = lines.stream().filter(x-> !x.equals("")).map(Integer::parseInt).collect(Collectors.toSet());
//    }

    private int relationRelevance(String word) {
        if (vocabulary.containsKey(word)){
            return vocabulary.get(word);
        }
        return 0;
    }

    public List<Integer> getAllDocSize() {
        List<Integer> allDocLength=new ArrayList<>();
        for (Article article: articleList) {
            allDocLength.add(article.getArticleContent().length());
        }
        allDocLength.sort(Comparator.reverseOrder());
        System.out.println(allDocLength.size());
        System.out.println(allDocLength);
        System.out.println(allDocLength.get(allDocLength.size() / 10));

        Map<Integer, Integer> m = new HashMap<>();

        allDocLength.stream().map(x-> x<10000?x:10000).sorted(Comparator.comparing(Integer::intValue)).forEach(x -> {
            Integer n = m.get(x/500);
            m.put(x/500, n == null?1:n+1);
        });
        ChartUtil chartUtil = new ChartUtil("app");
        chartUtil.plotLineChart("长度于次数关系", m);
        chartUtil.pack();
        RefineryUtilities.centerFrameOnScreen(chartUtil);
        chartUtil.setVisible(true);
        return allDocLength;
    }


    public void statistics(){
//        String a =[LOCATION, NATIONALITY, NUMBER, IDEOLOGY, MONEY, PERSON, FACILITY, MISC, TIME, ORDINAL,
//        CAUSE_OF_DEATH, O, STATE_OR_PROVINCE, ORGANIZATION, DATE, COUNTRY, CITY, RELIGION, PERCENT, DEMONYM, TITLE, GPE, CRIMINAL_CHARGE]
//        Map<Integer, Double> relevance = new HashMap<>();
//        loadSavedArticle();
        Set<String> allLabels = new HashSet<>();
        String [] labels = {"LOCATION","GPE", "COUNTRY","STATE_OR_PROVINCE","COUNTRY","CITY", "GPE", "FACILITY","DEMONYM", "NATIONALITY"};
        Set<String> labelSet = new HashSet<String>(){{addAll(Arrays.asList(labels));}};
        String outPath = "data/result/statistics_2.txt";
        for(Article article: articleList) {
//            if (savedArticleId.contains(article.getArticleId()))
//                continue;
            int entityCount = 0;
            int relationCount = 0;
            List<Sentence> sentences = stanfordnlp.getSentences(article.getArticleContent());
            for (Sentence sentence:sentences) {
                for (String token: sentence.getTokens()) {
                    int degree = relationRelevance(token);
                    if (degree > 0) {
                        relationCount += degree;
                        article.getSpatialSignals().put(token, degree);
                    }
                }
                for (int i=0; i < sentence.size()-1;i++) {
                    String phrase = sentence.getTokens().get(i) + sentence.getTokens().get(i+1);
                    if (phrase.length() >= 4) {
                        int degree = relationRelevance(phrase);
                        if (degree > 0) {
                            relationCount += degree;
                            article.getSpatialSignals().put(phrase, degree);
                        }
                    }
                }
                for (int i=0; i < sentence.mention_size();++i) {
                    String l = sentence.getNerTags().get(i);
                    allLabels.add(l);
                    if (labelSet.contains(l)) {
                        entityCount+=1;
                        article.getSpatialEntities().put(sentence.getMentions().get(i), l);
                    }
                }
            }
            article.setEntityNum(entityCount);
            article.setRelationNum(relationCount);
            System.out.println(article.getArticleTitle()+":relation:"+article.getRelationNum()+article.getSpatialSignals().toString());
            System.out.println(article.getArticleTitle()+":entity:"+article.getEntityNum()+article.getSpatialEntities().toString());
            FileUtil.writeFile(outPath, Collections.singletonList(article.toString()), true);
        }
        System.out.println(allLabels);
    }


    private boolean isTextSimilar(String text1, String text2, int truncation) {
        if (text1.length() > text2.length()) {
            String tmp=text2;
            text2 = text1;
            text1 = tmp;
        }
        String str1= (text1.length()<truncation)? text1:text1.substring(0, truncation);
        String str2= (text2.length()<truncation)? text2:text2.substring(0, truncation);
        double lcs = TextSimilarity.getLongestCommonSubsequence(str1, str2);
        return  (lcs == str1.length() || text1.length()>50 && lcs > 4/5.0*str1.length());
    }


    private Set<Integer> findDuplicatedDoc(List<Article> docList) {
        docList = docList.stream()
                .sorted(Comparator.comparingInt(Article::length))
                .collect(Collectors.toList());
        Set<Integer> removedArticle=new HashSet<>();
//        docList = docList.subList(500, docList.size());
        for(int i=0; i<docList.size();i++) {
            Article a = docList.get(i);
            for (int j=i+1;j<docList.size();j++) {
                Article b = docList.get(j);
                if (a.getArticleTitle().equals(b.getArticleTitle())) {
                    removedArticle.add(a.getArticleId());
                    break;
                }
            }
        }
        docList=docList.stream()
                .filter(o->!removedArticle.contains(o.getArticleId()))
                .collect(Collectors.toList());
        for(int i=0; i<docList.size();i++) {
            System.out.println("index: " + i);
            Article a = docList.get(i);
            for (int j=i+1;j<docList.size();j++) {
                Article b = docList.get(j);

                if (a.length() > b.length())
                    System.out.println("error");

                double proportion = 1.0*b.length()/a.length();
                if (proportion >= 2)
                    break;
//                if (a.length() < 200 && proportion >= 2 ||
//                        a.length() >= 200 && a.length()<500 && proportion >= 1.2 ||
//                        a.length() >= 500  && proportion >= 1.1)
//                    break;
                if (isTextSimilar(a.getArticleContent(), b.getArticleContent(), 2500)){
                    removedArticle.add(a.getArticleId());
                    System.out.println("比值："+proportion + " article a:  " + a.getArticleId() + " article b:  " + b.getArticleId());
                    break;
                }
            }
        }
        return removedArticle;
    }


    public void processDuplicatedDoc(String inputPath, String outputPath) {
        Set<Integer> docIdSet = FileUtil.readLines(inputPath).stream()
                .map(Integer::valueOf).collect(Collectors.toSet());
        List<Article> docList = articleList.stream()
                .filter(o->docIdSet.contains(o.getArticleId()))
                .collect(Collectors.toList());
        Set<Integer> removedArticleIds = findDuplicatedDoc(docList);
        System.out.println(removedArticleIds.toString());
        List<String> output = docList.stream()
                .map(Article::getArticleId)
                .filter(removedArticleIds::contains)
                .map(String::valueOf)
                .collect(Collectors.toList());
        FileUtil.writeFile(outputPath, output, false);
    }

    public void processDuplicatedDoc(){
        System.out.println(articleList.size());
//        Map<String, List<Integer>> group=articleList.stream()
//                .collect(Collectors.groupingBy(Article::getArticleTitle, Collectors.mapping(Article::getArticleId, Collectors.toList())));
//
//        group = group.entrySet().stream()
//                .filter(x->x.getValue().size() > 1)
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//
//        AtomicInteger count = new AtomicInteger();
//        group.forEach((key,value)-> System.out.println(key+value));
//
//        group.forEach((key, value)-> {
//            count.addAndGet(value.size());
//        });

        Set<Integer> removedArticle=new HashSet<>();

//        System.out.println(articleList.get(16440).getArticleContent().length());

        for (int i = 48000;i<articleList.size();i++) {
            Article a = articleList.get(i);
            for (int j = i + 1; j < articleList.size(); j++) {
                Article b = articleList.get(j);
                if (a.getArticleTitle().equals(b.getArticleTitle())) {
                    removedArticle.add(a.getArticleId());
//                    System.out.println("same");
//                    System.out.println(a.getArticleContent());
//                    System.out.println(b.getArticleContent());
                    break;
                }
            }
        }

        articleList = articleList.stream().filter(x -> !removedArticle.contains(x.getArticleId())).collect(Collectors.toList());
        System.out.println(articleList.size());


        for (int i = 48000;i<articleList.size();i++) {
            Article a = articleList.get(i);
            for (int j = i + 1;j<articleList.size();j++) {
                Article b = articleList.get(j);
                double proportion = a.length() > b.length() ? 1.0*a.length()/b.length() : 1.0*b.length()/a.length();
                if (a.length() < 200 && proportion >= 2 ||
                        a.length() >= 200 && a.length()<500 && proportion >= 1.2 ||
                        a.length() >= 500  && proportion >= 1.1)
                    break;
                if (isTextSimilar(a.getArticleContent(), b.getArticleContent(), 250)){
                    removedArticle.add(a.getArticleId());
                    System.out.println("比值："+proportion);
                    break;
                }
//                if (j % 1000 == 0)
//                    System.out.println(i + " " + j);
            }
            if (i % 10 == 0)
                System.out.println(i + " " + removedArticle.size());
            if (i % 2000 == 0)
                System.out.println("removed articles:" + removedArticle.toString());
        }
        System.out.println("removed articles:" + removedArticle.toString());




//        group.forEach((b, value) -> articleList.stream()
//                .filter(y -> {
//                    String a = y.getArticleTitle();
//                    return !a.equals(b) && TextSimilarity.getlongestCommonSubsequence(a, b) > 20;
//                })
//                .forEach(z -> System.out.println(b + ":" + z.getArticleTitle())));

//        for (Article article: articleList) {
//
//            if (article.getArticleTitle().equals())
//        }
//        for(Map.Entry<String, List<Integer>> entry: group.entrySet()) {
//            if (entry.getValue().size() > 1) {
//                System.out.println(entry.getKey() + " " + entry.getValue());
//            }
//        }
    }

    public void filterDuplicatedDoc() {
        Set<String> tmp = new HashSet<>();
        tmp.addAll(FileUtil.readFileByLine("data/result/0-16000.txt", 0));
        tmp.addAll(FileUtil.readFileByLine("data/result/16000-46000.txt", 0));
        Set<Integer> duplicatedId = tmp.stream().map(Integer::valueOf).collect(Collectors.toSet());
        List<Article> newArticleList = new ArrayList<>();
        articleList.forEach( x -> {
            if (!duplicatedId.contains(x.getArticleId())) {
                newArticleList.add(x);
            }
        });
        String s=JSON.toJSONString(newArticleList);
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(new File("data/corpus/new_Les_MMRC.json")))) {
            bw.write(s);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void analyze() {
        List<String []> lines = FileUtil.readFileByLine("data/result/statistics_2.txt", "\t");
        Set<Integer> allArticleId=articleList.stream().map(Article::getArticleId).collect(Collectors.toSet());

        System.out.println(lines.size());
        lines = lines.stream()
                .filter((String []x) -> allArticleId.contains(Integer.valueOf(x[0])) && Integer.valueOf(x[2])<20000)
                .collect(Collectors.toList());
        System.out.println(lines.size());
//        Comparator<String []> comparator = ((x,y)->Integer.valueOf(x[3]).compareTo(Integer.valueOf(y[3])));
        Comparator<String []> comparator1 = (Comparator.comparing(x -> Integer.valueOf(x[3])));
        Comparator<String []> comparator2 = (Comparator.comparing(x -> Integer.valueOf(x[4])));
        Comparator<String []> comparator3 = (Comparator.comparing(x -> Integer.parseInt(x[3])*Integer.parseInt(x[4])));

        lines = lines.stream().filter(x->!x[3].equals("0")||!x[4].equals("0")).collect(Collectors.toList());

//        .map(x -> String.join("\t", x))

        System.out.println(lines.size());
        List<String []> linesSortedBySignal = lines.stream()
                .sorted(comparator1.reversed())
                .collect(Collectors.toList());
        List<String []> linesSortedByEntity = lines.stream()
                .sorted(comparator2.reversed())
                .collect(Collectors.toList());
        List<String []> linesSortedByBoth = lines.stream()
                .sorted(comparator3.reversed())
                .collect(Collectors.toList());
        Set<String> ids1= linesSortedBySignal.subList(0,5000).stream()
                .map(x->x[0]).collect(Collectors.toSet());
        Set<String> ids2= linesSortedByEntity.subList(0,5000).stream()
                .map(x->x[0]).collect(Collectors.toSet());
        Set<String> ids3= linesSortedByBoth.subList(0,5000).stream()
                .map(x->x[0]).collect(Collectors.toSet());

        List<String> inter = ids1.stream().filter(x -> ids2.contains(x) && ids3.contains(x)).collect(Collectors.toList());
        System.out.println(inter.size());
        inter = ids1.stream().filter(ids3::contains).collect(Collectors.toList());
        System.out.println(inter.size());
        inter = ids2.stream().filter(ids3::contains).collect(Collectors.toList());
        System.out.println(inter.size());
        inter = ids1.stream().filter(ids2::contains).collect(Collectors.toList());
        System.out.println(inter.size());

        List<String []> mergeList = linesSortedBySignal.subList(0,4000);
        mergeList.addAll(linesSortedByEntity.subList(0,3000));
        mergeList = mergeList.stream().distinct()
                .sorted(comparator1.reversed().thenComparing(comparator2.reversed()))
                .collect(Collectors.toList());
        System.out.println(mergeList.size());
//        FileUtil.writeFile("data/corpus/sortbysignal.txt", linesSortedBySignal, false);
//        FileUtil.writeFile("data/corpus/sortbyentity.txt", linesSortedByEntity, false);
//        FileUtil.writeFile("data/corpus/sortbyboth.txt", linesSortedByBoth, false);

        FileUtil.writeFile("data/result/地点筛选结果.txt", mergeList.stream().map(x -> String.join("\t", x)).collect(Collectors.toList()), false);
        List<String> ids = mergeList.stream().map(x -> x[0]).collect(Collectors.toList());
        FileUtil.writeFile("data/result/地点.txt",ids, false);
    }



    public void merge() {
        List<String> place = FileUtil.readFileByLine("data/result/时间.txt", 0);
        List<String> time = FileUtil.readFileByLine("data/result/地点.txt", 0);
        List<String> entity = FileUtil.readFileByLine("data/result/实体.txt", ",", 0);
        place.retainAll(time);
        place.retainAll(entity);
        Set<String> s=new HashSet<>(place);
        FileUtil.writeFile("data/result/new_common_3.txt",s, false);
    }

    //    public void saveAnalyzeResult(){
//        List<String> lines = articleList.stream().map(Article::toString).collect(Collectors.toList());
//        FileUtil.
//    }

    public void generateRawDocuments() {
        Set<Integer> docIdSet=FileUtil.readLines("data/result/new_common_3.txt")
                .stream().map(Integer::valueOf).collect(Collectors.toSet());

        List<Article> articles =  articleList.stream()
                .filter(o->docIdSet.contains(o.getArticleId()))
                .sorted(Comparator.comparingInt(Article::getArticleId))
                .collect(Collectors.toList());

        Set<String> questionMarks=new TreeSet<>();

        articles.forEach(article -> {
            String filename="data/raw_document/" + article.getArticleId() + ".txt";
            String title = article.getArticleTitle().replaceAll("\\s+", " ");
            String content = article.getArticleContent().replaceAll("\\s+", " ");
            String doc = title + "\n" + content;
            StringBuilder newContent = new StringBuilder();
            String [] phrases = doc.split("\n");
            List<Sentence> sentences = new ArrayList<>();
            for (String phrase: phrases) {
                List<Sentence> list = stanfordnlp.getSentences(phrase);
                sentences.addAll(list);
            }
            for (int i = 0; i < sentences.size(); i++) {
                Sentence sentence = sentences.get(i);
                String sentenceText = sentence.getText();
                if (sentence.getText().endsWith("？") && i < sentence.size() - 1) {
                    questionMarks.add(filename);
                    Sentence nextSentence = sentences.get(i + 1);
                    List<String> m1 = sentence.getNerTags();
                    List<String> m2 = nextSentence.getNerTags();
                    if (m1.get(m1.size() - 2).equals("PERSON") && m2.get(0).equals("PERSON")) {
                        sentenceText = sentenceText.substring(0, sentenceText.length() - 1) + "·";
                        nextSentence.setText(sentenceText + nextSentence.getText());
                        System.out.println(sentenceText);
                        System.out.println(nextSentence.getText());
                        System.out.println(filename);
                        continue;
                    }
                }
                newContent.append(sentenceText).append("\n");
            }

            FileUtil.writeFile(filename, newContent.toString(), false);
        });
        FileUtil.writeFile("data/result/question_mark.txt", questionMarks, false);

//        articles.stream().sorted(Comparator.comparingInt(Article::length)).forEach(o-> System.out.println(o.length() + " " + o.getArticleId()));

    }


    // 对语料进行预标注
    public  void generateDocuments(String dirPath, String  ann_dir, int num) {
        File folder = new File("data/raw_document");
        List<File> files = Arrays.asList(Objects.requireNonNull(folder.listFiles()));

        List<Article> articles = files.stream().map(file -> {
            String filename = file.getName().replace(".txt", "");
            String articleContent = FileUtil.readFile(file.getPath());
            return new Article(Integer.valueOf(filename),"","",articleContent);
        })
                .sorted(Comparator.comparingInt(Article::getArticleId))
                .collect(Collectors.toList()).subList(0, num);


        List<String> idxToIdMap = new ArrayList<>();
        for (int i=0; i<articles.size();i++) {
            idxToIdMap.add(i + "\t" + articles.get(i).getArticleId());
        }
        FileUtil.writeFile("data/result/idxToId.txt", idxToIdMap, false);
        PostProcess postProcess = new PostProcess();
        Map<String,BratEntity> strToEntity = postProcess.getAllMentionType(ann_dir);
        List<String> taggedMentions = strToEntity.keySet().stream()
                .sorted(Comparator.comparingInt(String::length).reversed()).collect(Collectors.toList());
        Map<String, String> nerTagMap = BratUtil.nerTagMap;
        DecimalFormat df = new DecimalFormat("0000");

//        IntStream.range(0, articles.size()).boxed().forEach(System.out::println);


        IntStream.range(0, articles.size()).forEach(idx -> {

            String path = dirPath + '/' + String.valueOf((idx/50)*50) + '-' + String.valueOf((idx/50)*50+49) + '/';
            FileUtil.createDir(path);
            String baseFilename=path + "/" + df.format(idx);
            String textFilename = baseFilename+ ".txt";
            String annFilename = baseFilename+ ".ann";
            String content = articles.get(idx).getArticleContent();
            int offset, tid=1, aid=1, noteId=1, baseIdx;
            List<String> triples = new ArrayList<>();
//            List<Object> tripleList = new ArrayList<>();
            StringBuilder newContent = new StringBuilder();

            String [] phrases = content.split("\n");
            for (String phrase: phrases) {
                List<Sentence> sentences = stanfordnlp.getSentences(phrase);
//                List<Sentence> sentences = new ArrayList<Sentence>(){{add(new Sentence(phrase));}};
//                List<Sentence> sentences = stanfordnlp.getSentences("《 人民日报 》（ 2018年01月06日 03 版）");
                for (Sentence sentence: sentences) {
                    baseIdx = offset = newContent.length();
                    String sentenceText = sentence.getText();
                    Set<Integer> offsetList = new HashSet<>(sentence.getOffsets());
                    IntervalTree<String> intervalTree = new IntervalTree<>();

                    for (String str: taggedMentions) {
                        if (BratUtil.isTimeTag(strToEntity.get(str).getTag()))
                            continue;
                        List<Integer> indexList = MyStringUtils.indexOfAllNoOverlap(sentenceText, str);
                        if (indexList.size() > 0) {
                            BratEntity bratEntity = strToEntity.get(str);
                            for (int index: indexList) {
                                int end = index+str.length();
//
                                if (!offsetList.contains(index) || !offsetList.contains(end) || intervalTree.overlap(index, end))
                                    continue;
                                BratEntity bratEntity1 = new BratEntity("T"+(tid++), str, bratEntity.getTag(), index + baseIdx,end+baseIdx);
                                intervalTree.addInterval(index, end, bratEntity1.getId());
                                for (BratAttribute bratAttribute : bratEntity.getBratAttributes()) {
                                    BratAttribute attr = new BratAttribute("A"+(aid++), bratAttribute.getName(), bratAttribute.getValue(), "");
                                    bratEntity1.addAttribute(attr);
                                }
                                triples.addAll(bratEntity1.toStringList());
                            }
                        }
                    }
                    Queue<Integer> blankIndexes = new LinkedList<>();
                    for (int i = 0; i < sentenceText.length(); i++) {
                        if (sentenceText.charAt(i) == ' ')
                            blankIndexes.offer(i + offset);
                    }

                    int sentence_length = 0;

                    List<String> tokens = sentence.getTokens();
                    List<String> mentions = sentence.getMentions();
                    List<String> mention_types=sentence.getNerTags();
                    List<String> norms = sentence.getNormalizations();
                    for (int i=0; i < mention_types.size();++i) {
                        String label = mention_types.get(i),word = mentions.get(i), norm = norms.get(i), tag=nerTagMap.get(label);

                        while (!blankIndexes.isEmpty() && blankIndexes.element() >= offset
                                &&blankIndexes.element() <= word.length() + offset ) {
                            Integer blank_pos = blankIndexes.poll();
                            StringBuilder sb = new StringBuilder(word);
                            if (blank_pos != null) {
                                sb.insert(blank_pos-offset, " ");
                                word = sb.toString();
                            }
                        }
//                        word = word.trim();
                        if (!intervalTree.overlap(offset-baseIdx, offset-baseIdx+word.length())
                                &&(strToEntity.containsKey(word) || tag != null)) {
//                            intervalTree.addInterval(offset-baseIdx, offset-baseIdx+word.length(), "");
                            String tidStr =  "T"+(tid++);
                            BratEntity bratEntity = new BratEntity(tidStr, word, tag, offset, offset+word.length());
                            if (strToEntity.containsKey(word)) {
                                BratEntity tmp = strToEntity.get(word);
                                bratEntity.setTag(tmp.getTag());
                                for (BratAttribute bratAttribute : tmp.getBratAttributes()) {
                                    bratAttribute.setId("A"+(aid++));
                                    bratEntity.addAttribute(bratAttribute);
                                }
                                if (tmp.getBratNote()!=null) {
                                    bratEntity.setBratNote(new BratNote("#"+(noteId++), tmp.getBratNote().getNote(), tidStr));
                                }
                            } else if (tag.equals(BratUtil.COUNTRY) || tag.equals(BratUtil.PLACE) || tag.equals(BratUtil.ADMIN_DIV)) {
                                bratEntity.getBratAttributes().add(new BratAttribute("A"+(aid++), "form", "NAM", tidStr));
                                if (label.equals("FACILITY")) {
                                    bratEntity.getBratAttributes().add (new BratAttribute("A"+(aid++), "place_type", "Facility", tidStr));
                                }
                            } else if(tag.equals("Time") || tag.equals("Date") && norm.length() > 0) {
                                BratNote bratNote = new BratNote("#"+(noteId++), "val="+ norm, tidStr);
                                bratEntity.setBratNote(bratNote);
                            }
                            triples.addAll(bratEntity.toStringList());
                        }

                        sentence_length+=word.length();
                        offset+=word.length();
                    }
                    if (sentence.getText().length() != sentence_length)
                        System.out.println(sentence.getText() + " "+sentence.getTokens().toString());
                    newContent.append(sentence.getText()).append("\n");
                }
//                    newContent.append("\n");
            }
            FileUtil.writeFile(textFilename, newContent.toString(), false);
            FileUtil.writeFile(annFilename, triples, false);
        });
    }


    public  void generateOtherDocuments(String in_dir, String out_dir, String  ann_dir, int num) {
        File folder = new File(in_dir);
        List<File> files = Arrays.asList(Objects.requireNonNull(folder.listFiles()));

        List<Article> articles = files.stream().map(file -> {
            String filename = file.getName().replace(".txt", "");
            String articleContent = FileUtil.readFile(file.getPath());
            return new Article(Integer.valueOf(filename),"","",articleContent);
        })
                .sorted(Comparator.comparingInt(Article::getArticleId))
                .collect(Collectors.toList()).subList(0, num);

        PostProcess postProcess = new PostProcess();
        Map<String,BratEntity> strToEntity = postProcess.getAllMentionType(ann_dir);
        List<String> taggedMentions = strToEntity.keySet().stream()
                .sorted(Comparator.comparingInt(String::length).reversed()).collect(Collectors.toList());
        Map<String, String> nerTagMap = BratUtil.nerTagMap;
        DecimalFormat df = new DecimalFormat("0000");

//        IntStream.range(0, articles.size()).boxed().forEach(System.out::println);


        IntStream.range(0, articles.size()).forEach(idx -> {

            String path = out_dir + '/' + String.valueOf((idx/50)*50) + '-' + String.valueOf((idx/50)*50+49) + '/';
            FileUtil.createDir(path);
            String baseFilename=path + "/" + df.format(idx);
            String textFilename = baseFilename+ ".txt";
            String annFilename = baseFilename+ ".ann";
            String content = articles.get(idx).getArticleContent();
            int offset, tid=1, aid=1, noteId=1, baseIdx;
            List<String> triples = new ArrayList<>();
//            List<Object> tripleList = new ArrayList<>();
            StringBuilder newContent = new StringBuilder();

            String [] phrases = content.split("\n");
            for (String phrase: phrases) {
                List<Sentence> sentences = stanfordnlp.getSentences(phrase);
//                List<Sentence> sentences = new ArrayList<Sentence>(){{add(new Sentence(phrase));}};
//                List<Sentence> sentences = stanfordnlp.getSentences("《 人民日报 》（ 2018年01月06日 03 版）");
                for (Sentence sentence: sentences) {
                    baseIdx = offset = newContent.length();
                    String sentenceText = sentence.getText();
                    Set<Integer> offsetList = new HashSet<>(sentence.getOffsets());
                    IntervalTree<String> intervalTree = new IntervalTree<>();

                    for (String str: taggedMentions) {
                        if (BratUtil.isTimeTag(strToEntity.get(str).getTag()))
                            continue;
                        List<Integer> indexList = MyStringUtils.indexOfAllNoOverlap(sentenceText, str);
                        if (indexList.size() > 0) {
                            BratEntity bratEntity = strToEntity.get(str);
                            for (int index: indexList) {
                                int end = index+str.length();
//
                                if (!offsetList.contains(index) || !offsetList.contains(end) || intervalTree.overlap(index, end))
                                    continue;
                                BratEntity bratEntity1 = new BratEntity("T"+(tid++), str, bratEntity.getTag(), index + baseIdx,end+baseIdx);
                                intervalTree.addInterval(index, end, bratEntity1.getId());
                                for (BratAttribute bratAttribute : bratEntity.getBratAttributes()) {
                                    BratAttribute attr = new BratAttribute("A"+(aid++), bratAttribute.getName(), bratAttribute.getValue(), "");
                                    bratEntity1.addAttribute(attr);
                                }
                                triples.addAll(bratEntity1.toStringList());
                            }
                        }
                    }
                    Queue<Integer> blankIndexes = new LinkedList<>();
                    for (int i = 0; i < sentenceText.length(); i++) {
                        if (sentenceText.charAt(i) == ' ')
                            blankIndexes.offer(i + offset);
                    }

                    int sentence_length = 0;

                    List<String> tokens = sentence.getTokens();
                    List<String> mentions = sentence.getMentions();
                    List<String> mention_types=sentence.getNerTags();
                    List<String> norms = sentence.getNormalizations();
                    for (int i=0; i < mention_types.size();++i) {
                        String label = mention_types.get(i),word = mentions.get(i), norm = norms.get(i), tag=nerTagMap.get(label);

                        while (!blankIndexes.isEmpty() && blankIndexes.element() >= offset
                                &&blankIndexes.element() <= word.length() + offset ) {
                            Integer blank_pos = blankIndexes.poll();
                            StringBuilder sb = new StringBuilder(word);
                            if (blank_pos != null) {
                                sb.insert(blank_pos-offset, " ");
                                word = sb.toString();
                            }
                        }
//                        word = word.trim();
                        if (!intervalTree.overlap(offset-baseIdx, offset-baseIdx+word.length())
                                &&(strToEntity.containsKey(word) || tag != null)) {
//                            intervalTree.addInterval(offset-baseIdx, offset-baseIdx+word.length(), "");
                            String tidStr =  "T"+(tid++);
                            BratEntity bratEntity = new BratEntity(tidStr, word, tag, offset, offset+word.length());
                            if (strToEntity.containsKey(word)) {
                                BratEntity tmp = strToEntity.get(word);
                                bratEntity.setTag(tmp.getTag());
                                for (BratAttribute bratAttribute : tmp.getBratAttributes()) {
                                    bratAttribute.setId("A"+(aid++));
                                    bratEntity.addAttribute(bratAttribute);
                                }
                                if (tmp.getBratNote()!=null) {
                                    bratEntity.setBratNote(new BratNote("#"+(noteId++), tmp.getBratNote().getNote(), tidStr));
                                }
                            } else if (tag.equals(BratUtil.COUNTRY) || tag.equals(BratUtil.PLACE) || tag.equals(BratUtil.ADMIN_DIV)) {
                                bratEntity.getBratAttributes().add(new BratAttribute("A"+(aid++), "form", "NAM", tidStr));
                                if (label.equals("FACILITY")) {
                                    bratEntity.getBratAttributes().add (new BratAttribute("A"+(aid++), "place_type", "Facility", tidStr));
                                }
                            } else if(tag.equals("Time") || tag.equals("Date") && norm.length() > 0) {
                                BratNote bratNote = new BratNote("#"+(noteId++), "val="+ norm, tidStr);
                                bratEntity.setBratNote(bratNote);
                            }
                            triples.addAll(bratEntity.toStringList());
                        }

                        sentence_length+=word.length();
                        offset+=word.length();
                    }
                    if (sentence.getText().length() != sentence_length)
                        System.out.println(sentence.getText() + " "+sentence.getTokens().toString());
                    newContent.append(sentence.getText()).append("\n");
                }
//                    newContent.append("\n");
            }
            FileUtil.writeFile(textFilename, newContent.toString(), false);
            FileUtil.writeFile(annFilename, triples, false);
        });
    }



    public  void generateDocuments() {
        List<String> tmp = FileUtil.readFileByLine("data/result/new_common_3.txt", 0);
//        tmp = tmp.subList(0,10);
        List<Integer> articleIds = tmp.stream().map(Integer::valueOf).distinct()
                .sorted(Comparator.comparingInt(Integer::intValue))
                .collect(Collectors.toList());
//        List<Integer> mapArticleIDs= IntStream.range(0, articleIds.size()).boxed().collect(Collectors.toList());
//        Map<Integer, Integer> id_map = IntStream.range(0, articleIds.size()).boxed().collect(Collectors.toMap(x->x, articleIds::get));
        List<String> mapIdsStrs = IntStream.range(0, articleIds.size()).boxed()
                .map(x -> String.valueOf(x) + '\t' + String.valueOf(articleIds.get(x)))
                .collect(Collectors.toList());
        FileUtil.writeFile("data/result/idxToIDs.txt", mapIdsStrs, false);
        DecimalFormat df = new DecimalFormat("0000");
        articleList.forEach(article -> {
            if (articleIds.contains(article.getArticleId())) {
                String baseFilename="data/document/" + df.format(articleIds.indexOf(article.getArticleId()));
                if (articleIds.indexOf(article.getArticleId()) == 6)
                    System.out.println(1);
//                String baseFilename="data/document/" + article.getArticleId() + "_"
//                        + article.getArticleTitle().replaceAll("[|\\s'\"/{}()*#!?\n\t:]", "_");
                String textFilename = baseFilename+ ".txt";
                String annFilename = baseFilename+ ".ann";
//                String content = article.getArticleTitle().replaceAll("\\s+", " ") + "\n" +
//                        article.getArticleContent().replaceAll("\\s+", " ");
                String title = article.getArticleTitle().replaceAll("\\s+", " ");
                String content = article.getArticleContent().replaceAll("\\s+", " ");
                String doc = title + "\n" + content;
//                content = content.replaceAll("\\s+", " ");
                int offset;
                int tid=1;
                int aid=1;
                int noteId=1;

//        String a =[LOCATION, NATIONALITY, NUMBER, IDEOLOGY, MONEY, PERSON, FACILITY, MISC, TIME, ORDINAL,
//        CAUSE_OF_DEATH, O, STATE_OR_PROVINCE, ORGANIZATION, DATE, COUNTRY, CITY, RELIGION, PERCENT, DEMONYM, TITLE, GPE, CRIMINAL_CHARGE]
//        Map<Integer, Double> relevance = new HashMap<>();
//                Set<String> allLabels = new HashSet<>();
//                String [] labels = {"LOCATION","GPE", "COUNTRY","STATE_OR_PROVINCE","COUNTRY","CITY", "FACILITY","DEMONYM", "NATIONALITY"};
//                Set<String> labelSet = new HashSet<String>(){{addAll(Arrays.asList(labels));}};
                List<String> triples = new ArrayList<>();
                StringBuilder newContent = new StringBuilder();

                String [] phrases = doc.split("\n");
                for (String phrase: phrases) {
                    List<Sentence> sentences = stanfordnlp.getSentences(phrase);
                    for (Sentence sentence:sentences) {
                        offset = newContent.length();
                        String sentenceText = sentence.getText();
                        Queue<Integer> blankIndexes = new LinkedList<>();
                        for (int i = 0; i < sentenceText.length(); i++) {
                            if (sentenceText.charAt(i) == ' ')
                                blankIndexes.offer(i + offset);
                        }
                        System.out.println(sentence.getText());
                        int sentence_length = 0;
                        List<String> mentions = sentence.getMentions();
                        List<String> mention_types=sentence.getNerTags();
                        List<String> norms = sentence.getNormalizations();
                        for (int i=0; i < mention_types.size();++i) {
                            String label = mention_types.get(i);
                            String word = mentions.get(i);
                            String norm = norms.get(i);
                            String tag=null;
                            while (!blankIndexes.isEmpty() &&
                                    blankIndexes.element() <= word.length() + offset && blankIndexes.element() >= offset) {
                                Integer blank_pos = blankIndexes.poll();
                                StringBuilder sb = new StringBuilder(word);
                                if (blank_pos != null) {
                                    sb.insert(blank_pos-offset, " ");
                                    word = sb.toString();
                                }
                            }

                            switch (label){
                                case "PERSON":tag="Person";break;
                                case "ORGANIZATION":tag="Organization";break;
                                case "GPE": tag="AdministrativeDivision";break;
                                case "CITY": tag="AdministrativeDivision";break;
                                case "STATE_OR_PROVINCE":tag="AdministrativeDivision";break;
                                case "COUNTRY":tag="Country";break;
                                case "FACILITY":tag="Place";break;
                                case "LOCATION": tag="Place";break;
                                case "TIME": tag="Time";break;
                                case "DATE": tag="Date";break;
                                default:break;
                            }
//                        String [] location={"AdminDiv", "Facility", "Place", "Country"};
                            if (tag != null) {
//                            String [] triple = {"T"+tid, tag+" "+offset+" "+(offset+word.length()), word};
                                String tidStr="T"+(tid++);
                                String tripleStr=tidStr + "\t" + tag+" "+offset+" "+(offset+word.length()) + "\t"+word;
                                triples.add(tripleStr);

                                if (tag.equals("Country") ||tag.equals("Place")||tag.equals("AdministrativeDivision")){
                                    tripleStr="A"+(aid++) + "\t" + "form " +tidStr+ " NAM";
                                    triples.add(tripleStr);
//                                    tripleStr="A"+(aid++) + "\t" + "dimensionality " +tidStr+ " area";
//                                    triples.add(tripleStr);
                                    if (label.equals("FACILITY")) {
                                        tripleStr="A"+(aid++) + "\t" + "place_type " +tidStr+ " Facility";
                                        triples.add(tripleStr);
                                    }
                                }
                                norm = "val="+ norm;
                                if (tag.equals("Time") || tag.equals("Date") && norm.length() > 0) {
                                    tripleStr="#"+(noteId++) + "\t" + "AnnotatorNotes " +tidStr+ "\t" + norm;
                                    triples.add(tripleStr);
                                }
                            }
                            sentence_length+=word.length();
                            offset+=word.length();
                        }
                        if (sentence.getText().length() != sentence_length)
                            System.out.println(sentence.getText() + " "+sentence.getTokens().toString());
                        newContent.append(sentence.getText()).append("\n");
                    }
//                    newContent.append("\n");
                }
                FileUtil.writeFile(textFilename, newContent.toString(), false);
                FileUtil.writeFile(annFilename, triples, false);
            }
        });
    }



    void func() {
        List<String> fileList = FileUtil.readLines("data/result/question_mark.txt")
                .stream().map(o -> o.substring(o.lastIndexOf("/")+1)).collect(Collectors.toList());
        File folder = new File("data/raw_document_3");
        File[] files = folder.listFiles();
        assert files != null;
        for (File file: files) {
            if (!fileList.contains(file.getName()))
                FileUtil.deleteFile(file.getPath());
        }
    }
    public  static void main(String [] args) {
        DocSelection docSelection = new DocSelection();
//        docSelection.statistics();
//        docSelection.getAllDocSize();
//        docSelection.analyze();
//        docSelection.merge();
//        docSelection.processDuplicatedDoc();
//        docSelection.generateDocuments();
//        docSelection.filterDuplicatedDoc();

//        docSelection.processDuplicatedDoc("data/result/new_common_3.txt",
//                "data/result/new_common_3_filter.txt");
//        docSelection.func();
//        docSelection.generateRawDocuments();
//        docSelection.generateDocuments("data/document_3", "data/annotation/all", 400);


        docSelection.generateOtherDocuments("data/msra_data", "data/document_4",
                "data/annotation/msra",3);

//        DecimalFormat df = new DecimalFormat("0000");
//        System.out.println(df.format(4));
//
//        String str1 = FileUtil.readFile("data/document/0001.txt");
//        String str2 = FileUtil.readFile("data/document/0002.txt");
//        System.out.println(TextSimilarity.getlongestCommonSubsequence(str1, str2)*1.0 / str1.length());
//        System.out.println(TextSimilarity.getlongestCommonSubsequence(str1, str2)*1.0 / str2.length());
//        System.out.println(docSelection.isTextSimilar(str1, str2, 250));
//        System.out.println(docSelection.isTextSimilar(str1, str2, 500));
//        System.out.println(docSelection.isTextSimilar(str1, str2, 1000));
    }

}
