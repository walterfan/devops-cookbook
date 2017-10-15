package com.github.walterfan.msa.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created by yafan on 22/7/2017.
 */
@Controller
public class KanbanController {

    @Autowired
    private TaskRepository taskRepository;

    @RequestMapping(method = GET)
    public String index(Model model) {
        return getTasks(model);
    }

    @RequestMapping(method = GET, value="/tasks/list")
    public String getTasks(Model model) {

        Pageable pageRequest = new PageRequest(0, 40);
        Page<Task> page = taskRepository.findAll(pageRequest);


        if (page != null) {
            model.addAttribute("tasks", page.getContent());
        }

        return "index";
    }

    @RequestMapping(method=GET, value="/tasks/add")
    public String addTask(Model model) {
        return "task";
    }
}
