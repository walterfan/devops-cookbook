package com.github.walterfan.kanban.domain;

public class Context extends BaseObject {
	private int contextID;
	
	private String contextName;
	
	private String description;
	
	public int getContextID() {
		return contextID;
	}
	public void setContextID(int contextID) {
		this.contextID = contextID;
	}
	public String getContextName() {
		return contextName;
	}
	public void setContextName(String contextName) {
		this.contextName = contextName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
