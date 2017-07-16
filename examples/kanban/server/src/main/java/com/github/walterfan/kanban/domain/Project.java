package com.github.walterfan.kanban.domain;

import java.util.List;


public class Project extends Task {
    
    private List<Task> taskList;

	/**
     * @return the taskList
     */
    public List<Task> getTaskList() {
        return taskList;
    }

    
    /**
     * @param taskList the taskList to set
     */
    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }
    
    public void addTask(Task task) {
        taskList.add(task);
    }
    
    public void removeTask(Task task) {
        taskList.remove(task);
    }
    
    
}
