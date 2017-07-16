package com.github.walterfan.kanban.controller;

import com.github.walterfan.kanban.dao.LinkRepository;
import com.github.walterfan.kanban.domain.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.github.walterfan.kanban.domain.HttpReq;

import java.util.List;

@Controller
@RequestMapping("/")
public class KanbanController {
	private static final Logger logger = LoggerFactory.getLogger(KanbanController.class);

	@Autowired
	private LinkRepository linkRepository;


	@RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) {
        List<Link> links = linkRepository.findAll();
        if(links != null) {
            model.addAttribute("links", links);
        }

        return "index";
    }
}
