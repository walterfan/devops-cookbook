package com.github.walterfan.checklist.controller;

import com.github.walterfan.checklist.dao.UserEntity;
import com.github.walterfan.checklist.dto.Activation;
import com.github.walterfan.checklist.dto.Registration;
import com.github.walterfan.checklist.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;

/**
 * Created by walterfan on 4/2/2017.
 */
@Controller
@RequestMapping("/checklist/api/v1/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @RequestMapping(value = "register", method = RequestMethod.POST)
    public ModelAndView register(@Valid @RequestBody Registration registration) {
        logger.info("register: " + registration.toString());
        userService.register(registration);

        return new ModelAndView("index");
    }

    @RequestMapping(value = "activate", method = RequestMethod.POST)
    public ModelAndView activate(@Valid @RequestBody Activation activation) {
        logger.info("activate: " + activation.toString());
        userService.activate(activation);

        return new ModelAndView("index");
    }

    //TODO: login required
    @RequestMapping(method = RequestMethod.GET)
    public List<UserEntity> getUsers() {
        logger.info("-----getUsers------");
        List<UserEntity> list = userService.getUsers();
        list.stream().forEach(x ->  logger.info(x.toString()));
        return list;
    }
}
