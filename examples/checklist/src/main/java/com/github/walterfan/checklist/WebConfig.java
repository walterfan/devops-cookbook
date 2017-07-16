package com.github.walterfan.checklist;

import com.github.walterfan.msa.common.domain.BaseObject;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by yafan on 16/7/2017.
 */
@Configuration
@EnableAutoConfiguration
@EntityScan(basePackageClasses=BaseObject.class)

public class WebConfig {
}
