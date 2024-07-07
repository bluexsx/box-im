package com.bx.implatform.service;

import cn.hutool.core.util.StrUtil;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.entity.User;
import com.bx.implatform.session.NotifySession;
import com.bx.implatform.service.thirdparty.UniPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 私聊离线通知服务
 *
 * @author: blue
 * @date: 2024-07-06
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class INotifyPrivateService {
    private final UniPushService uniPushService;
    private final IUserService userService;
    private final RedisTemplate<String, Object> redisTemplate;
    @Value("${notify.enable}")
    private Boolean enable = false;
    @Value("${notify.max.private}")
    private Integer max = -1;
    public void sendMessage(Long sendId, Long recvId, String content) {
        if(!enable){
            return;
        }
        NotifySession session = findNotifySession(sendId, recvId);
        if (Objects.isNull(session)) {
            session = createNotifySession(sendId, recvId);
        }
        // 未上报cid,无法推送
        if (StrUtil.isEmpty(session.getCid())) {
            log.info("用户'{}'未上报cid,无法推送离线通知", recvId);
            return;
        }
        // 已达到最大数量
        if (max > 0 && session.getCount() >= max) {
            log.info("用户'{}'已到达推送数量上线,不再推送离线通知", recvId);
            return;
        }
        // 消息数量加1
        session.setCount(session.getCount()+1);
        String body = String.format("%s:%s", session.getSendNickName(),content);
        // 大于1条时需要展示数量
        if (session.getCount() > 1) {
            body = String.format("[%d条] ", session.getCount()) + body;
        }
        uniPushService.pushByCid(session,body);
        // 保存会话
        saveNotifySession(session,sendId,recvId);
    }

    public void removeNotifySession(Long sendId, Long recvId){
        String key = StrUtil.join(":", RedisKey.IM_OFFLINE_NOTIFY_PRIVATE, "*", recvId);
        Set<String> keys =  redisTemplate.keys(key);
        redisTemplate.delete(keys);
    }

    private NotifySession createNotifySession(Long sendId, Long recvId) {
        String key = StrUtil.join(":", RedisKey.IM_OFFLINE_NOTIFY_PRIVATE, sendId, recvId);
        User sendUser = userService.getById(sendId);
        User recvUser = userService.getById(recvId);
        NotifySession session = new NotifySession();
        session.setCount(0);
        session.setCid(recvUser.getCid());
        session.setTitle(sendUser.getNickName());
        session.setSendNickName(sendUser.getNickName());
        session.setNotifyId(Math.abs(key.hashCode()));
        session.setLogo(sendUser.getHeadImageThumb());
        redisTemplate.opsForValue().set(key, session, 30, TimeUnit.DAYS);
        return session;
    }

    private NotifySession findNotifySession(Long sendId, Long recvId) {
        String key = StrUtil.join(":", RedisKey.IM_OFFLINE_NOTIFY_PRIVATE, sendId, recvId);
        return (NotifySession)redisTemplate.opsForValue().get(key);
    }

    private void saveNotifySession(NotifySession session, Long sendId, Long recvId) {
        String key = StrUtil.join(":", RedisKey.IM_OFFLINE_NOTIFY_PRIVATE, sendId, recvId);
        redisTemplate.opsForValue().set(key, session);
    }

}
