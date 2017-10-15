package com.github.walterfan.checklist.dao;


import com.github.walterfan.msa.common.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

/**
 * Created by walterfan on 7/2/2017.
 */
@RepositoryRestResource
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
}


