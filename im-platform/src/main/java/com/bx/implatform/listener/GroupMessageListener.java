package com.bx.implatform.listener;

import com.bx.imclient.annotation.IMListener;
import com.bx.imclient.listener.MessageListener;
import com.bx.imcommon.enums.IMListenerType;
import com.bx.imcommon.enums.IMSendCode;
import com.bx.imcommon.model.IMSendResult;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.vo.GroupMessageVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;


@Slf4j
@IMListener(type = IMListenerType.GROUP_MESSAGE)
@AllArgsConstructor
public class GroupMessageListener implements MessageListener<GroupMessageVO> {
    
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void process(IMSendResult<GroupMessageVO> result){
        GroupMessageVO messageInfo = result.getData();
        // todo 删除
        // 保存该用户已拉取的最大消息id
        if(result.getCode().equals(IMSendCode.SUCCESS.code())) {
            String key = String.join(":",RedisKey.IM_GROUP_READED_POSITION,messageInfo.getGroupId().toString(),result.getReceiver().getId().toString());
            redisTemplate.opsForValue().set(key, messageInfo.getId());
        }
    }

}
