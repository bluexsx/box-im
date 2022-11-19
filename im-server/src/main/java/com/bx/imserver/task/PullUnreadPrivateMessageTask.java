package com.bx.imserver.task;


import com.bx.common.contant.RedisKey;
import com.bx.common.enums.IMCmdType;
import com.bx.common.model.im.IMRecvInfo;
import com.bx.common.model.im.PrivateMessageInfo;
import com.bx.imserver.websocket.WebsocketServer;
import com.bx.imserver.websocket.processor.MessageProcessor;
import com.bx.imserver.websocket.processor.ProcessorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
public class PullUnreadPrivateMessageTask extends  AbstractPullMessageTask {

    @Autowired
    private WebsocketServer WSServer;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void pullMessage() {
        // 从redis拉取未读消息
        String key = RedisKey.IM_UNREAD_PRIVATE_QUEUE + WSServer.getServerId();
        List messageInfos = redisTemplate.opsForList().range(key,0,-1);
        for(Object o: messageInfos){
            redisTemplate.opsForList().leftPop(key);
            IMRecvInfo<PrivateMessageInfo> recvInfo = (IMRecvInfo)o;
            MessageProcessor processor = ProcessorFactory.createProcessor(IMCmdType.PRIVATE_MESSAGE);
            processor.process(recvInfo);

        }
    }

}
