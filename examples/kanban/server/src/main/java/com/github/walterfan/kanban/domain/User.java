/**
 * User Object
 */
package com.github.walterfan.kanban.domain;

import java.util.Date;

/**
 * @author walter.fan@gmail.com
 * 
 */
public class User extends BaseObject {
    private int userID;
    // private int roleID;
    private String userName;

    private String password;

    private String email;

    private String phoneNumber;

    private Role role = new Role();

    private Date pwdLastmodifiedTime;

    private Date activeLastmodifiedTime;

    /**
     * pwdNeedChange 0--need not change password 1--need change password at once
     */
    private int pwdNeedChange;

    /**
     * activeStatus: 0--active 1--inactive 2--lock out
     */
    private int activeStatus;

    /**
	 * 
	 */
    public User() {

    }

    public User(int userID, String userName, String password, String email) {
        super();
        this.userID = userID;
        this.userName = userName;
        this.password = password;
        this.email = email;
    }

    public User(String userName, String phoneNumber) {
        super();
        this.userName = userName;
        this.phoneNumber = phoneNumber;
    }

    public int getRoleID() {
        if (role == null) {
            return 0;
        }
        return role.getRoleID();
    }

    public void setRoleID(int roleID) {
        this.role.setRoleID(roleID);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((email == null) ? 0 : email.hashCode());
        result = PRIME * result
                + ((password == null) ? 0 : password.hashCode());
        result = PRIME * result + userID;
        result = PRIME * result
                + ((userName == null) ? 0 : userName.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final User other = (User) obj;
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (userID != other.userID)
            return false;
        if (userName == null) {
            if (other.userName != null)
                return false;
        } else if (!userName.equals(other.userName))
            return false;
        return true;
    }

    /**
     * @return the role
     */
    public Role getRole() {
        return role;
    }

    /**
     * @param role
     *            the role to set
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * @return the pwdLastmodifiedTime
     */
    public Date getPwdLastmodifiedTime() {
        return pwdLastmodifiedTime;
    }

    /**
     * @param pwdLastmodifiedTime
     *            the pwdLastmodifiedTime to set
     */
    public void setPwdLastmodifiedTime(Date pwdLastmodifiedTime) {
        this.pwdLastmodifiedTime = pwdLastmodifiedTime;
    }

    /**
     * @return the activeLastmodifiedTime
     */
    public Date getActiveLastmodifiedTime() {
        return activeLastmodifiedTime;
    }

    /**
     * @param activeLastmodifiedTime
     *            the activeLastmodifiedTime to set
     */
    public void setActiveLastmodifiedTime(Date activeLastmodifiedTime) {
        this.activeLastmodifiedTime = activeLastmodifiedTime;
    }

    /**
     * @return the pwdNeedChange
     */
    public int getPwdNeedChange() {
        return pwdNeedChange;
    }

    /**
     * @param pwdNeedChange
     *            the pwdNeedChange to set
     */
    public void setPwdNeedChange(int pwdNeedChange) {
        this.pwdNeedChange = pwdNeedChange;
    }

    /**
     * @return the activeStatus
     */
    public int getActiveStatus() {
        return activeStatus;
    }

    /**
     * @param activeStatus
     *            the activeStatus to set
     */
    public void setActiveStatus(int activeStatus) {
        this.activeStatus = activeStatus;
    }

    @Override
    public String toString() {
        return "User [activeLastmodifiedTime=" + activeLastmodifiedTime
                + ", activeStatus=" + activeStatus + ", email=" + email
                + ", password=" + password + ", phoneNumber=" + phoneNumber
                + ", pwdLastmodifiedTime=" + pwdLastmodifiedTime
                + ", pwdNeedChange=" + pwdNeedChange + ", role=" + role
                + ", userID=" + userID + ", userName=" + userName + "]";
    }

}
