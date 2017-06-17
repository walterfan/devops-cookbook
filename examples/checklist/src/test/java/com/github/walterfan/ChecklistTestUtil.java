package com.github.walterfan;

import com.github.walterfan.checklist.dto.Registration;

/**
 * Created by walterfan on 17/6/2017.
 */
public class ChecklistTestUtil {

    public static Registration createRegistration() {
        Registration registration = new Registration();
        registration.setEmail("walter4test@gmail.com");
        registration.setPassword("testpassword");
        registration.setPasswordConfirmation("testpassword");
        registration.setUsername("walter");
        return registration;
    }
}
