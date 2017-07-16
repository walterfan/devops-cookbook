package com.github.walterfan.kanban.domain;


/**
 * @author walter
 * 
 *
 */
public class RemindTask extends BaseObject {

    private Task task;
    private Remind remind;

    
    /**
     * @return the task
     */
    public Task getTask() {
        return task;
    }

    
    /**
     * @param task the task to set
     */
    public void setTask(Task task) {
        this.task = task;
    }


    
    /**
     * @return the remind
     */
    public Remind getRemind() {
        return remind;
    }


    
    /**
     * @param remind the remind to set
     */
    public void setRemind(Remind remind) {
        this.remind = remind;
    }
    
    
    	
	
}
