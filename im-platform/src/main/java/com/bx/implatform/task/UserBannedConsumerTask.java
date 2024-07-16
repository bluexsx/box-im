package com.bx.implatform.task;

import com.bx.imclient.IMClient;
import com.bx.imcommon.enums.IMTerminalType;
import com.bx.imcommon.model.IMPrivateMessage;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.imcommon.mq.RedisMQConsumer;
import com.bx.imcommon.mq.RedisMQListener;
import com.bx.implatform.contant.Constant;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.dto.UserBanDTO;
import com.bx.implatform.service.IUserService;
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.vo.PrivateMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

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
        // 推送消息

        PrivateMessageVO msgInfo = new PrivateMessageVO();
        msgInfo.setRecvId(dto.getId());
        msgInfo.setSendId(Constant.SYS_USER_ID);
        msgInfo.setContent(dto.getReason());
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(Constant.SYS_USER_ID, IMTerminalType.WEB.code()));
        sendMessage.setRecvId(dto.getId());
        sendMessage.setSendToSelf(false);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(true);
        imClient.sendPrivateMessage(sendMessage);
    }
}
