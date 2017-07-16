package com.github.walterfan.kanban.domain;

/**
 * 
 *    public final static int CATEGORY_TYPE_TASK = 1;
 *    
 *    public final static int CATEGORY_TYPE_LORE = 2;
 *    
 *    public final static int CATEGORY_TYPE_BOOK = 3;
 *    
 *    public final static int CATEGORY_TYPE_FRIEND = 4;
 *    
 *    public final static int CATEGORY_TYPE_SITE = 5;
 * 
 * @author Walter Fan
 *
 */
public enum CategoryType {
	TASK(1), 
	LORE(2), 
	BOOK(3), 
	FIRIENT(4), 
	SITE(5);
	
	private int value;
	
	CategoryType(int val) {
		this.value = val;
	}
	
	public int getValue() {
		return this.value;
	}
}
