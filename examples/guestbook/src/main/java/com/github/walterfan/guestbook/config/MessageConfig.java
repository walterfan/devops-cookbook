package com.github.walterfan.guestbook.config;

import com.github.walterfan.guestbook.controller.IndexController;
import com.github.walterfan.guestbook.controller.MessageController;
import com.github.walterfan.guestbook.dao.MessageDao;
import com.github.walterfan.guestbook.dao.MessageMapper;
import com.github.walterfan.guestbook.service.MessageService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import javax.sql.DataSource;
import java.sql.Driver;
import java.util.Properties;

/**
 * Created by walter on 06/11/2016.
 */
@Configuration
@EnableWebMvc
@Import({
        IndexController.class,
        MessageController.class
})
public class MessageConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private Environment env;

    @Bean
    public MessageService messageService() {
        return new MessageService();
    }

    @Bean
    public MessageProperties messageProperties()  {
        return new MessageProperties();
    }


    @Bean
    public DataSource dataSource() throws ClassNotFoundException {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass((Class<? extends Driver>) Class.forName(messageProperties().getJdbcDriver()));
        dataSource.setUsername(messageProperties().getJdbcUserName());
        dataSource.setUrl(messageProperties().getJdbcUrl());
        dataSource.setPassword(messageProperties().getJdbcPassword());

        return dataSource;
    }

    @Bean
    public DataSourceTransactionManager transactionManager() throws ClassNotFoundException {
        return new DataSourceTransactionManager(dataSource());
    }


    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(dataSource());
        return (SqlSessionFactory) sqlSessionFactory.getObject();
    }

    @Bean
    public MessageDao messageDao() throws Exception {
        SqlSessionFactory sessionFactory = sqlSessionFactory();
        sessionFactory.getConfiguration().addMapper(MessageMapper.class);

        SqlSessionTemplate sessionTemplate = new SqlSessionTemplate(sqlSessionFactory());
        return sessionTemplate.getMapper(MessageMapper.class);
    }

    //--view

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.viewResolver(viewResolver());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("/static/");
    }
    @Bean
    public ViewResolver viewResolver() {
        FreeMarkerViewResolver viewResolver = new FreeMarkerViewResolver();
        viewResolver.setCache(true);
        viewResolver.setSuffix(".ftl");
        viewResolver.setContentType("text/html; charset=UTF-8");
        viewResolver.setRequestContextAttribute("context");
        viewResolver.setExposeRequestAttributes(true);
        viewResolver.setRedirectHttp10Compatible(false);
        return viewResolver;
    }

    @Bean
    public FreeMarkerConfigurer freeMarkerConfigurer() {
        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();

        configurer.setTemplateLoaderPath("/WEB-INF/views/");
        configurer.setDefaultEncoding("UTF-8");

        Properties settings = new Properties();
        settings.put("default_encoding", "UTF-8");
        //settings.put("auto_import", "/layout.ftl as layout");
        configurer.setFreemarkerSettings(settings);

        return configurer;
    }
}
