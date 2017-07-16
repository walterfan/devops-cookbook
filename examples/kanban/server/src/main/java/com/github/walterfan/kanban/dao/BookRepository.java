package com.github.walterfan.kanban.dao;

import com.github.walterfan.kanban.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by yafan on 16/7/2017.
 */
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByTitle(String title);
}
