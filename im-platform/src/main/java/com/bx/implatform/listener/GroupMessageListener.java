package com.bx.implatform.listener;

import com.bx.common.enums.ListenerType;
import com.bx.common.enums.MessageType;
import com.bx.common.model.im.GroupMessageInfo;
import com.bx.common.model.im.SendResult;
import com.bx.imclient.annotation.IMListener;
import com.bx.imclient.listener.MessageListener;
import com.bx.implatform.contant.RedisKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;


@Slf4j
@IMListener(type = ListenerType.GROUP_MESSAGE)
public class GroupMessageListener implements MessageListener {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void process(SendResult result){
        GroupMessageInfo messageInfo = (GroupMessageInfo) result.getMessageInfo();
        if(messageInfo.getType().equals(MessageType.TIP)){
            // 提示类数据不记录
            return;
        }
        // 保存该用户已拉取的最大消息id
        String key =  RedisKey.IM_GROUP_READED_POSITION + messageInfo.getGroupId()+":"+result.getRecvId();
        redisTemplate.opsForValue().set(key,messageInfo.getId());
    }

}
