package com.github.walterfan.checklist.service;

import com.github.walterfan.checklist.domain.BaseObject;
import com.github.walterfan.checklist.domain.Token;
import com.github.walterfan.checklist.domain.User;
import com.github.walterfan.checklist.dao.UserRepository;
import com.github.walterfan.checklist.domain.UserStatus;
import com.github.walterfan.checklist.dto.Activation;
import com.github.walterfan.checklist.dto.Registration;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by walterfan on 7/2/2017.
 */
@Service
public class UserService extends BaseObject {
    public static final int ACTIVATION_CODE_SIZE = 32;
    @Autowired
    private UserRepository userRepository;

    public User register(Registration registration) {

        Optional<User> userOptional = userRepository.findByEmail(registration.getEmail());
        if(userOptional.isPresent()) {
            throw new RuntimeException("The email existed: " + registration.getEmail());
        }

        User user = new User();
        BeanUtils.copyProperties(registration, user);
        Token token = new Token();
        token.generateToken();

        user.setId(UUID.randomUUID().toString());
        user.setTokens(Arrays.asList(token));

        return userRepository.save(user);
    }

    public User activate(Activation activation) {
        userRepository.findByEmail(activation.getEmail());

        User user = new User();
        BeanUtils.copyProperties(activation, user);
        user.setStatus(UserStatus.active);

        return userRepository.save(user);
    }

    public List<User> getUsers() {

        Iterable<User> it = userRepository.findAll();
        List<User> list = new ArrayList<>();
        it.forEach(list::add);
        return list;

    }
}
