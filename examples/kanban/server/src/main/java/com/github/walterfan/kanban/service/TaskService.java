/**
 * This is the sevice class for those actions about user
 *
 */
package com.github.walterfan.kanban.service;

import com.github.walterfan.kanban.domain.*;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @author walter.fan@gmail.com on 07/18/06
 *
 */
public interface TaskService   {
    
    List<Task> getTaskList(int taskType, int userID);
    
    List<Task> findTasks(Task task);
        
    List<Task> getFinishedTasks(int userID);
    
    List<Task> getTaskList(Date beginTime, Date endTime, int userID);
    
    int finishTask(int taskID);
    
    Map<Integer, Context> getContexts();
    
    List<Goal> getAllGoalList(int userID);
    
    List<Goal> getGoalList(int goalType, int userID);
    
    List<Goal> findGoal(Goal goal, String[] fields);
    
    int createGoal(Goal goal);
    
    int updateGoal(Goal goal);
    
    int deleteGoal(int goalID);
    
    Goal retrieveGoal(int goalID);
    
    List<Task> getTaskListByDeadline(Date fromDate, Date toDate, int userID);

    List<Task> getSubTaskList(int taskID);
    
    int getSubTaskCount(int taskID);
    
    public int createRepeatInfo(RepeatVO vo);
    
    public RepeatInfo retrieveRepeatInfo(int repeatID);
    
    public int updateRepeatInfo(RepeatVO vo);
    
    public int deleteRepeatInfo(int repeatID);
    
    public int createTask(TaskVO vo);
    
    public int updateTask(TaskVO vo);
}
