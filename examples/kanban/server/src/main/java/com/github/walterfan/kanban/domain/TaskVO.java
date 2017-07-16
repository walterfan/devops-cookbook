package com.github.walterfan.kanban.domain;

import java.sql.Timestamp;


/**
 * @author walter
 * represent object of repeat info on presentaion layer
 *
 */
public class TaskVO extends RepeatVO {

    //---------task
    private int taskID;
    
    private String taskName;
    
    private int priority;
    
    private Timestamp deadline;
    
    private int userID ;

    private int duration;
    
    private String description;
    
    private Timestamp beginTime;
    
    private Timestamp endTime;
      
    private Timestamp createTime;

    private int categoryID;
    
    private int contextID;
    
    private int taskType;
    
    private int energy;
    
    //---------remind
    private boolean isRemind;
    
    private int remindID;
    
    private String remindMethod;
    
    private Timestamp remindTime;
    
    private int status;
    //---------repeat
    private int repeatID;
    
    private boolean isRepeat;
    
    private String repeatType;
    
    private int repeatInterval;
    
    private String repeatEndType;
    
    private Timestamp repeatBeginTime;
    
    private Timestamp repeatEndTime;
    
    private int repeatTimes;
    
    /**
     * @return the isRepeat
     */
    public boolean isRepeat() {
        return isRepeat;
    }
    
    /**
     * @param isRepeat the isRepeat to set
     */
    public void setRepeat(boolean isRepeat) {
        this.isRepeat = isRepeat;
    }
    
    
    /**
     * @return the repeatInterval
     */
    public int getRepeatInterval() {
        return repeatInterval;
    }
    
    /**
     * @param repeatInterval the repeatInterval to set
     */
    public void setRepeatInterval(int repeatInterval) {
        this.repeatInterval = repeatInterval;
    }


    
    /**
     * @return the repeatTimes
     */
    public int getRepeatTimes() {
        return repeatTimes;
    }
    
    /**
     * @param repeatTimes the repeatTimes to set
     */
    public void setRepeatTimes(int repeatTimes) {
        this.repeatTimes = repeatTimes;
    }

    
    /**
     * @return the repeatType
     */
    public String getRepeatType() {
        return repeatType;
    }

    
    /**
     * @param repeatType the repeatType to set
     */
    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }

    
    /**
     * @return the repeatEndType
     */
    public String getRepeatEndType() {
        return repeatEndType;
    }

    
    /**
     * @param repeatEndType the repeatEndType to set
     */
    public void setRepeatEndType(String repeatEndType) {
        this.repeatEndType = repeatEndType;
    }

    
    /**
     * @return the repeatEndTime
     */
    public Timestamp getRepeatEndTime() {
        return repeatEndTime;
    }

    
    /**
     * @param repeatEndTime the repeatEndTime to set
     */
    public void setRepeatEndTime(Timestamp repeatEndTime) {
        this.repeatEndTime = repeatEndTime;
    }

    
    /**
     * @return the repeatBeginTime
     */
    public Timestamp getRepeatBeginTime() {
        return repeatBeginTime;
    }

    
    /**
     * @param repeatBeginTime the repeatBeginTime to set
     */
    public void setRepeatBeginTime(Timestamp repeatBeginTime) {
        this.repeatBeginTime = repeatBeginTime;
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
     * @return the taskID
     */
    public int getTaskID() {
        return taskID;
    }

    
    /**
     * @param taskID the taskID to set
     */
    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }

    
    /**
     * @return the taskName
     */
    public String getTaskName() {
        return taskName;
    }

    
    /**
     * @param taskName the taskName to set
     */
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    
    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    
    /**
     * @param priority the priority to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    
    /**
     * @return the deadline
     */
    public Timestamp getDeadline() {
        return deadline;
    }

    
    /**
     * @param deadline the deadline to set
     */
    public void setDeadline(Timestamp deadline) {
        this.deadline = deadline;
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
     * @return the duration
     */
    public int getDuration() {
        return duration;
    }

    
    /**
     * @param duration the duration to set
     */
    public void setDuration(int duration) {
        this.duration = duration;
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
     * @return the contextID
     */
    public int getContextID() {
        return contextID;
    }

    
    /**
     * @param contextID the contextID to set
     */
    public void setContextID(int contextID) {
        this.contextID = contextID;
    }

    
    /**
     * @return the taskType
     */
    public int getTaskType() {
        return taskType;
    }

    
    /**
     * @param taskType the taskType to set
     */
    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    
    /**
     * @return the energy
     */
    public int getEnergy() {
        return energy;
    }

    
    /**
     * @param energy the energy to set
     */
    public void setEnergy(int energy) {
        this.energy = energy;
    }

    
    /**
     * @return the isRemind
     */
    public boolean isRemind() {
        return isRemind;
    }

    
    /**
     * @param isRemind the isRemind to set
     */
    public void setRemind(boolean isRemind) {
        this.isRemind = isRemind;
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

    
    /**
     * @return the remindMethod
     */
    public String getRemindMethod() {
        return remindMethod;
    }

    
    /**
     * @param remindMethod the remindMethod to set
     */
    public void setRemindMethod(String remindMethod) {
        this.remindMethod = remindMethod;
    }

    
    /**
     * @return the remindTime
     */
    public Timestamp getRemindTime() {
        return remindTime;
    }

    
    /**
     * @param remindTime the remindTime to set
     */
    public void setRemindTime(Timestamp remindTime) {
        this.remindTime = remindTime;
    }

    
    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    
    /**
     * @param status the status to set
     */
    public void setStatus(int status) {
        this.status = status;
    }
    
    
}
