package com.github.walterfan.guestbook.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by walter on 06/11/2016.
 */
@Controller
@RequestMapping(value = "/", produces = { "text/html" })
public class IndexController {


    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView index(@ModelAttribute("model") ModelMap model) {

        model.addAttribute("copyRight", "walterfan.github.com © 2016. All Rights Reserved. ");

        return new ModelAndView("index", model);
    }

    @RequestMapping(value = "/guestbook", method = RequestMethod.GET)
    public ModelAndView guestbook(@ModelAttribute("model") ModelMap model) {

        model.addAttribute("copyRight", "http://walterfan.github.com © 2016. All Rights Reserved. ");

        return new ModelAndView("guestbook", model);
    }

}
