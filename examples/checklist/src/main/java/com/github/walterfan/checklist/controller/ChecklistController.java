package com.github.walterfan.checklist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by walterfan on 30/1/2017.
 */
@Controller
public class ChecklistController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView home() {
        return new ModelAndView("index");
    }

    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public ModelAndView adminForm() {
        return new ModelAndView("admin");
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView signupForm() {
        return new ModelAndView("login");
    }

    @RequestMapping(value = "/about", method = RequestMethod.GET)
    public ModelAndView about() {
        return new ModelAndView("about");
    }
}
