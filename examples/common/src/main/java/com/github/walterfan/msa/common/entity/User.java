package com.github.walterfan.msa.common.entity;

import com.github.walterfan.msa.common.domain.BaseObject;
import com.github.walterfan.msa.common.domain.UserStatus;
import com.github.walterfan.msa.common.entity.Token;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

/**
 * Created by walterfan on 7/2/2017.
 */
@Entity
@Table(name = "user")
public class User extends BaseObject {
    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid2")
    private String id;


    private String username;

    private String password;

    private String email;


    private String phoneNumber;


    private UserStatus status;


    private Date createTime;


    private Date lastModifiedTime;

    @OneToMany(cascade= CascadeType.ALL)
    private List<Token> tokens;


    public User() {
        this.status = UserStatus.pending;
        this.createTime = new Date(System.currentTimeMillis());
        this.lastModifiedTime = new Date(System.currentTimeMillis());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }
}
