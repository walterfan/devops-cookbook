package com.github.walterfan.kanban.domain;


public class Permission extends BaseObject {
    private int permissionID;
    private String permissionName;
    private String description;
    
    private Resource resource;
    private Operation operation;
    
    
	public int getPermissionID() {
		return permissionID;
	}
	public void setPermissionID(int permissionID) {
		this.permissionID = permissionID;
	}
	public String getPermissionName() {
		return permissionName;
	}
	public void setPermissionName(String permissionName) {
		this.permissionName = permissionName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
    
}
