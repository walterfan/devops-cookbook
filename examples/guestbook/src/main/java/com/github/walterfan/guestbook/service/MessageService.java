package com.github.walterfan.guestbook.service;

import com.github.walterfan.guestbook.dao.MessageDao;
import com.github.walterfan.guestbook.domain.GenericQuery;
import com.github.walterfan.guestbook.domain.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Created by walter on 06/11/2016.
 */
@Service
public class MessageService {

    @Autowired
    private MessageDao messageDao;

    public void createMessage(Message message) {
        UUID id = UUID.randomUUID();
        message.setId(id.toString());
        messageDao.createMessage(message);
    }

    public Message retrieveMessage(String id) {
        return messageDao.retrieveMessage(id);
    }

    public List<Message> queryMessage(GenericQuery query) {
        return messageDao.queryMessage(query);
    }

    public void updateMessage(Message message) {
        messageDao.updateMessage(message);
    }

    public void deleteMessage(String id) {
        messageDao.deleteMessage(id);

    }
}
