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
public class IMPlatformApp implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(IMPlatformApp.class, args);
    }

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Override
    public void run(ApplicationArguments args) throws Exception {
//        String matchKey = RedisKey.IM_GROUP_READED_POSITION+"*";
//        Set<String> keys = redisTemplate.keys(matchKey);
//        Map<String, Map<String,Object>> map = new HashMap<>();
//        for(String key:keys){
//            String[] arr = key.split(":");
//            String groupId = arr[4];
//            String userId = arr[5];
//            Object messageId = redisTemplate.opsForValue().get(key);
//            String newKey = StrUtil.join(":",RedisKey.IM_GROUP_READED_POSITION,groupId);
//            redisTemplate.opsForHash().put(newKey,userId,messageId);
//            redisTemplate.delete(key);
//            log.info("key:{},value:{}",newKey,messageId);
//        }
    }
}
