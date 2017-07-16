package com.github.walterfan.kanban.domain;

import java.sql.Timestamp;
import java.util.Map;
import java.util.TreeMap;


public class Item extends BaseObject {
    
    public final static int ITEMTYPE_STUFF = 0;
    public final static int ITEMTYPE_MAYBE = 1;
    public final static int ITEMTYPE_REFER = 2;
    //--------goal type---------
    public final static int ITEMTYPE_HEALTH = 3;
    public final static int ITEMTYPE_BIRTHDAY = 4;
    public final static int ITEMTYPE_MAINTAIN = 5;     
    public final static int ITEMTYPE_HOLIDAY = 6;
    public final static int ITEMTYPE_CLEAN = 7;
    public final static int ITEMTYPE_HAPPY = 8;
    public final static int ITEMTYPE_FINISH = 9;
    //----------------------------------------------
    private int itemID;
    private String itemName;
    private int itemType;
    private String description;
    private int categoryID;
    private int userID;
    private Timestamp createTime = new Timestamp(System.currentTimeMillis());
    private Timestamp updateTime = new Timestamp(System.currentTimeMillis());
    
    public int getItemType() {
		return itemType;
	}

	public void setItemType(int itemType) {
		this.itemType = itemType;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}
    
    /**
     * @return the itemID
     */
    public int getItemID() {
        return itemID;
    }
    
    /**
     * @param itemID the itemID to set
     */
    public void setItemID(int itemID) {
        this.itemID = itemID;
    }
    
    /**
     * @return the itemName
     */
    public String getItemName() {
        return itemName;
    }
    
    /**
     * @param itemName the itemName to set
     */
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * @return the createTime
     */
    public Timestamp getCreateTime() {
        return createTime;
    }
    
    /**
     * @param createTime the createTime to set
     */
    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
    
    /**
     * @return the updateTime
     */
    public Timestamp getUpdateTime() {
        return updateTime;
    }
    
    /**
     * @param updateTime the updateTime to set
     */
    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
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
    
    public static Map<Integer, String> getItemTypes() {
        Map<Integer, String> map = new TreeMap<Integer, String>();
 
        map.put(ITEMTYPE_STUFF,"ItemType.Stuff");
        map.put(ITEMTYPE_MAYBE,"ItemType.Maybe");
        map.put(ITEMTYPE_REFER,"ItemType.Refer");
        map.put(ITEMTYPE_HEALTH,"ItemType.Health");
        map.put(ITEMTYPE_BIRTHDAY,"ItemType.Birthday");
        map.put(ITEMTYPE_MAINTAIN,"ItemType.Maintain");
        map.put(ITEMTYPE_HOLIDAY,"ItemType.Holiday");
        map.put(ITEMTYPE_CLEAN,"ItemType.Clean");
        map.put(ITEMTYPE_HAPPY,"ItemType.Happy");
        return map;
    }
}
