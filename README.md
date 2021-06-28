### 生成空间信息抽取数据集

#### SpaceEval数据集
- 空间关系抽取，SpRL格式， `spaceeval/GenerateSRLCourpus_new.java`
- 空间关系抽取，MHS格式， `spaceeval/GenerateMultiHeadCorpus.java`
- 空间关系抽取，OpenNRE格式， `spaceeval/GenerateOpenNRECorpus.java`
- 空间关系抽取，二元关系抽取格式， `spaceeval/GenerateRelationCorpus.java`
- 空间元素识别， `spaceeval/GenerateNERCorpus.java`

#### SemEval2012 SpRL数据集
- 空间关系抽取，MHS格式， `msprl/GenerateMHSCorpus.java`
- 空间元素识别，`msprl/GenerateNERCorpus.java`

#### CLEF2017 mSpRL数据集
- 空间关系抽取，MHS格式， `sprl/GenerateMHSCorpus.java`
- 空间元素识别，`sprl/GenerateNERCorpus.java`

### 基于规则的关系抽取
GetRelation_SRL_new: 新的SRL格式输出

生成SRL格式输出：GetRelation_SRL_new.java内buildtags函数

resource/relation/: 词表文件

输入输出文件指定：
```$xslt
    static String inputdir = "data/SpaceEval2015/processed_data/SRL/QSNoTrigger/";
    static String outputdir = inputdir.replaceFirst("data", "output");
    static String filename = "train.txt";
```