package com.bx.imclient.task;

import com.alibaba.fastjson.JSONObject;
import com.bx.imclient.listener.MessageListenerMulticaster;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMListenerType;
import com.bx.imcommon.model.IMSendResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

;

@Slf4j
@Component
@AllArgsConstructor
public class PrivateMessageResultResultTask extends AbstractMessageResultTask {


    @Resource(name = "IMRedisTemplate")
    private RedisTemplate<String,Object> redisTemplate;

    private MessageListenerMulticaster listenerMulticaster;

    @Override
    public void pullMessage() {
        String key = IMRedisKey.IM_RESULT_PRIVATE_QUEUE;
        JSONObject jsonObject = (JSONObject)redisTemplate.opsForList().leftPop(key,10, TimeUnit.SECONDS);
        if(jsonObject != null) {
            IMSendResult result =  jsonObject.toJavaObject(IMSendResult.class);
            listenerMulticaster.multicast(IMListenerType.PRIVATE_MESSAGE, result);
        }
    }

}
