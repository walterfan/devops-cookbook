package com.github.walterfan.checklist.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import javax.management.relation.Role;

/**
 * Created by yafan on 23/9/2017.
 */
@RepositoryRestResource
public interface RoleRepository extends CrudRepository<Role, Long> {
}
