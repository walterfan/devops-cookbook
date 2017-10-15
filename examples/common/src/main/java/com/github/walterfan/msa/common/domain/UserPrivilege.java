package com.github.walterfan.msa.common.domain;

import com.github.walterfan.msa.common.entity.Role;
import com.github.walterfan.msa.common.entity.User;

import java.util.Set;

/**
 * Created by yafan on 24/9/2017.
 */
public class UserPrivilege extends BaseObject {

    private User user;

    private Set<Role> roles;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
