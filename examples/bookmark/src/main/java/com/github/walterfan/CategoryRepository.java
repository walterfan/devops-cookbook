package com.github.walterfan;

import com.github.walterfan.msa.common.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by yafan on 24/9/2017.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
