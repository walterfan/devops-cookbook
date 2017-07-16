package com.github.walterfan.kanban.service;

public interface SecurityService {
	String getMasterKey();
	
	String getMasterSalt();
	
	String getSessionEncrKey();
	
	String getSessionSaltKey();
	
	String getSessionAuthKey();
}
