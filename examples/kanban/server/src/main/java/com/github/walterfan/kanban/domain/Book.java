package com.github.walterfan.kanban.domain;

import javax.persistence.Entity;
import java.sql.Timestamp;

/**
 * @author walter
 * 
 */

@Entity
public class Book extends BaseObject {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1156666440406865554L;

    private long id;

    private String isbn;

    private String title;

    private String tags;

    private String author;

    private String description;

    private Timestamp createTime;


    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
