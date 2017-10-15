package com.github.walterfan.checklist.dto;

import com.github.walterfan.msa.common.domain.BaseObject;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Size;

/**
 * Created by yafan on 23/9/2017.
 */
public class LoginForm extends BaseObject {

    @Email
    private String email;

    @NotBlank
    private String password;


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
}
