package com.bx.imcommon.mq;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class RedisMQConfig {

    @Bean
    public RedisMQTemplate redisMQTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisMQTemplate redisMQTemplate = new RedisMQTemplate();
        redisMQTemplate.setConnectionFactory(redisConnectionFactory);
        // 设置值（value）的序列化采用FastJsonRedisSerializer
        redisMQTemplate.setValueSerializer(fastJsonRedisSerializer());
        redisMQTemplate.setHashValueSerializer(fastJsonRedisSerializer());
        // 设置键（key）的序列化采用StringRedisSerializer。
        redisMQTemplate.setKeySerializer(new StringRedisSerializer());
        redisMQTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisMQTemplate.afterPropertiesSet();
        return redisMQTemplate;
    }

    @Bean
    public FastJsonRedisSerializer fastJsonRedisSerializer(){
        return new FastJsonRedisSerializer<>(Object.class);
    }

}
