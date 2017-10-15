package com.github.walterfan;

import com.github.walterfan.msa.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by yafan on 24/9/2017.
 */
public interface UserRepository extends JpaRepository<User, String> {
}
