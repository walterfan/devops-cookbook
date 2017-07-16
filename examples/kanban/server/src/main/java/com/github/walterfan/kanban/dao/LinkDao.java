package com.github.walterfan.kanban.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.github.walterfan.kanban.domain.Category;
import com.github.walterfan.kanban.domain.Link;


public interface LinkDao {
	 
    //if @Select is used table/column name and class/property name should be the same
 
    //SQL query in "Mapper.xml"
    public List<Link> selectAllLink();
 
    //SQL query in "Mapper.xml"
    public Link selectLink(@Param("id") int id);
 
    @Insert("INSERT INTO link (name) VALUES (#{parentName})")
    public int insertLink(Link parent);
 
    @Select("SELECT * FROM child WHERE parentId = #{id}")
    public List<Category> selectAllCategories(@Param("id") int id);
 
    @Select("SELECT * FROM child WHERE childId = #{id}")
    public Category selectCategory(@Param("id") int id);
 
    //SQL query in "Mapper.xml"
    public int insertCategory(Category child);        
}