package com.github.walterfan.msa.common.entity;

import java.util.List;

public interface ICRUD<T, K> {
    public  K create(T t);
    public  T retrieve(K id);
    public int delete(K id);
    public  int update(T t);

}
