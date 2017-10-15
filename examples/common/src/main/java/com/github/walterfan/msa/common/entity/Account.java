package com.github.walterfan.msa.common.entity;

import com.github.walterfan.msa.common.domain.BaseObject;
import com.github.walterfan.msa.common.entity.Bookmark;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by yafan on 24/9/2017.
 */
@Entity
@Table(name = "account")
public class Account extends BaseObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String username;
    private String password;
    private String email;
    private String hint;
    private Bookmark site;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public Bookmark getSite() {
        return site;
    }

    public void setSite(Bookmark site) {
        this.site = site;
    }
}
