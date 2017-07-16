package com.github.walterfan.kanban.domain;

import org.apache.ibatis.type.Alias;

import java.sql.Timestamp;

/**
 * @author walter
 * 
 */
@Alias("Category")
public class Category extends BaseObject {
    
    public final static int CATEGORY_TYPE_TASK = 1;
    
    public final static int CATEGORY_TYPE_LORE = 2;
    
    public final static int CATEGORY_TYPE_BOOK = 3;
    
    public final static int CATEGORY_TYPE_FRIEND = 4;
    
    public final static int CATEGORY_TYPE_SITE = 5;
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8081693669094881422L;
    
    private int categoryID;
    
    private String categoryName;
    
    private String description;
    
    private int categoryType;
    
    private Timestamp createTime;
    
    public Category() {
    	
    }
    
    public Category(String name) {
    	this.categoryName = name;
    }
    
    public Timestamp getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
    
    /**
     * @return the categoryID
     */
    public int getCategoryID() {
        return categoryID;
    }
    
    /**
     * @param categoryID
     *            the categoryID to set
     */
    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }
    
    /**
     * @return the categoryName
     */
    public String getCategoryName() {
        return categoryName;
    }
    
    /**
     * @param categoryName
     *            the categoryName to set
     */
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * @return the categoryType
     */
    public int getCategoryType() {
        return categoryType;
    }
    
    /**
     * @param categoryType
     *            the categoryType to set
     */
    public void setCategoryType(int categoryType) {
        this.categoryType = categoryType;
    }
    
}
