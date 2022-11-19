package com.bx.imclient.task;

import com.bx.common.contant.RedisKey;
import com.bx.common.enums.ListenerType;
import com.bx.common.model.im.SendResult;
import com.bx.imclient.listener.MessageListenerMulticaster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

;

@Slf4j
@Component
public class PullSendResultPrivateMessageTask extends  AbstractPullMessageTask{


    @Qualifier("IMRedisTemplate")
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private MessageListenerMulticaster listenerMulticaster;

    @Override
    public void pullMessage() {
        String key = RedisKey.IM_RESULT_PRIVATE_QUEUE;
        SendResult result =  (SendResult)redisTemplate.opsForList().leftPop(key,10, TimeUnit.SECONDS);
        if(result != null) {
            listenerMulticaster.multicast(ListenerType.PRIVATE_MESSAGE, result);
        }
    }

}
