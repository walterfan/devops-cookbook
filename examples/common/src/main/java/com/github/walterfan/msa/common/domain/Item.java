package com.github.walterfan.msa.common.domain;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;


public class Item extends BaseObject {

    private int id;

    private String name;

    private String description;

    private String tags;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }


}
