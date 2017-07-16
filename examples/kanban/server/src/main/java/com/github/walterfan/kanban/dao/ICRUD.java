package com.github.walterfan.kanban.dao;

import java.util.List;

public interface ICRUD<K,T> {
    public  K create(T t);
    public  T retrieve(K id);
    public int delete(K id);
    public  int update(T t);
   //finders...
    public List<T> list();
    public List<T> list(K id);
    public List<T> find(T t);
}
