package com.github.walterfan.msa.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by yafan on 23/7/2017.
 */
@RestController
@RequestMapping("/kanban/api/v1")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    @Autowired
    private TaskRepository taskRepository;



    @RequestMapping(method=POST, value="/tasks")
    public String saveTask(Task task) {
        logger.info("saved {}", task);
        taskRepository.save(task);
        return "redirect:/tasks/list";
    }
}
