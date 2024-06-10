package com.bx.implatform.util;

import cn.hutool.core.util.StrUtil;
import com.bx.implatform.contant.RedisKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author: 谢绍许
 * @date: 2024-06-10
 * @version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserStateUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    public void setBusy(Long userId){
        String key = StrUtil.join(":", RedisKey.IM_USER_STATE,userId);
        redisTemplate.opsForValue().set(key,1,30, TimeUnit.SECONDS);
    }

    public void expire(Long userId){
        String key = StrUtil.join(":", RedisKey.IM_USER_STATE,userId);
        redisTemplate.expire(key,30, TimeUnit.SECONDS);
    }

    public void setFree(Long userId){
        String key = StrUtil.join(":", RedisKey.IM_USER_STATE,userId);
        redisTemplate.delete(key);
    }

    public Boolean isBusy(Long userId){
        String key = StrUtil.join(":", RedisKey.IM_USER_STATE,userId);
        return  redisTemplate.hasKey(key);
    }

}
