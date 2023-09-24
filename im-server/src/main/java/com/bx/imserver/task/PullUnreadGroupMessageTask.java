package com.bx.imserver.task;

import com.bx.imcommon.contant.RedisKey;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.model.IMRecvInfo;
import com.bx.imserver.netty.IMServerGroup;
import com.bx.imserver.netty.processor.AbstractMessageProcessor;
import com.bx.imserver.netty.processor.ProcessorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PullUnreadGroupMessageTask extends  AbstractPullMessageTask {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void pullMessage() {
        // 从redis拉取未读消息
        String key = String.join(":",RedisKey.IM_UNREAD_GROUP_QUEUE,IMServerGroup.serverId+"");
        IMRecvInfo recvInfo = (IMRecvInfo)redisTemplate.opsForList().leftPop(key,10, TimeUnit.SECONDS);
        if(recvInfo != null){
            AbstractMessageProcessor processor = ProcessorFactory.createProcessor(IMCmdType.GROUP_MESSAGE);
            processor.process(recvInfo);
        }
    }

}
