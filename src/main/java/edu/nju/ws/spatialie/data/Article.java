package edu.nju.ws.spatialie.data;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.HashMap;
import java.util.Map;

public class Article {
    @JSONField(name="article_id")
    private int articleId;

    @JSONField(name="article_type")
    private String articleType;

    @JSONField(name="article_title")
    private String articleTitle;

    @JSONField(name="article_content")
    private String articleContent;

    @JSONField(serialize = false)
    private int entityNum = 0;

    @JSONField(serialize = false)
    private int relationNum = 0;

    @JSONField(serialize = false)
    private Map<String, String> spatialEntities=new HashMap<>();

    @JSONField(serialize = false)
    private Map<String, Integer> spatialSignals= new HashMap<>();

    public Article(int articleId, String articleType, String articleTitle, String articleContent) {
        this.articleId = articleId;
        this.articleType = articleType;
        this.articleContent = articleContent;
        this.articleTitle = articleTitle;
    }


    public int length() {
        return articleContent.length();
    }

    public void setEntityNum(int entityNum) {
        this.entityNum = entityNum;
    }

    public void setRelationNum(int relationNum) {
        this.relationNum = relationNum;
    }

    public int getRelationNum() {
        return relationNum;
    }

    public int getEntityNum() {
        return entityNum;
    }

    public String getArticleContent() {
        return articleContent;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public String getArticleType() {
        return articleType;
    }

    public int getArticleId() {
        return articleId;
    }

    public void setArticleContent(String articleContent) {
        this.articleContent = articleContent;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
    }

    public void setArticleType(String articleType) {
        this.articleType = articleType;
    }

    public void setSpatialSignals(Map<String, Integer> spatialSignals) {
        this.spatialSignals = spatialSignals;
    }

    public void setSpatialEntities(Map<String, String> spatialEntities) {
        this.spatialEntities = spatialEntities;
    }

    public Map<String, Integer> getSpatialSignals() {
        return spatialSignals;
    }

    public Map<String, String> getSpatialEntities() {
        return spatialEntities;
    }

    public String toString() {
        String [] prop = {String.valueOf(articleId), articleTitle, String.valueOf(articleContent.length()),
                String.valueOf(relationNum),String.valueOf(entityNum), spatialSignals.toString(),spatialEntities.toString()};
        return String.join("\t", prop);
    }
}
