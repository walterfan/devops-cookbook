package com.github.walterfan.kanban.domain;

import java.util.Date;


/**
 * @author walter
 *
 */
public class Friend  extends BaseObject {
    private String name;
    private String email;
    private String im;
    private String address;
    private String phone;
    private String mobile;
    private Date birthday;
    private String comments;
    
    private int  friendID;
    private int userID;
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }
    
    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }
    
    /**
     * @return the phoneNumber
     */
    
    
    /**
     * @return the birthday
     */
    public Date getBirthday() {
        return birthday;
    }
    
    /**
     * @param birthday the birthday to set
     */
    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    
    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    
    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    
    /**
     * @return the im
     */
    public String getIm() {
        return im;
    }

    
    /**
     * @param im the im to set
     */
    public void setIm(String im) {
        this.im = im;
    }

    
    /**
     * @return the comments
     */
    public String getComments() {
        return comments;
    }

    
    /**
     * @param comments the comments to set
     */
    public void setComments(String comments) {
        this.comments = comments;
    }

    
    /**
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }

    
    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    
    /**
     * @return the mobile
     */
    public String getMobile() {
        return mobile;
    }

    
    /**
     * @param mobile the mobile to set
     */
    public void setMobile(String mobile) {
        this.mobile = mobile;
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
     * @return the friendID
     */
    public int getFriendID() {
        return friendID;
    }

    
    /**
     * @param friendID the friendID to set
     */
    public void setFriendID(int friendID) {
        this.friendID = friendID;
    }
    
    
    
}
