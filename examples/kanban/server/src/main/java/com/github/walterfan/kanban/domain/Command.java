package com.github.walterfan.kanban.domain;

import java.util.Map;

public class Command extends BaseObject {
	private String name;
	private String uri;
	private int id;
	private int type;
	
	private Map<String, Object> parameters;

	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public void addParameter(String key, Object val) {
		this.parameters.put(key, val);
	}
	
	public Object getParameter(String key) {
		return this.parameters.get(key);
	}
}
