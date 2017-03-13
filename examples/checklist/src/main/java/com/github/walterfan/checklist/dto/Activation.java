package com.github.walterfan.checklist.dto;

import com.github.walterfan.checklist.dao.BaseObject;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Created by walterfan on 25/2/2017.
 */
public class Activation extends BaseObject {
    @Email
    private String email;

    @NotBlank
    private String activationCode;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }
}
