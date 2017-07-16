package com.github.walterfan.kanban.domain;

import java.sql.Timestamp;

/**
 * @author walter
 * 
 *
 */
public class Remind extends BaseObject {
    public final static int REMIND_SCHEDULED = 0;
    public final static int REMIND_START = 1;
    public final static int REMIND_END = 2;
    public final static int REMIND_ERROR = 3;
    
    private int remindID;
       
    private String remindMethod = "email";
    
	private Timestamp remindTime;
	
	private int status;
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
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
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + remindID;
        result = prime * result + ((remindMethod == null) ? 0 : remindMethod.hashCode());
        result = prime * result + ((remindTime == null) ? 0 : remindTime.hashCode());
        return result;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Remind other = (Remind) obj;
        if (remindID != other.remindID)
            return false;
        if (remindMethod == null) {
            if (other.remindMethod != null)
                return false;
        } else if (!remindMethod.equals(other.remindMethod))
            return false;
        if (remindTime == null) {
            if (other.remindTime != null)
                return false;
        } else if (!remindTime.equals(other.remindTime))
            return false;
        return true;
    }
    	
	
}
