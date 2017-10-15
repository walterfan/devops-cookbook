package com.github.walterfan.msa.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class WebApplication extends WebMvcConfigurerAdapter {


    public static void main(String[] args) {
		SpringApplication.run(WebApplication.class, args);
	}

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/kanban");
    }



}
