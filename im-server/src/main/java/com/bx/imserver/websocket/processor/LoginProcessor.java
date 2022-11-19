package com.bx.imserver.websocket.processor;

import cn.hutool.core.bean.BeanUtil;
import com.bx.imcommon.contant.Constant;
import com.bx.imcommon.contant.RedisKey;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.model.im.LoginInfo;
import com.bx.imcommon.model.im.SendInfo;
import com.bx.imserver.websocket.WebsocketChannelCtxHolder;
import com.bx.imserver.websocket.WebsocketServer;
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
public class LoginProcessor extends   MessageProcessor<LoginInfo> {


    @Autowired
    private WebsocketServer WSServer;

    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    @Override
    synchronized public void process(ChannelHandlerContext ctx, LoginInfo loginInfo) {
        log.info("用户登录，userId:{}",loginInfo.getUserId());
        ChannelHandlerContext context = WebsocketChannelCtxHolder.getChannelCtx(loginInfo.getUserId());
        if(context != null){
            // 不允许多地登录,强制下线
            SendInfo sendInfo = new SendInfo();
            sendInfo.setCmd(IMCmdType.FORCE_LOGUT.getCode());
            context.channel().writeAndFlush(sendInfo);
        }
        // 绑定用户和channel
        WebsocketChannelCtxHolder.addChannelCtx(loginInfo.getUserId(),ctx);
        // 设置用户id属性
        AttributeKey<Long> attr = AttributeKey.valueOf("USER_ID");
        ctx.channel().attr(attr).set(loginInfo.getUserId());
        // 心跳次数
        attr = AttributeKey.valueOf("HEARTBEAt_TIMES");
        ctx.channel().attr(attr).set(0L);
        // 在redis上记录每个user的channelId，15秒没有心跳，则自动过期
        String key = RedisKey.IM_USER_SERVER_ID+loginInfo.getUserId();
        redisTemplate.opsForValue().set(key, WSServer.getServerId(), Constant.ONLINE_TIMEOUT_SECOND, TimeUnit.SECONDS);
        // 响应ws
        SendInfo sendInfo = new SendInfo();
        sendInfo.setCmd(IMCmdType.LOGIN.getCode());
        ctx.channel().writeAndFlush(sendInfo);
    }


    @Override
    public LoginInfo transForm(Object o) {
        HashMap map = (HashMap)o;
        LoginInfo loginInfo = BeanUtil.fillBeanWithMap(map, new LoginInfo(), false);
        return  loginInfo;
    }
}
