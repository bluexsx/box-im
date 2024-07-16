package com.bx.imcommon.mq;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * redis 队列消费监听注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RedisMQListener {

    /**
     * 队列，也是redis的key
     */
    String queue();

    /**
     * 一次性拉取的数据数量
     */
    int batchSize() default 1;

    /**
     * 拉取间隔周期，单位:ms
     */
    int period() default 100;
}
