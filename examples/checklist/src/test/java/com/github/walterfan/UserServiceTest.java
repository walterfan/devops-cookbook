package com.github.walterfan;

import com.github.walterfan.checklist.dao.UserRepository;
import com.github.walterfan.checklist.dto.Registration;
import com.github.walterfan.checklist.service.UserService;
import com.github.walterfan.msa.common.domain.User;
import com.github.walterfan.msa.common.domain.UserStatus;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.util.ReflectionUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
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
    public void setup() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        userService = new UserService();
        userRepository = Mockito.mock(UserRepository.class);

        Field field = userService.getClass().getDeclaredField("userRepository");;
        field.setAccessible(true);
        field.set(userService, userRepository);

        //PropertyUtils.setProperty(userService, "userRepository", userRepository);
    }

    @Test
    public void testRegister() {
        Registration reg = ChecklistTestUtil.createRegistration();


        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);

        when(userRepository.findByEmail(reg.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(argument.capture())).thenReturn(argument.capture());

        User user = userService.register(reg);
        Assert.assertEquals(argument.getValue().getTokens().size(), 1);
        Assert.assertEquals(argument.getValue().getStatus(), UserStatus.pending);
    }
}
