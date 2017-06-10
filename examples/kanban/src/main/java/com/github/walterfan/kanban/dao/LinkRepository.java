package com.github.walterfan.kanban.dao;

import com.github.walterfan.msa.common.domain.Link;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

;

/**
 * Created by walterfan on 29/5/2017.
 */
public interface LinkRepository extends JpaRepository<Link, Long> {
    List<Link> findByTag(String tag);
}
