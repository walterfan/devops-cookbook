package com.github.walterfan.kanban.domain;

/**
 * @author walter
 *
 */
public class UserCategory extends Category {
    
    /**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -3768572168959593515L;
	
	private int userID;

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
    
}
