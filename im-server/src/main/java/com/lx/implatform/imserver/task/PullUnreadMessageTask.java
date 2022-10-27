package com.lx.implatform.imserver.task;


import com.lx.common.contant.RedisKey;
import com.lx.common.enums.WSCmdEnum;
import com.lx.common.model.im.SingleMessageInfo;
import com.lx.implatform.imserver.websocket.WebsocketChannelCtxHloder;
import com.lx.implatform.imserver.websocket.WebsocketServer;
import com.lx.implatform.imserver.websocket.processor.MessageProcessor;
import com.lx.implatform.imserver.websocket.processor.ProcessorFactory;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
public class PullUnreadMessageTask {


    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Scheduled(fixedRate=100)
    public void pullUnreadMessage() {

        // 从redis拉取未读消息
        String key = RedisKey.IM_UNREAD_MESSAGE + WebsocketServer.LOCAL_SERVER_ID;
        List messageInfos = redisTemplate.opsForList().range(key,0,-1);
        for(Object o: messageInfos){
            redisTemplate.opsForList().leftPop(key);
            SingleMessageInfo messageInfo = (SingleMessageInfo)o;
            ChannelHandlerContext ctx = WebsocketChannelCtxHloder.getChannelCtx(messageInfo.getRecvUserId());
            if(ctx != null){
                MessageProcessor processor = ProcessorFactory.createProcessor(WSCmdEnum.SINGLE_MESSAGE);
                processor.process(ctx,messageInfo);
            }
        }
    }
}
