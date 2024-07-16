package com.bx.imclient.task;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.bx.imclient.listener.MessageListenerMulticaster;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMListenerType;
import com.bx.imcommon.model.IMSendResult;
import com.bx.imcommon.mq.RedisMQTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrivateMessageResultResultTask extends AbstractMessageResultTask {

    @Autowired
    private RedisMQTemplate redisMQTemplate;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${im.result.batch:100}")
    private int batchSize;

    private final MessageListenerMulticaster listenerMulticaster;

    @Override
    public void pullMessage() {
        List<IMSendResult> results;
        do {
            results = loadBatch();
            if(!results.isEmpty()){
                listenerMulticaster.multicast(IMListenerType.PRIVATE_MESSAGE, results);
            }
        } while (results.size() >= batchSize);
    }

    List<IMSendResult> loadBatch() {
        String key = StrUtil.join(":", IMRedisKey.IM_RESULT_PRIVATE_QUEUE, appName);
        //这个接口redis6.2以上才支持
        //List<Object> list = redisMQTemplate.opsForList().leftPop(key, batchSize);
        List<IMSendResult> results = new LinkedList<>();
        JSONObject jsonObject = (JSONObject) redisMQTemplate.opsForList().leftPop(key);
        while (!Objects.isNull(jsonObject) && results.size() < batchSize) {
            results.add(jsonObject.toJavaObject(IMSendResult.class));
            jsonObject = (JSONObject) redisMQTemplate.opsForList().leftPop(key);
        }
        return results;
    }

}
