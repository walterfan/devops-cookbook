package com.github.walterfan.msa.common.entity;

import java.util.List;
import java.util.Map;

/**
 * Created by yafan on 9/10/2017.
 */
public interface IFinder<T, K>  {

    T findOne(K key);

    List<T> findAll();

    List<T> find(Map<String, Object> params);
}
