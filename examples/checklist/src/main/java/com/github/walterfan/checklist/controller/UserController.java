package com.github.walterfan.checklist.controller;


import com.github.walterfan.checklist.dto.Activation;
import com.github.walterfan.checklist.dto.LoginForm;
import com.github.walterfan.checklist.dto.Registration;
import com.github.walterfan.checklist.service.UserService;
import com.github.walterfan.msa.common.domain.User;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Created by walterfan on 4/2/2017.
 */
@RestController
@RequestMapping("/checklist/api/v1/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @RequestMapping(value = "register", method = RequestMethod.POST)
    public User register(@Valid @RequestBody Registration registration) {
        logger.info("register: " + registration.toString());
        return userService.register(registration);
    }

    @RequestMapping(value = "activate", method = RequestMethod.POST)
    public User activate(@Valid @RequestBody Activation activation) {
        logger.info("activate: " + activation.toString());
        return userService.activate(activation);
    }

    @RequestMapping(value = "login", method = RequestMethod.POST)
    public ResponseEntity login(@Valid @RequestBody LoginForm loginForm) throws NotFoundException {

        Optional<User> optUser = userService.getUserByEmail(loginForm.getEmail());
        if(optUser.isPresent()) return new ResponseEntity(HttpStatus.OK);
        return new ResponseEntity(HttpStatus.UNAUTHORIZED);
    }


    //@AuthorizationRole({ "admin" })
    @RequestMapping(method = RequestMethod.GET)
    public List<User> getUsers() {
        logger.info("-----getUsers------");
        List<User> list = userService.getUsers();
        list.stream().forEach(x ->  logger.info(x.toString()));
        return list;
    }
}
