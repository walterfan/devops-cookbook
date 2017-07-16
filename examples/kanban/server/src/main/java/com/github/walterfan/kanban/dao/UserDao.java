/**
 * 
 */
package com.github.walterfan.kanban.dao;


import com.github.walterfan.kanban.domain.EmailTask;
import com.github.walterfan.kanban.domain.User;

import java.util.List;


/**
 * @author walter
 *
 */
public interface UserDao extends ICRUD<Integer,User> {
	
    int fetchEmailTaskList(String executor, int count, int maxDuration);
    
    List<EmailTask> getEmailTaskList(String status, int count, String executor);
	   
	int createEmailTask(EmailTask et);
	
	EmailTask retrieveEmailTask(int etID);
	
	int updateEmailTask(EmailTask et);
	
	int deleteEmailTask(int etID);
	
	User findUserByName(String userName);
}
