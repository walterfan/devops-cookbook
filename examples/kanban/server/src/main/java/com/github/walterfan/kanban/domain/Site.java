package com.github.walterfan.kanban.domain;

public class Site {
	private long siteID;
	private String siteName;
	private String siteUrl;
	private int categoryID;
	private int userID;
	
	public Site() {
		
	}
	public Site(long siteID, String siteName) {
		this.siteID = siteID;
		this.siteName = siteName;
	}
	public Site(String siteName) {
		this.siteName = siteName;
	}
	
	public long getSiteID() {
		return siteID;
	}
	public void setSiteID(long siteID) {
		this.siteID = siteID;
	}
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	public String getSiteUrl() {
		return siteUrl;
	}
	public void setSiteUrl(String siteUrl) {
		this.siteUrl = siteUrl;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((siteName == null) ? 0 : siteName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Site))
			return false;
		Site other = (Site) obj;
		if (siteName == null) {
			if (other.siteName != null)
				return false;
		} else if (!siteName.equals(other.siteName))
			return false;
		return true;
	}
	
	public String toString() {
		return siteName;
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
