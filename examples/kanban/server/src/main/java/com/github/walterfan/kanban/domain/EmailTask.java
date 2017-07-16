package com.github.walterfan.kanban.domain;

import java.sql.Timestamp;


/**
 * @author walter
 *
 */
public class EmailTask extends BaseObject{
    
    public static final String EMAIL_SCHEDULED = "SCHEDULED";
    public static final String EMAIL_START = "START";
    public static final String EMAIL_END = "END";
    public static final String EMAIL_FAILED = "FAILED";
    
    private String status = EMAIL_SCHEDULED;
    
    private String toAddr;
    private String ccAddr;
    private String bccAddr;
    private String fromAddr;
    private String replyAddr;
    
    private String subject;
    private String content;
    private String contentType = "text/plain";
    private int emailID;
    private int failedTimes;
    private String failedReason;
    private String executor;
    private Timestamp startTime;
    
    /**
     * @return the toAddr
     */
    public String getToAddr() {
        return toAddr;
    }
    
    /**
     * @param toAddr the toAddr to set
     */
    public void setToAddr(String toAddr) {
        this.toAddr = toAddr;
    }
    
    /**
     * @return the ccAddr
     */
    public String getCcAddr() {
        return ccAddr;
    }
    
    /**
     * @param ccAddr the ccAddr to set
     */
    public void setCcAddr(String ccAddr) {
        this.ccAddr = ccAddr;
    }
    
    /**
     * @return the bccAddr
     */
    public String getBccAddr() {
        return bccAddr;
    }
    
    /**
     * @param bccAddr the bccAddr to set
     */
    public void setBccAddr(String bccAddr) {
        this.bccAddr = bccAddr;
    }
    
    /**
     * @return the fromAddr
     */
    public String getFromAddr() {
        return fromAddr;
    }
    
    /**
     * @param fromAddr the fromAddr to set
     */
    public void setFromAddr(String fromAddr) {
        this.fromAddr = fromAddr;
    }
    
    /**
     * @return the replyAddr
     */
    public String getReplyAddr() {
        return replyAddr;
    }
    
    /**
     * @param replyAddr the replyAddr to set
     */
    public void setReplyAddr(String replyAddr) {
        this.replyAddr = replyAddr;
    }
    
    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }
    
    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }
    
    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }
    
    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    /**
     * @return the emailID
     */
    public int getEmailID() {
        return emailID;
    }
    
    /**
     * @param emailID the emailID to set
     */
    public void setEmailID(int emailID) {
        this.emailID = emailID;
    }
    
    /**
     * @return the failedTimes
     */
    public int getFailedTimes() {
        return failedTimes;
    }
    
    /**
     * @param failedTimes the failedTimes to set
     */
    public void setFailedTimes(int failedTimes) {
        this.failedTimes = failedTimes;
    }
    
    /**
     * @return the failedReason
     */
    public String getFailedReason() {
        return failedReason;
    }
    
    /**
     * @param failedReason the failedReason to set
     */
    public void setFailedReason(String failedReason) {
        this.failedReason = failedReason;
    }
    
    /**
     * @return the executor
     */
    public String getExecutor() {
        return executor;
    }
    
    /**
     * @param executor the executor to set
     */
    public void setExecutor(String executor) {
        this.executor = executor;
    }
    
    /**
     * @return the startTime
     */
    public Timestamp getStartTime() {
        return startTime;
    }
    
    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    
    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    
    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    
}
