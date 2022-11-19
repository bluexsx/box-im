package com.bx.implatform;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan(basePackages = {"com.bx.implatform.mapper"})
@SpringBootApplication
public class ImplatformApp {

    public static void main(String[] args) {
        SpringApplication.run(ImplatformApp.class,args);
    }
}
