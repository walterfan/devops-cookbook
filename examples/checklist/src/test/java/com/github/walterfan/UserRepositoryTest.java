package com.github.walterfan;

import com.github.walterfan.checklist.dao.UserEntity;
import com.github.walterfan.checklist.dao.UserRepository;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.testng.annotations.Test;

import javax.transaction.Transactional;

import static org.testng.Assert.assertNotNull;

/**
 * Created by walter on 11/03/2017.
 */
@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    @Rollback(true)
    public void saveUserEntity() throws Exception {
        UserEntity user = new UserEntity();
        user.setUsername("walter");
        user.setPassword("pass");
        user.setEmail("walter@");

        UserEntity savedUser  = userRepository.save(user);
        assertNotNull(user.getId());

    }
}
