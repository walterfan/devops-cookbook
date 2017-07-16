package com.github.walterfan.kanban.domain;

import java.sql.Timestamp;
import java.util.Date;


/**
 * 
 * check wfjob every minute, if it founds any job
 * @author walter
 *
 */
public class RemindJob {
    private int jobID;
    //1:task remind; 
    //2:password expiration remind
    //3:clean job
    private int jobType;
    
    //0-not start, 1: in progress, 2: finished
    private int jobStatus;
    
    private String jobParameter;
    
    private String jobResult;
    
    private Timestamp jobTime;
    
    private Timestamp startTime;
    
    private Timestamp endTime;
    
    private Date createTime;
}
