/**
 * 
 */
package com.github.walterfan.kanban.dao;

import com.github.walterfan.kanban.domain.*;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @author walter
 *
 */
public interface TaskDao extends ICRUD<Integer,Task> {
    List<Task> getTaskList(int taskType, int userID);
    
    List<Task> list(int userID);
    
    List<Task> getFinishedTasks(int userID);
    
    List<Task> getTaskList(Date beginTime, Date endTime, int userID);
    
    List<Task> findTasks(Task task);
    
    int finishTask(int taskID) ;
    
    Map<Integer, Context> getContexts();
    
    List<Goal> getGoalList(int goalType, int userID);
    
    List<Goal> getAllGoalList(int userID);
    
    List<Goal> findGoal(Goal goal, String[] fields);
    
    int createGoal(Goal goal);
    
    int updateGoal(Goal goal);
    
    int deleteGoal(int goalID);
    
    Goal retrieveGoal(int goalID);
    
  
    //--------------remind task-----------
    int createRemind(Remind rm);

    Remind retrieveRemind(int remindID);
       
    int updateRemind(Remind rm, int status);
    
    int deleteRemind(int remindID);
    
    List<RemindTask> getRemindTaskList();
    
    void finishRemindTask(final List<RemindTask> taskList);
    
    //----------------cleanup ----------------
    int cleanFinishedTask(int count);
    
    int cleanFinishedRemindTask(int count);
    
    int cleanFinishedEmailTask(int count);
    
    
  //--------------repeat task-----------
    List<Task> getRepeatTaskList();
    
    RepeatInfo retrieveRepeatInfo(int repeatID);
    
    int deleteRepeatInfo(int repeatID);
    
    int updateRepeatInfo(RepeatInfo ri);
       
    List<Task> getTaskListByDeadline(Date fromDate, Date toDate, int userID);
    
    int createRepeatInfo(RepeatInfo ri);
    
    List<Task> getSubTaskList(int taskID);
    
    int getSubTaskCount(int taskID);
}
