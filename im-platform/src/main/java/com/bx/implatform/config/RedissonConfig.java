package com.bx.implatform.config;

import cn.hutool.core.util.StrUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Blue
 * @date: 2024-06-09
 * @version: 1.0
 */

@Configuration
@ConditionalOnClass(Config.class)
@EnableConfigurationProperties(RedisProperties.class)
public class RedissonConfig {

    @Bean
    RedissonClient redissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        config.setCodec(new StringCodec());
        String address = "redis://" + redisProperties.getHost()+":"+redisProperties.getPort();
        SingleServerConfig serverConfig = config.useSingleServer()
            .setAddress(address)
            .setDatabase(redisProperties.getDatabase());
        if(StrUtil.isNotEmpty(redisProperties.getPassword())) {
            serverConfig.setPassword(redisProperties.getPassword());
        }

        return Redisson.create(config);
    }


}
