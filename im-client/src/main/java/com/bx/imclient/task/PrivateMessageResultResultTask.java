package com.bx.imclient.task;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.bx.imclient.listener.MessageListenerMulticaster;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMListenerType;
import com.bx.imcommon.model.IMSendResult;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrivateMessageResultResultTask extends AbstractMessageResultTask {

    @Resource(name = "IMRedisTemplate")
    private RedisTemplate<String,Object> redisTemplate;

    @Value("${spring.application.name}")
    private String appName;

    private final MessageListenerMulticaster listenerMulticaster;

    @Override
    public void pullMessage() {
        String key = StrUtil.join(":",IMRedisKey.IM_RESULT_PRIVATE_QUEUE,appName);
        JSONObject jsonObject = (JSONObject)redisTemplate.opsForList().leftPop(key,10, TimeUnit.SECONDS);
        if(jsonObject != null) {
            IMSendResult result =  jsonObject.toJavaObject(IMSendResult.class);
            listenerMulticaster.multicast(IMListenerType.PRIVATE_MESSAGE, result);
        }
    }

}
