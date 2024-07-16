package com.bx.imcommon.mq;

import cn.hutool.core.util.StrUtil;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Properties;

/**
 * @author: 谢绍许
 * @date: 2024-07-16
 * @version: 1.0
 */
public class RedisMQTemplate extends RedisTemplate<String, Object> {

    private String version = Strings.EMPTY;

    public String getVersion() {
        if (version.isEmpty()) {
            RedisConnection redisConnection = this.getConnectionFactory().getConnection();
            Properties properties = redisConnection.info();
            version = properties.getProperty("redis_version");
        }
        return version;
    }

    /**
     * 是否支持批量拉取，redis版本大于6.2支持批量拉取
     * @return
     */
    Boolean isSupportBatchPull() {
        String version = getVersion();
        String[] arr = version.split("\\.");
        if (arr.length < 2) {
            return false;
        }
        Integer firVersion = Integer.valueOf(arr[0]);
        Integer secVersion = Integer.valueOf(arr[1]);
        return firVersion > 6 || (firVersion == 6 && secVersion >= 2);
    }
}
