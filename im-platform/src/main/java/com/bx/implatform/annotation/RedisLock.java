package com.bx.implatform.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解
 */
@Retention(RetentionPolicy.RUNTIME)//运行时生效
@Target(ElementType.METHOD)//作用在方法上
public @interface RedisLock {

    /**
     * key的前缀,prefixKey+key就是redis的key
     */
    String prefixKey() ;

    /**
     * spel 表达式
     */
    String key() default "";

    /**
     * 等待锁的时间，默认-1，不等待直接失败,redisson默认也是-1
     */
    int waitTime() default -1;

    /**
     * 等待锁的时间单位，默认毫秒
     *
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;

}
