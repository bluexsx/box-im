package com.bx.imserver.netty.processor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.bx.imcommon.contant.IMConstant;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.enums.IMEventType;
import com.bx.imcommon.enums.IMForceLogoutType;
import com.bx.imcommon.model.*;
import com.bx.imcommon.mq.RedisMQTemplate;
import com.bx.imcommon.util.JwtUtil;
import com.bx.imserver.constant.ChannelAttrKey;
import com.bx.imserver.netty.IMServerGroup;
import com.bx.imserver.netty.UserChannelCtxMap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginProcessor extends AbstractMessageProcessor<IMLoginInfo> {

    private final RedisMQTemplate redisMQTemplate;

    @Value("${jwt.accessToken.secret}")
    private String accessTokenSecret;

    @Override
    public void process(ChannelHandlerContext ctx, IMLoginInfo loginInfo) {
        if (!JwtUtil.checkSign(loginInfo.getAccessToken(), accessTokenSecret)) {
            ctx.channel().close();
            log.warn("用户token校验不通过，强制下线,token:{}", loginInfo.getAccessToken());
            return;
        }
        String strInfo = JwtUtil.getInfo(loginInfo.getAccessToken());
        IMSessionInfo sessionInfo = JSON.parseObject(strInfo, IMSessionInfo.class);
        Long userId = sessionInfo.getUserId();
        Integer terminal = sessionInfo.getTerminal();
        // 封禁/注销等拒绝建立长连接
        if (Boolean.TRUE.equals(redisMQTemplate.hasKey(StrUtil.join(":", IMRedisKey.IM_USER_DENIED, userId)))) {
            ctx.channel().close();
            log.warn("用户不可用，拒绝连接,userId:{}", userId);
            return;
        }
        log.info("用户登录，userId:{}", userId);
        String key = IMRedisKey.userServerIdKey(userId, terminal);
        Object serverId = redisMQTemplate.opsForValue().get(key);
        if (!Objects.isNull(serverId)) {
            // 用户已在线，强制使其下线
            if (IMServerGroup.serverId.equals(Long.parseLong(serverId.toString()))) {
                // 两次连的是同一个服务器
                ChannelHandlerContext context = UserChannelCtxMap.getChannelCtx(userId, terminal);
                if (context != null && !ctx.channel().id().equals(context.channel().id())) {
                    AttributeKey<String> devIdAttr = AttributeKey.valueOf(ChannelAttrKey.DEVICE_ID);
                    String devId = context.channel().attr(devIdAttr).get();
                    if (StrUtil.isEmpty(loginInfo.getDevId()) || !loginInfo.getDevId().equals(devId)) {
                        // 不允许多地登录,强制下线
                        IMForceLogoutData data = new IMForceLogoutData();
                        data.setType(IMForceLogoutType.KICKED.code());
                        IMSendInfo<IMForceLogoutData> sendInfo = new IMSendInfo<>();
                        sendInfo.setCmd(IMCmdType.FORCE_LOGOUT.code());
                        sendInfo.setData(data);
                        context.channel().writeAndFlush(sendInfo).addListener(ChannelFutureListener.CLOSE);
                        log.info("异地登录，强制下线,userId:{},终端:{}", userId, terminal);
                    }
                }
            } else {
                // 连的是不同的服务器，投递下线命令到上次连的服务器
                IMForceLogoutInfo logoutInfo = new IMForceLogoutInfo();
                logoutInfo.setUserId(userId);
                logoutInfo.setTerminal(terminal);
                logoutInfo.setDevId(loginInfo.getDevId());
                logoutInfo.setType(IMForceLogoutType.KICKED.code());
                String queueKey = StrUtil.join(":", IMRedisKey.IM_USER_FORCE_LOGOUT_QUEUE, serverId);
                redisMQTemplate.opsForList().rightPush(queueKey, logoutInfo);
            }
        }
        // 绑定用户和channel
        UserChannelCtxMap.addChannelCtx(userId, terminal, ctx);
        // 设置用户id属性
        AttributeKey<Long> userIdAttr = AttributeKey.valueOf(ChannelAttrKey.USER_ID);
        ctx.channel().attr(userIdAttr).set(userId);
        // 设置用户终端类型
        AttributeKey<Integer> terminalAttr = AttributeKey.valueOf(ChannelAttrKey.TERMINAL_TYPE);
        ctx.channel().attr(terminalAttr).set(terminal);
        // 设置用户设备id
        AttributeKey<String> devIdAttr = AttributeKey.valueOf(ChannelAttrKey.DEVICE_ID);
        ctx.channel().attr(devIdAttr).set(loginInfo.getDevId());
        // 初始化心跳次数
        AttributeKey<Long> heartBeatAttr = AttributeKey.valueOf(ChannelAttrKey.HEARTBEAT_TIMES);
        ctx.channel().attr(heartBeatAttr).set(0L);
        // 在redis上记录每个user的channelId，超时无心跳则自动过期
        redisMQTemplate.opsForValue().set(key, IMServerGroup.serverId, IMConstant.ONLINE_TIMEOUT_SECOND, TimeUnit.SECONDS);
        // 推送用户上线事件给业务层
        IMUserEvent event = new IMUserEvent();
        event.setEventType(IMEventType.ONLINE.code());
        event.setUserInfo(new IMUserInfo(userId, terminal));
        key = IMRedisKey.IM_USER_EVENT_QUEUE;
        redisMQTemplate.opsForList().rightPush(key, event);
        // 响应ws
        IMSendInfo<Object> sendInfo = new IMSendInfo<>();
        sendInfo.setCmd(IMCmdType.LOGIN.code());
        ctx.channel().writeAndFlush(sendInfo);
    }

    @Override
    public IMLoginInfo transForm(Object o) {
        HashMap map = (HashMap) o;
        return BeanUtil.fillBeanWithMap(map, new IMLoginInfo(), false);
    }
}
