package com.github.walterfan.kanban.domain;

import java.sql.Timestamp;


/**
 * @author walter
 * represent object of repeat info on presentaion layer
 *
 */
public class RepeatVO extends BaseObject {

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
    
    
}
