package com.github.walterfan.checklist.dao;

import com.github.walterfan.msa.common.domain.Module;


import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Created by yafan on 23/9/2017.
 */
@RepositoryRestResource
public interface ModuleRepository extends CrudRepository<Module, Long>  {
}
