package com.github.walterfan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.walterfan.checklist.dao.UserRepository;
import com.github.walterfan.checklist.service.UserService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by walterfan on 27/2/2017.
 */
@Configuration
public class ChecklistTestConfig extends WebMvcConfigurationSupport {

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        super.configureHandlerExceptionResolvers(exceptionResolvers);
        //exceptionResolvers.add(0, new ExceptionResolver());
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.configureMessageConverters(converters);
        if (converters == null) converters = new ArrayList<>();
        // we know we should be user fasterxml but we're not ready yet
        MappingJackson2HttpMessageConverter conv = new MappingJackson2HttpMessageConverter();
        // This ObjectMapper is properly configured, including date formatting.
        //conv.setObjectMapper(JsonUtil.getObjectMapper());
        converters.add(0, conv);
    }


    @Bean
    public UserService userService() {
        return Mockito.mock(UserService.class, Mockito.withSettings().defaultAnswer(Mockito.RETURNS_SMART_NULLS));
    }

    @Bean
    public UserRepository userRepository() {
        return Mockito.mock(UserRepository.class, Mockito.withSettings().defaultAnswer(Mockito.RETURNS_SMART_NULLS));
    }

    @Bean
    public HttpServletRequest servletRequest() {
        return Mockito.mock(HttpServletRequest.class, Mockito.RETURNS_SMART_NULLS);
    }

    @Bean
    public ServletContext getServletContext() {
        return Mockito.mock(ServletContext.class, Mockito.withSettings().defaultAnswer(Mockito.RETURNS_SMART_NULLS));
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
