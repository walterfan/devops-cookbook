package com.github.walterfan.kanban.dao;

import com.github.walterfan.msa.common.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by walterfan on 3/6/2017.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByUsername(String name);
}
