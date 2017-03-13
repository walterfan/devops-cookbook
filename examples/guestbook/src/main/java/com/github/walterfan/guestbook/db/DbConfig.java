package com.github.walterfan.guestbook.db;

/**
 * Created by walter on 16/11/2016.
 */
public class DbConfig  {
    public static final String DRIVER_CLASS = "jdbc.driverClass";
    public static final String URL = "jdbc.url";
    public static final String USERNAME = "jdbc.username";
    public static final String PASSWORD = "jdbc.password";

    private String driverClass = null;
    /**
     * JDBC connection URL
     */
    private String url = null;
    /**
     * JDBC connect user name
     */
    private String userName = null;
    /**
     * JDBC connect password
     */
    private String password = null;

    public DbConfig() {

    }

    public DbConfig(String drv, String url, String user, String pwd) {
        this.driverClass = drv;
        this.url = url;
        this.userName = user;
        this.password = pwd;
    }

    public DbConfig( String url, String user, String pwd) {
        this.url = url;
        this.userName = user;
        this.password = pwd;
    }
    /**
     * @return the driverClass
     */
    public String getDriverClass() {
        return driverClass;
    }

    /**
     * @param driverClass the driverClass to set
     */
    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public String toString() {
        return "driverClass=" + driverClass
                + ", url=" + url
                + ", userName=" + userName
                + ", password=" + password;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((driverClass == null) ? 0 : driverClass.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DbConfig other = (DbConfig) obj;
        if (driverClass == null) {
            if (other.driverClass != null)
                return false;
        } else if (!driverClass.equals(other.driverClass))
            return false;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (url == null) {
            if (other.url != null)
                return false;
        } else if (!url.equals(other.url))
            return false;
        if (userName == null) {
            if (other.userName != null)
                return false;
        } else if (!userName.equals(other.userName))
            return false;
        return true;
    }
}
