package com.github.walterfan.kanban.dao;



import com.github.walterfan.kanban.domain.Item;
import com.github.walterfan.kanban.domain.UserCategory;

import java.util.List;
import java.util.Map;


public interface ItemDao extends ICRUD<Integer, Item>{
	List<Item> findItems(Item item);
	
	List<Item> getCheckList(int userID);
	
	UserCategory getCategory(int categoryID);

	Map<Integer, UserCategory> getCategories(int categoryType, int userID);

	List<UserCategory> getCategories(int userID);
	
	List<UserCategory> getCategoryList(int categoryType, int userID) ;
	
    int createCategory(UserCategory category);
    
    int updateCategory(UserCategory category);
    
    int deleteCategory(int categoryID);
}
