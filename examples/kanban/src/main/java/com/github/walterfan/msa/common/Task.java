package com.github.walterfan.msa.common;

import org.apache.commons.lang3.time.DateFormatUtils;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


import java.text.ParseException;
import java.time.Duration;
import java.util.Date;

/**
 * Created by yafan on 22/7/2017.
 */
@Entity
public class Task {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;

    private String name;

    private String description;

    private TaskPriority priority;

    private Date deadline;

    private Duration estimation;

    private Date beginTime;

    private Date endTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public void setDeadline(String deadlineStr) throws ParseException {
        this.deadline = DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.parse(deadlineStr);
    }

    public Duration getEstimation() {
        return estimation;
    }

    public void setEstimation(Duration estimation) {
        this.estimation = estimation;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public void setBeginTime(String beginTimeStr) throws ParseException {
        this.beginTime = DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.parse(beginTimeStr);
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public void setEndTime(String endTimeStr) throws ParseException {
        this.endTime = DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.parse(endTimeStr);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", priority=" + priority +
                ", deadline=" + deadline +
                ", estimation=" + estimation +
                ", beginTime=" + beginTime +
                ", endTime=" + endTime +
                '}';
    }
}
