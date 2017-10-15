package com.github.walterfan.hello;

import com.codahale.metrics.MetricRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * Created by yafan on 15/10/2017.
 */
@Aspect
public class DurationTimerAspect {


    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MetricRegistry metricRegistry;

    public <T> T proxy(T o) {
        final AspectJProxyFactory factory = new AspectJProxyFactory(o);
        factory.setProxyTargetClass(true);
        factory.addAspect(this);
        return factory.getProxy();
    }

    @Around("@annotation( durationAnnotation ) ")
    public Object measureTimeRequest(final ProceedingJoinPoint pjp, DurationTimer durationAnnotation) throws Throwable {
        final long start = System.nanoTime();
        final Object retVal = pjp.proceed();

        String timerName = durationAnnotation.name();
        if("".equals(timerName)) {
            timerName = pjp.getSignature().toShortString();
        }
        TimeUnit timeUnit = durationAnnotation.thresholdTimeUnit();
        long threshold = durationAnnotation.logThreshold();
        //System.out.println("timerName=" + timerName);
        try {
            long difference = timeUnit.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);

            if(difference > threshold) {
                metricRegistry.histogram(timerName).update(difference);
                logger.info("Duration of {}: {} {}, threshold: {} {}", timerName, difference, timeUnit.name(), threshold, timeUnit.name());
            }

        } catch (Exception ex) {
            logger.error("Cannot measure api timing.... :" + ex.getMessage(), ex);
        }
        return retVal;
    }

}
