package com.lx.implatform;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan(basePackages = {"com.lx.implatform.mapper"})
@ComponentScan(basePackages={"com.lx"})
@SpringBootApplication
public class ImplatformApp {

    public static void main(String[] args) {
        SpringApplication.run(ImplatformApp.class);
    }
}
