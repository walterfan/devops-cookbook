package com.github.walterfan;

import com.github.walterfan.config.HelloConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

/**
 * Created by yafan on 9/10/2017.
 */
@ContextConfiguration(classes = { HelloConfig.class })
public class HelloRedis {

    private static final Logger logger = LoggerFactory.getLogger(HelloRedis.class);

    public static void main(String[] args) {

    }
}
