package com.github.walterfan.kanban.domain;

import java.sql.Timestamp;
import java.util.Map;
import java.util.TreeMap;


public class Goal extends BaseObject {
    
    public final static int GOALTYPE_LONG = 1;
    
    public final static int GOALTYPE_MEDIUM = 2;
    
    public final static int GOALTYPE_SHORT = 3;
    
    public final static int GOALTYPE_MONTHLY = 4;
    
    public final static int GOALTYPE_WEEKLY = 5;
    
    public final static int GOALTYPE_DAILY = 6;
    
    private int goalID;
    private String goalName;
    private int goalType;
    private String description;
    private int categoryID;
    private int userID;
    private Timestamp createTime;
    private Timestamp endTime;
    private Timestamp deadline;
    public int getGoalType() {
		return goalType;
	}

	public void setGoalType(int goalType) {
		this.goalType = goalType;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}
    
    /**
     * @return the goalID
     */
    public int getGoalID() {
        return goalID;
    }
    
    /**
     * @param goalID the goalID to set
     */
    public void setGoalID(int goalID) {
        this.goalID = goalID;
    }
    
    /**
     * @return the goalName
     */
    public String getGoalName() {
        return goalName;
    }
    
    /**
     * @param goalName the goalName to set
     */
    public void setGoalName(String goalName) {
        this.goalName = goalName;
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
    


    
    public Timestamp getDeadline() {
		return deadline;
	}

	public void setDeadline(Timestamp deadline) {
		this.deadline = deadline;
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
    
    public static Map<Integer, String> getGoalTypes() {
        Map<Integer, String> map = new TreeMap<Integer, String>();
        map.put(GOALTYPE_LONG,"GoalType.Long");
        map.put(GOALTYPE_MEDIUM,"GoalType.Medium");
        map.put(GOALTYPE_SHORT,"GoalType.Short");
        map.put(GOALTYPE_MONTHLY,"GoalType.Monthly");
        map.put(GOALTYPE_WEEKLY,"GoalType.Weekly");
        map.put(GOALTYPE_DAILY,"GoalType.Daily");
        return map;
    }

    
    /**
     * @return the endTime
     */
    public Timestamp getEndTime() {
        return endTime;
    }

    
    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    
}
