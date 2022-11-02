package com.lx.implatform.imserver.websocket.processor;

import cn.hutool.core.bean.BeanUtil;
import com.lx.common.contant.RedisKey;
import com.lx.common.enums.WSCmdEnum;
import com.lx.common.model.im.HeartbeatInfo;
import com.lx.common.model.im.SendInfo;
import com.lx.implatform.imserver.websocket.WebsocketChannelCtxHloder;
import com.lx.implatform.imserver.websocket.WebsocketServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class HeartbeatProcessor implements  MessageProcessor<HeartbeatInfo> {


    @Autowired
    private WebsocketServer WSServer;

    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    @Override
    public void process(ChannelHandlerContext ctx, HeartbeatInfo beatInfo) {
        log.info("接收到心跳，userId:{}",beatInfo.getUserId());

        // 绑定用户和channel
        WebsocketChannelCtxHloder.addChannelCtx(beatInfo.getUserId(),ctx);
        // 设置属性
        AttributeKey<Long> attr = AttributeKey.valueOf("USER_ID");
        ctx.channel().attr(attr).set(beatInfo.getUserId());

        // 在redis上记录每个user的channelId，15秒没有心跳，则自动过期
        String key = RedisKey.IM_USER_SERVER_ID+beatInfo.getUserId();
        redisTemplate.opsForValue().set(key, WSServer.getServerId(),15, TimeUnit.SECONDS);

        // 响应ws
        SendInfo sendInfo = new SendInfo();
        sendInfo.setCmd(WSCmdEnum.HEARTBEAT.getCode());
        ctx.channel().writeAndFlush(sendInfo);
    }

}
