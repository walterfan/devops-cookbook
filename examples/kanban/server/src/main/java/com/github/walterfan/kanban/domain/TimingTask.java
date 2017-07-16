package com.github.walterfan.kanban.domain;

import java.util.Date;
import java.util.List;

class Alarm {
    private String audioFilePath;
    private Date alarmTime;
}

public class TimingTask {
    private int taskID;
    private String taskName;
    private Date deadline;
    private List<Alarm> alarmList;
    
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
     * @return the deadline
     */
    public Date getDeadline() {
        return deadline;
    }
    
    /**
     * @param deadline the deadline to set
     */
    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }
    
    /**
     * @return the alarmList
     */
    public List<Alarm> getAlarmList() {
        return alarmList;
    }
    
    /**
     * @param alarmList the alarmList to set
     */
    public void setAlarmList(List<Alarm> alarmList) {
        this.alarmList = alarmList;
    }
    
    
}
