package com.csu.sms.vo;

public class KnowledgeFavoriteVO {
    private Long knowledgeId;
    private String title;
    private String author;
    private String linkUrl;
    private String imagePath;
    private String remark;
    private String favoriteTime;
    // getter/setter
    public Long getKnowledgeId() { return knowledgeId; }
    public void setKnowledgeId(Long knowledgeId) { this.knowledgeId = knowledgeId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getFavoriteTime() { return favoriteTime; }
    public void setFavoriteTime(String favoriteTime) { this.favoriteTime = favoriteTime; }
} 