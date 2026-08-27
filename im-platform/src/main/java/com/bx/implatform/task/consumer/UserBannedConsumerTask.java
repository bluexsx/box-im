package com.bx.implatform.task.consumer;

import cn.hutool.core.util.StrUtil;
import com.bx.imclient.IMClient;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMForceLogoutType;
import com.bx.imcommon.mq.RedisMQConsumer;
import com.bx.imcommon.mq.RedisMQListener;
import com.bx.implatform.config.props.JwtProperties;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.dto.UserBanDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author: Blue
 * @date: 2024-07-15
 * @version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RedisMQListener(queue = RedisKey.IM_QUEUE_USER_BANNED)
public class UserBannedConsumerTask extends RedisMQConsumer<UserBanDTO> {

    private final IMClient imClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtProperties jwtProperties;

    @Override
    public void onMessage(UserBanDTO dto) {
        log.info("用户被封禁处理,userId:{},原因:{}", dto.getId(), dto.getReason());
        // 写入拒绝访问标记，拦截 HTTP 鉴权与 WS 重连（TTL 对齐 accessToken）
        String key = StrUtil.join(":", IMRedisKey.IM_USER_DENIED, dto.getId());
        redisTemplate.opsForValue().set(key, IMForceLogoutType.BANNED.code(),
            jwtProperties.getAccessTokenExpireIn(), TimeUnit.SECONDS);
        imClient.forceLogout(dto.getId(), IMForceLogoutType.BANNED.code(), dto.getReason());
    }
}
