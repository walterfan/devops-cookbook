package com.github.walterfan.hello;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Created by yafan on 15/10/2017.
 */
@Target({ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface DurationTimer {

    String name() default "";

    long logThreshold() default 0;

    //default timeunit μs
    TimeUnit thresholdTimeUnit() default TimeUnit.MICROSECONDS;
}


