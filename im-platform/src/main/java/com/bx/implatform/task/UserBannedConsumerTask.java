package com.bx.implatform.task;

import com.bx.imclient.IMClient;
import com.bx.imcommon.model.IMSystemMessage;
import com.bx.imcommon.mq.RedisMQConsumer;
import com.bx.imcommon.mq.RedisMQListener;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.dto.UserBanDTO;
import com.bx.implatform.enums.MessageType;
import com.bx.implatform.vo.SystemMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Collections;

/**
 * @author: 谢绍许
 * @date: 2024-07-15
 * @version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RedisMQListener(queue = RedisKey.IM_QUEUE_USER_BANNED)
public class UserBannedConsumerTask extends RedisMQConsumer<UserBanDTO> {

    private final IMClient imClient;
    @Override
    public void onMessage(UserBanDTO dto) {
        log.info("用户被封禁处理,userId:{},原因:{}",dto.getId(),dto.getReason());
        // 推送消息将用户赶下线
        SystemMessageVO msgInfo = new SystemMessageVO();
        msgInfo.setType(MessageType.USER_BANNED.code());
        msgInfo.setContent(dto.getReason());
        IMSystemMessage<SystemMessageVO> sendMessage = new IMSystemMessage<>();
        sendMessage.setRecvIds(Collections.singletonList(dto.getId()));
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(true);
        imClient.sendSystemMessage(sendMessage);
    }
}
