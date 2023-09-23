package com.bx.imserver.netty.processor;

import cn.hutool.core.bean.BeanUtil;
import com.bx.imcommon.contant.Constant;
import com.bx.imcommon.contant.RedisKey;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.model.HeartbeatInfo;
import com.bx.imcommon.model.IMSendInfo;
import com.bx.imserver.constant.ChannelAttrKey;
import com.bx.imserver.netty.ws.WebSocketServer;
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
public class HeartbeatProcessor extends AbstractMessageProcessor<HeartbeatInfo> {


    @Autowired
    private WebSocketServer wsServer;

    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    @Override
    public void process(ChannelHandlerContext ctx, HeartbeatInfo beatInfo) {
        // 响应ws
        IMSendInfo sendInfo = new IMSendInfo();
        sendInfo.setCmd(IMCmdType.HEART_BEAT.code());
        ctx.channel().writeAndFlush(sendInfo);

        // 设置属性
        AttributeKey<Long> heartBeatAttr = AttributeKey.valueOf(ChannelAttrKey.HEARTBEAT_TIMES);
        Long heartbeatTimes = ctx.channel().attr(heartBeatAttr).get();
        ctx.channel().attr(heartBeatAttr).set(++heartbeatTimes);
        if(heartbeatTimes%10 == 0){
            // 每心跳10次，用户在线状态续一次命
            AttributeKey<Long> userIdAttr = AttributeKey.valueOf(ChannelAttrKey.USER_ID);
            Long userId = ctx.channel().attr(userIdAttr).get();
            AttributeKey<Integer> terminalAttr = AttributeKey.valueOf(ChannelAttrKey.TERMINAL_TYPE);
            Integer ternimal = ctx.channel().attr(terminalAttr).get();
            String key = String.join(":",RedisKey.IM_USER_SERVER_ID,userId.toString(),ternimal.toString());
            redisTemplate.expire(key, Constant.ONLINE_TIMEOUT_SECOND, TimeUnit.SECONDS);
        }
    }


    @Override
    public HeartbeatInfo transForm(Object o) {
        HashMap map = (HashMap)o;
        HeartbeatInfo heartbeatInfo = BeanUtil.fillBeanWithMap(map, new HeartbeatInfo(), false);
        return  heartbeatInfo;
    }
}
