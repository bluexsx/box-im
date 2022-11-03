package com.lx.implatform.imserver.websocket.processor;

import com.lx.common.contant.RedisKey;
import com.lx.common.enums.WSCmdEnum;
import com.lx.common.model.im.SendInfo;
import com.lx.common.model.im.PrivateMessageInfo;
import com.lx.implatform.imserver.websocket.WebsocketChannelCtxHloder;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class PrivateMessageProcessor extends  MessageProcessor<PrivateMessageInfo> {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void process(ChannelHandlerContext ctx, PrivateMessageInfo data) {
        log.info("接收到消息，发送者:{},接收者:{}，内容:{}",data.getSendId(),data.getRecvId(),data.getContent());
        // 一个用户可以同时登陆，所以有多个channel
        ChannelHandlerContext channelCtx = WebsocketChannelCtxHloder.getChannelCtx(data.getRecvId());
        if(channelCtx != null ){
            // 推送消息到用户
            SendInfo sendInfo = new SendInfo();
            sendInfo.setCmd(WSCmdEnum.PRIVATE_MESSAGE.getCode());
            sendInfo.setData(data);
            channelCtx.channel().writeAndFlush(sendInfo);
            // 已读消息推送至redis,等待更新数据库
            String key = RedisKey.IM_READED_PRIVATE_MESSAGE_ID;
            redisTemplate.opsForList().rightPush(key,data.getId());
        }else{
            log.error("未找到WS连接，发送者:{},接收者:{}，内容:{}",data.getSendId(),data.getRecvId(),data.getContent());
        }

    }

}
