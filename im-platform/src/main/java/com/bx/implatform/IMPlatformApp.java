package com.bx.implatform;

import cn.hutool.core.util.StrUtil;
import com.bx.implatform.contant.RedisKey;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan(basePackages = {"com.bx.implatform.mapper"})
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})// 禁用secrity
public class IMPlatformApp {

    public static void main(String[] args) {
        SpringApplication.run(IMPlatformApp.class, args);
    }

}
