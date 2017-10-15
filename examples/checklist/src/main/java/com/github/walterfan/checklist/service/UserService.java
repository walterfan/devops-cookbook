package com.github.walterfan.checklist.service;

import com.github.walterfan.checklist.dao.UserRepository;
import com.github.walterfan.checklist.dto.Activation;
import com.github.walterfan.checklist.dto.Registration;
import com.github.walterfan.msa.common.domain.BaseObject;
import com.github.walterfan.msa.common.domain.Token;
import com.github.walterfan.msa.common.domain.User;
import com.github.walterfan.msa.common.domain.UserStatus;
import javassist.NotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.internet.MimeMessage;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private JavaMailSender mailSender;

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

    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return  userRepository.findByEmail(email);
    }

    private void sendEmail(User user) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message);



        Map<String, Object> model = new HashMap<>();

        model.put("user", "qpt");

        helper.setTo("set-your-recipient-email-here@gmail.com");

        helper.setText("<html><body>Here is a cat picture! <img src='cid:id101'/><body></html>", true);

        helper.setSubject("Hi");


        helper.setSubject("Hi");



        mailSender.send(message);

    }

}
