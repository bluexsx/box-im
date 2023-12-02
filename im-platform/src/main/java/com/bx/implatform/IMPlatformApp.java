package com.bx.implatform;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan(basePackages = {"com.bx.implatform.mapper"})
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})// 禁用secrity
public class IMPlatformApp {

    public static void main(String[] args) {
        SpringApplication.run(IMPlatformApp.class, args);
    }
}
