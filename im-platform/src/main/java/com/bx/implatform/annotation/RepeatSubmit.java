package com.bx.implatform.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 *  防止表单重复提交注解
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {


    /**
     * 间隔时间，小于此时间视为重复提交
     */
    int interval() default 5000;

    /**
     * 间隔时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * 提示消息
     */
    String message() default "请勿提交重复请求";

}
