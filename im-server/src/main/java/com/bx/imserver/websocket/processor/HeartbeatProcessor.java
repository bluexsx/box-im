package com.bx.imserver.websocket.processor;

import cn.hutool.core.bean.BeanUtil;
import com.bx.common.enums.WSCmdEnum;
import com.bx.common.model.im.HeartbeatInfo;
import com.bx.common.model.im.SendInfo;
import com.bx.imserver.websocket.WebsocketServer;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Slf4j
@Component
public class HeartbeatProcessor extends   MessageProcessor<HeartbeatInfo> {


    @Autowired
    private WebsocketServer WSServer;

    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    @Override
    public void process(ChannelHandlerContext ctx, HeartbeatInfo beatInfo) {
        // 响应ws
        SendInfo sendInfo = new SendInfo();
        sendInfo.setCmd(WSCmdEnum.HEART_BEAT.getCode());
        ctx.channel().writeAndFlush(sendInfo);
    }


    @Override
    public HeartbeatInfo transForm(Object o) {
        HashMap map = (HashMap)o;
        HeartbeatInfo heartbeatInfo = BeanUtil.fillBeanWithMap(map, new HeartbeatInfo(), false);
        return  heartbeatInfo;
    }
}
