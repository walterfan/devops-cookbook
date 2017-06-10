package com.github.walterfan.kanban.controller;

import com.github.walterfan.kanban.dao.LinkRepository;
import com.github.walterfan.msa.common.domain.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * Created by walterfan on 29/5/2017.
 */
@Controller
@RequestMapping("/links")
public class LinkController {
    @Autowired
    private LinkRepository linkRepository;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List<Link> queryLinks() {
        return linkRepository.findAll();
    }

    @RequestMapping(value = "/tags/{tag}", method = RequestMethod.GET)
    public List<Link> queryLinks(String tag) {
        return linkRepository.findByTag(tag);
    }

}
