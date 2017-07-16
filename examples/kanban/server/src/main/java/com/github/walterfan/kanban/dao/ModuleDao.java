/**
 * 
 */
package com.github.walterfan.kanban.dao;



import com.github.walterfan.kanban.domain.Module;

import java.util.List;


/**
 * @author walter
 *
 */
public interface ModuleDao extends ICRUD<Integer,Module> {
	List<Module> getAllModuleList();

	List<Module> getModuleList(int roleID);
	
	List<Module> getFavoriteModules(int userID);
}
