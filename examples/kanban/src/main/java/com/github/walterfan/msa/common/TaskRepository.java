package com.github.walterfan.msa.common;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by yafan on 22/7/2017.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByPriority(TaskPriority priority);

}
