package com.github.walterfan.kanban.dao;


import com.github.walterfan.kanban.domain.Category;
import com.github.walterfan.kanban.domain.Link;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by yafan on 16/7/2017.
 */

public interface LinkRepository extends JpaRepository<Link, Long> {
    List<Link> findByCategory(Category category);
}
