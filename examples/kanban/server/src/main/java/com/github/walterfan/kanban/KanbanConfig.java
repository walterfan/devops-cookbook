package com.github.walterfan.kanban;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Spring configuration for the app. Picked up from web.xml.
 */
@Import({
        KanbanMetrics.class

})
@Configuration
@EnableWebSecurity
public class KanbanConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    protected Environment env;

    protected final Logger log = LoggerFactory.getLogger(getClass());

}
