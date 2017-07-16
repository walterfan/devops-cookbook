package com.github.walterfan.kanban.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * @author walter
 * 
 */
@Entity
public class News extends BaseObject {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -5398258407862613064L;
	@Id
	private int id;
	private String subject;
	private String content;
	private String author;
	private Date createTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

}
