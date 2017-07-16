package com.github.walterfan.kanban.domain;

import java.util.Date;


public class Article extends BaseObject {
    private String topic;
    private String content;
    private Date createTime;
    private String keywords;
    private int userID;
    private int categoryID;
    private int articleID;
    
    /**
     * @return the keywords
     */
    public String getKeywords() {
        return keywords;
    }
    
    /**
     * @param keywords the keywords to set
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    
    /**
     * @return the topic
     */
    public String getTopic() {
        return topic;
    }

    
    /**
     * @param topic the topic to set
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    
    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    
    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }


    
    /**
     * @return the userID
     */
    public int getUserID() {
        return userID;
    }

    
    /**
     * @param userID the userID to set
     */
    public void setUserID(int userID) {
        this.userID = userID;
    }

    
    /**
     * @return the categoryID
     */
    public int getCategoryID() {
        return categoryID;
    }

    
    /**
     * @param categoryID the categoryID to set
     */
    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    
    /**
     * @return the articleID
     */
    public int getArticleID() {
        return articleID;
    }

    
    /**
     * @param articleID the articleID to set
     */
    public void setArticleID(int articleID) {
        this.articleID = articleID;
    }

    
    /**
     * @return the createTime
     */
    public Date getCreateTime() {
        return createTime;
    }

    
    /**
     * @param createTime the createTime to set
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
