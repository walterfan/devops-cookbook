package com.github.walterfan.kanban.domain;

import java.sql.Timestamp;
import java.util.Map;
import java.util.TreeMap;

public class Task extends BaseObject {
	
    public final static int TASKTYPE_UNDEFINED = 0;

    public final static int TASKTYPE_NEXTACTION = 1;
    
    public final static int TASKTYPE_ONSCHEDULE = 2;
    
    public final static int TASKTYPE_WAITOTHERS = 3;   
    
    //-----------------------------------------------//
        
	private int taskID;
	
	private String taskName;
	
	private int priority;
	
	private Timestamp deadline;
	
	private int userID ;
	
	protected int repeatID = 0;
	   
	//unit is minute
	private int duration;
	
	private String description;
	
	private Timestamp beginTime;
	
	private Timestamp endTime;
	
	private int remindID;
    
    private Timestamp createTime = new Timestamp(System.currentTimeMillis());

	private int categoryID;
	
	private int contextID;
	
	private int taskType;
	
	private int energy;
	
	public Task() {
	    
	}
	
	
	public Task(Task aTask) {
        super();
        this.taskID = aTask.taskID;
        this.taskName = aTask.taskName;
        this.priority = aTask.priority;
        this.deadline = aTask.deadline;
        this.userID = aTask.userID;
        this.repeatID = aTask.repeatID;
        this.duration = aTask.duration;
        this.description = aTask.description;
        this.beginTime = aTask.beginTime;
        this.endTime = aTask.endTime;
        //this.remindTime = aTask.remindTime;
        this.createTime = aTask.createTime;
        this.categoryID = aTask.categoryID;
        this.contextID = aTask.contextID;
        this.taskType = aTask.taskType;
        this.energy = aTask.energy;
    }
	
    public int getContextID() {
		return contextID;
	}
	public void setContextID(int contextID) {
		this.contextID = contextID;
	}
	public int getTaskType() {
		return taskType;
	}
	public void setTaskType(int taskType) {
		this.taskType = taskType;
	}
	public int getEnergy() {
		return energy;
	}
	public void setEnergy(int energy) {
		this.energy = energy;
	}

	
	public int getTaskID() {
		return taskID;
	}
	public void setTaskID(int taskID) {
		this.taskID = taskID;
	}
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	

	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public Timestamp getDeadline() {
		return deadline;
	}
	public void setDeadline(Timestamp deadline) {
		this.deadline = deadline;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getDescription() {
		return description;
	}
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
     * @return the beginTime
     */
    public Timestamp getBeginTime() {
        return beginTime;
    }
    
    /**
     * @param beginTime the beginTime to set
     */
    public void setBeginTime(Timestamp beginTime) {
        this.beginTime = beginTime;
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
	
    public static Map<Integer, String> getTaskTypes() {
        Map<Integer, String> map = new TreeMap<Integer, String>();
        map.put(TASKTYPE_UNDEFINED,"TaskType.Undefined");
        map.put(TASKTYPE_NEXTACTION,"TaskType.NextAction");
        map.put(TASKTYPE_ONSCHEDULE,"TaskType.OnSchedule");
        map.put(TASKTYPE_WAITOTHERS,"TaskType.WaitOthers");
        
        return map;
    }
    
    /**
     * @return the repeatID
     */
    public int getRepeatID() {
        return repeatID;
    }
    
    /**
     * @param repeatID the repeatID to set
     */
    public void setRepeatID(int repeatID) {
        this.repeatID = repeatID;
    }


    
    /**
     * @return the remindID
     */
    public int getRemindID() {
        return remindID;
    }


    
    /**
     * @param remindID the remindID to set
     */
    public void setRemindID(int remindID) {
        this.remindID = remindID;
    }


    
    
    
}
