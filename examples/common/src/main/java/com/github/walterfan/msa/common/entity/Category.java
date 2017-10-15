package com.github.walterfan.msa.common.entity;

import com.github.walterfan.msa.common.domain.BaseObject;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collection;

/**
 * Created by yafan on 24/9/2017.
 */
@Entity
@Table(name = "category")
public class Category extends BaseObject {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private String name;
    @ManyToOne
    private Category parent;

    @OneToMany(mappedBy="parent")
    private Collection<Category> children;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public Collection<Category> getChildren() {
        return children;
    }

    public void setChildren(Collection<Category> children) {
        this.children = children;
    }
}
