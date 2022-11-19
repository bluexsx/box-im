package com.bx.imserver.task;

import com.bx.imcommon.contant.RedisKey;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.model.GroupMessageInfo;
import com.bx.imcommon.model.IMRecvInfo;
import com.bx.imserver.processor.MessageProcessor;
import com.bx.imserver.processor.ProcessorFactory;
import com.bx.imserver.ws.WebsocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class PullUnreadGroupMessageTask extends  AbstractPullMessageTask {

    @Autowired
    private WebsocketServer WSServer;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;



    @Override
    public void pullMessage() {
        // 从redis拉取未读消息
        String key = RedisKey.IM_UNREAD_GROUP_QUEUE + WSServer.getServerId();
        List messageInfos = redisTemplate.opsForList().range(key,0,-1);
        for(Object o: messageInfos){
            redisTemplate.opsForList().leftPop(key);
            IMRecvInfo<GroupMessageInfo> recvInfo = (IMRecvInfo)o;
            MessageProcessor processor = ProcessorFactory.createProcessor(IMCmdType.GROUP_MESSAGE);
            processor.process(recvInfo);
        }
    }



}
