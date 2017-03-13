package com.github.walterfan.guestbook.config;

import java.io.InputStream;
import java.util.Properties;

import static ch.qos.logback.core.util.CloseUtil.closeQuietly;


/**
 * Created by walter on 07/11/2016.
 */
public class MessageProperties {

    private Properties prop = new Properties();

    public MessageProperties() {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("jdbc.properties");
        try {
            prop.load(in);
        } catch(Exception e) {
            e.printStackTrace();
        }finally {
            closeQuietly(in);
        }
    }

    public String getJdbcDriver() {
        return prop.getProperty("jdbc.driverClass", "org.h2.Driver");
    }

    public String getJdbcUrl() {
        return prop.getProperty("jdbc.url", "jdbc:h2:mem:mydb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
    }

    public String getJdbcUserName() {
        return prop.getProperty("jdbc.username", "sa");
    }

    public String getJdbcPassword() {
        return prop.getProperty("jdbc.password", "");
    }
}
