package com.github.walterfan.kanban.service;

import com.github.walterfan.kanban.dao.TaskDao;
import com.github.walterfan.kanban.domain.*;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.*;

public class TaskServiceImpl implements TaskService {
    private static Log logger = LogFactory.getLog(TaskServiceImpl.class);
    
	private EmailService sender = null;
	   
    private TaskDao taskDao;
    
    public EmailService getSender() {
		return sender;
	}

	public void setSender(EmailService sender) {
		this.sender = sender;
	}

	public List<Task> getTaskList(int taskType, int userID) {
        return taskDao.getTaskList(taskType, userID);
    }

    public List<Task> list(Integer userID) {
        return taskDao.list(userID);
    }
        
    public Integer create(Task t) {
        /*if(t.getRemindID()!=0) {
            taskDao.createRemindTask(t);
        }*/
        return taskDao.create(t);
    }

    public int delete(Integer id) {
        int ret = taskDao.deleteRemind(id);
        return ret + taskDao.delete(id);
    }

    public List<Task> find(Task task) {
        return taskDao.find(task);
    }

    public List<Task> list() {
        return taskDao.list();
    }

    public Task retrieve(Integer id) {
        return taskDao.retrieve(id);
    }

    public int update(Task newTask) {
        //Task oldTask = taskDao.retrieve(newTask.getTaskID());
        /*if(oldTask.getRemindTime()!=newTask.getRemindTime()) {
            taskDao.deleteRemindTask(newTask.getTaskID());
            if(newTask.getRemindTime()!=null) {    
                taskDao.createRemindTask(newTask);
            }
        }*/
        return taskDao.update(newTask);
    }

    
    /**
     * @return the taskDao
     */
    public TaskDao getTaskDao() {
        return taskDao;
    }

    
    /**
     * @param taskDao the taskDao to set
     */
    public void setTaskDao(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    public Map<Integer, Context> getContexts() {
        return this.taskDao.getContexts();
    }

	public int createGoal(Goal goal) {
		return taskDao.createGoal(goal);
	}

	public int deleteGoal(int goalID) {
		return  taskDao.deleteGoal(goalID);
	}

	public List<Goal> getGoalList(int goalType, int userID) {
		return  taskDao.getGoalList(goalType, userID);
	}

	public Goal retrieveGoal(int goalID) {
		return taskDao.retrieveGoal(goalID);
	}

	public int updateGoal(Goal goal) {
		return taskDao.updateGoal(goal);
	}

	public List<Goal> findGoal(Goal goal, String[] fields) {
		return taskDao.findGoal(goal, fields);
	}

	public List<Task> findTasks(Task task) {
		return taskDao.findTasks(task);
	}

	public List<Goal> getAllGoalList(int userID) {
		return taskDao.getAllGoalList(userID);
	}

	public List<Task> getTaskList(Date beginTime, Date endTime, int userID) {
		return taskDao.getTaskList(beginTime, endTime, userID);
	}

	public List<Task> getFinishedTasks(int userID) {
		return taskDao.getFinishedTasks(userID);
	}

	public int finishTask(int taskID) {
		return taskDao.finishTask(taskID);
	}

    public List<Task> getTaskListByDeadline(Date fromDate, Date toDate, int userID) {
        return taskDao.getTaskListByDeadline(fromDate, toDate, userID);

    }

/*    public int createRepeatTask(Task task, RepeatVO vo) {
        int repeatID = createRepeatInfo(task, vo);
        task.setRepeatID(repeatID);
        int taskID = taskDao.create(task);
        return taskID;
    }*/

    public int createRepeatInfo(RepeatVO vo) {
        RepeatInfo ri = convertVO2RepeatInfo(vo);
        logger.info("createRepeatInfo: " + ri);
        int repeatID = taskDao.createRepeatInfo(ri);
        return repeatID;
    }

    private RepeatInfo convertVO2RepeatInfo(RepeatVO vo) {
        RepeatInfo ri = new RepeatInfo();

        String repeatType = vo.getRepeatType();
        ri.setRepeatType(repeatType);
        ri.setInterval(vo.getRepeatInterval());
        Timestamp beginTime = vo.getRepeatBeginTime();
        ri.setEffectiveTime(beginTime);

        String endType = vo.getRepeatEndType();
        if (RepeatInfo.NO_END_DATE.equals(endType)) {
            ri.setAlwaysRepeat(1);
            ri.setExpireTime(null);
        } else {
            Timestamp ts = vo.getRepeatEndTime();
            if (ts != null) {
                ri.setExpireTime(ts);
            } else {
                // compute expirationTime
                long expireTime = 0;
                int cnt = vo.getRepeatTimes();
                if ("daily".equalsIgnoreCase(repeatType)) {
                    expireTime = beginTime.getTime() + cnt * ri.getInterval() * DateUtils.MILLIS_PER_DAY;
                    ri.setExpireTime(new Timestamp(expireTime));
                } else if ("weekly".equalsIgnoreCase(repeatType)) {
                    expireTime = beginTime.getTime() + cnt * ri.getInterval() * 7 * DateUtils.MILLIS_PER_DAY;
                    ri.setExpireTime(new Timestamp(expireTime));
                } else if ("monthly".equalsIgnoreCase(repeatType)) {
                    long days = DateUtils.getFragmentInDays(beginTime, Calendar.MONTH);
                    expireTime = beginTime.getTime() + cnt * days * ri.getInterval() * DateUtils.MILLIS_PER_DAY;

                } else if ("yearly".equalsIgnoreCase(repeatType)) {
                    long days = DateUtils.getFragmentInDays(beginTime, Calendar.YEAR);
                    expireTime = beginTime.getTime() + cnt * days * ri.getInterval() * DateUtils.MILLIS_PER_DAY;

                }
                ri.setExpireTime(new Timestamp(expireTime));

            }

        }
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(beginTime.getTime());
        ri.setStartHour(cal.get(Calendar.HOUR_OF_DAY));
        ri.setStartMinute(cal.get(Calendar.MINUTE));
        ri.setStartSecond(cal.get(Calendar.SECOND));
        
        return ri;
}

    public int deleteRepeatInfo(int repeatID) {
        return taskDao.deleteRepeatInfo(repeatID);
    }

    public RepeatInfo retrieveRepeatInfo(int repeatID) {
        return taskDao.retrieveRepeatInfo(repeatID);
    }

    public int updateRepeatInfo(RepeatVO vo) {
        RepeatInfo ri = convertVO2RepeatInfo(vo);
        ri.setRepeatID(vo.getRepeatID());
        logger.info("updateRepeatInfo: " + ri);
        return taskDao.updateRepeatInfo(ri);
    }

	public List<Task> getSubTaskList(int taskID) {
		return taskDao.getSubTaskList(taskID);
	}

	public int getSubTaskCount(int taskID) {
		return taskDao.getSubTaskCount(taskID);
	}
	
	 public int createTask(TaskVO vo) {
        Task task = new Task();
        BeanUtils.copyProperties(vo, task);
        if (vo.isRemind()) {
            Remind rm = new Remind();
            BeanUtils.copyProperties(vo, rm);
            int remindID = taskDao.createRemind(rm);
            task.setRemindID(remindID);
        } else {
            task.setRepeatID(0);
        }

        if (vo.isRepeat()) {
            RepeatInfo rp = convertVO2RepeatInfo(vo);
            int repeatID = taskDao.createRepeatInfo(rp);
            task.setRepeatID(repeatID);
        } else {
            task.setRepeatID(0);
        }
        return taskDao.create(task);
    }

    public int updateTask(TaskVO vo) {
        Task task = new Task();
        BeanUtils.copyProperties(vo, task);
        
        Task oldTask = taskDao.retrieve(task.getTaskID());
        if(oldTask == null) {
            return 0;
        }
                      
        if(vo.isRemind()) {
            Remind rm = new Remind();
            BeanUtils.copyProperties(vo, rm);
            //rt.setTask(task);
            if(oldTask.getRemindID() > 0){
                Remind oldRm = taskDao.retrieveRemind(oldTask.getRemindID());
                if(!rm.equals(oldRm)) {
                    taskDao.updateRemind(rm, Remind.REMIND_SCHEDULED);
                }
            } else {
                int remindID = taskDao.createRemind(rm);
                task.setRemindID(remindID);
            }
        }  else {
            if(oldTask.getRemindID()>0) {
                taskDao.deleteRemind(oldTask.getRemindID());                
            }
            task.setRepeatID(0);
        }
        
        if(vo.isRepeat()) {
            RepeatInfo rp = convertVO2RepeatInfo(vo);
            
            //update repeat info
            if(oldTask.getRepeatID() > 0) {
                rp.setRepeatID(oldTask.getRepeatID());
                taskDao.updateRepeatInfo(rp);
            } else {
                int repeatID = taskDao.createRepeatInfo(rp);
                task.setRepeatID(repeatID);
            }
            
        } else {
            if(oldTask.getRepeatID() > 0) {
                //??? finished repeat task may use old repeatID
                //taskDao.deleteRepeatInfo(task.getRepeatID());                
            }
            task.setRepeatID(0);
        }
        return taskDao.update(task);
    }

    



}
