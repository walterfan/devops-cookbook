package com.github.walterfan.hello;

import com.github.walterfan.config.HelloConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by yafan on 7/10/2017.
 */
public class HelloSpring {
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(HelloConfig.class)) {



        }
    }
}