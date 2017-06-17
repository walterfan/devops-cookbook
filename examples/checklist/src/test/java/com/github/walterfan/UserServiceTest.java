package com.github.walterfan;

import com.github.walterfan.checklist.dao.UserRepository;
import com.github.walterfan.checklist.dto.Registration;
import com.github.walterfan.checklist.service.UserService;
import com.github.walterfan.msa.common.domain.User;
import com.github.walterfan.msa.common.domain.UserStatus;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by walterfan on 17/6/2017.
 */


public class UserServiceTest {

    private UserService userService;

    private UserRepository userRepository;

    @BeforeMethod
    public void setup() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        userService = new UserService();
        userRepository = Mockito.mock(UserRepository.class);

        //PropertyUtils.setProperty(userService, "userRepository", userRepository);
    }

    @Test
    public void testRegister() {
        Registration reg = ChecklistTestUtil.createRegistration();
        User mockUser = new User();

        when(userRepository.findByEmail(reg.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        User user = userService.register(reg);
        Assert.assertEquals(user.getTokens().size(), 1);
        Assert.assertEquals(user.getStatus(), UserStatus.pending);
    }
}
