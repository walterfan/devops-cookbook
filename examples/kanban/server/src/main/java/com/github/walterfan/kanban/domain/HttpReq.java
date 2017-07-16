package com.github.walterfan.kanban.domain;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMethod;

public class HttpReq extends BaseObject {
	private URL url;
	private RequestMethod method;
	
	private Map<String, String> parameters;
	private Map<String, String> headers;
	
	public HttpReq() {
		parameters = new HashMap<String, String>();
		headers = new HashMap<String, String>();
	}
	
	public URL getUrl() {
		return url;
	}
	public void setUrl(URL url) {
		this.url = url;
	}
	public RequestMethod getMethod() {
		return method;
	}
	public void setMethod(RequestMethod method) {
		this.method = method;
	}
	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(String url) throws UnsupportedEncodingException  {
		String[] pairs = url.split("&");
	    for (String pair : pairs) {
	        int idx = pair.indexOf("=");
	        parameters.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), 
	        		URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
	    }
	}
	
	public void setHeaders(String url) throws UnsupportedEncodingException {
		String[] pairs = url.split("&");
	    for (String pair : pairs) {
	        int idx = pair.indexOf("=");
	        headers.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), 
	        		URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
	    }
	}
	public Map<String, String> getHeaders() {
		return headers;
	}
	
	
	
}
