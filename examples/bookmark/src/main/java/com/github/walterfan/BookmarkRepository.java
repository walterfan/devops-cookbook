package com.github.walterfan;

import com.github.walterfan.msa.common.entity.Bookmark;
import com.github.walterfan.msa.common.entity.User;
import com.github.walterfan.msa.common.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by yafan on 24/9/2017.
 */
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    List<Bookmark> findByUser(User user);

    List<Bookmark> findByCategory(Category category);
}
