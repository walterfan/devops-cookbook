package com.github.walterfan.guestbook.dao;

import com.github.walterfan.guestbook.domain.GenericQuery;
import com.github.walterfan.guestbook.domain.Message;

import java.util.List;

/**
 * Created by walter on 06/11/2016.
 */
public interface MessageDao {

    void createMessage(Message message);


    Message retrieveMessage(String id);

    void updateMessage(Message message);


    void deleteMessage(String id);


    List<Message> queryMessage(GenericQuery query);
}
