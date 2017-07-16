package com.github.walterfan.kanban.domain;

public class Log extends BaseObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6437818077472740408L;

	private int logLevel;
	
	private int logType;
	
	private long logTime;
	
	private String logContent;

	public int getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(int logLevel) {
		this.logLevel = logLevel;
	}

	public int getLogType() {
		return logType;
	}

	public void setLogType(int logType) {
		this.logType = logType;
	}

	public long getLogTime() {
		return logTime;
	}

	public void setLogTime(long logTime) {
		this.logTime = logTime;
	}

	public String getLogContent() {
		return logContent;
	}

	public void setLogContent(String logContent) {
		this.logContent = logContent;
	}
	
	
	
}
