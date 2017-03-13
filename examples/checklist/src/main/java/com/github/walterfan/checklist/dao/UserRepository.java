package com.github.walterfan.checklist.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

/**
 * Created by walterfan on 7/2/2017.
 */
@RepositoryRestResource
public interface UserRepository extends CrudRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);
}


