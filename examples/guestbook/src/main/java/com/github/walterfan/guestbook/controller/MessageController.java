package com.github.walterfan.guestbook.controller;

/**
 * Created by walter on 06/11/2016.
 */

import com.github.walterfan.guestbook.domain.GenericQuery;
import com.github.walterfan.guestbook.domain.Message;
import com.github.walterfan.guestbook.service.MessageService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/guestbook/api/v1/", produces = { "application/json" })
public class MessageController {


    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MessageService messageService;


    @RequestMapping(value = "/messages", method = RequestMethod.POST)
    public Message createMessage(@Valid @RequestBody Message message) throws Exception {
        logger.info("got post request: " + message.toString());
        messageService.createMessage(message);
        return message;
    }

    @RequestMapping(value = {"/messages", "/"}, method = RequestMethod.GET)
    public List<Message> queryMessages(@RequestParam(value = "start",   required = false) Integer start,
                                       @RequestParam(value = "limit",   required = false) Integer limit,
                                       @RequestParam(value = "order",   required = false) String order,
                                       @RequestParam(value = "sortBy",  required = false) String sortBy,
                                       @RequestParam(value = "keyword", required = false) String keyword,
                                       @RequestParam(value = "fieldName",   required = false) String fieldName) {
        logger.info("query messages request");

        GenericQuery query = new GenericQuery();
        if(null != start) query.setStart(start);
        if(null != limit) query.setLimit(limit);
        if(null != order) {
            if("ASC".equalsIgnoreCase(order)) {
                query.setOrder(GenericQuery.OrderType.ASC);
            } else if("DESC".equalsIgnoreCase(order)) {
                query.setOrder(GenericQuery.OrderType.DESC);
            }
        }
        if(StringUtils.isNotBlank(sortBy)) query.setSortBy(sortBy);
        if(StringUtils.isNotBlank(fieldName)) query.setFieldName(fieldName);
        if(StringUtils.isNotBlank(keyword)) query.setKeyword(keyword);

        List<Message> messageList = messageService.queryMessage(query);
        return messageList;
    }

    @RequestMapping(value = "messages/{id}", method = RequestMethod.GET)
    public Message getMessage(@PathVariable("id") String id) throws Exception {
        return messageService.retrieveMessage(id);
    }


    @RequestMapping(value = "messages/{id}", method = RequestMethod.PUT)
    public Message updateMessage(@PathVariable("id") String id, @RequestBody Message message) {
        message.setId(id);
        messageService.updateMessage(message);
        return message;
    }

    @RequestMapping(value = "messages/{id}", method = RequestMethod.DELETE)
    public void deleteMessage(@PathVariable("id") String id) {
        messageService.deleteMessage(id);

    }
}
