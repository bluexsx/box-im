package com.bx.imclient.task;

import com.alibaba.fastjson.JSONObject;
import com.bx.imclient.listener.MessageListenerMulticaster;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMListenerType;
import com.bx.imcommon.model.IMSendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PullSendResultGroupMessageTask extends  AbstractPullMessageTask{

    @Qualifier("IMRedisTemplate")
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private MessageListenerMulticaster listenerMulticaster;

    @Override
    public void pullMessage() {
        String key = IMRedisKey.IM_RESULT_GROUP_QUEUE;
        JSONObject jsonObject = (JSONObject)redisTemplate.opsForList().leftPop(key,10, TimeUnit.SECONDS);
        if(jsonObject != null) {
            IMSendResult result =  jsonObject.toJavaObject(IMSendResult.class);
            listenerMulticaster.multicast(IMListenerType.GROUP_MESSAGE,result);
        }
    }

}
