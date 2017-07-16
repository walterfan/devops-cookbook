package com.github.walterfan.kanban.service;

import com.github.walterfan.kanban.domain.Email;

import javax.mail.MessagingException;

/**
 * Created by walter on 8/6/16.
 */
public interface EmailService {
    public void send(Email email) throws MessagingException ;
}
