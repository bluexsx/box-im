package com.bx.implatform.listener;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bx.imclient.annotation.IMListener;
import com.bx.imclient.listener.MessageListener;
import com.bx.imcommon.enums.IMListenerType;
import com.bx.imcommon.enums.IMSendCode;
import com.bx.imcommon.model.IMSendResult;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.enums.MessageStatus;
import com.bx.implatform.service.PrivateMessageService;
import com.bx.implatform.vo.PrivateMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@IMListener(type = IMListenerType.PRIVATE_MESSAGE)
public class PrivateMessageListener implements MessageListener<PrivateMessageVO> {
    @Lazy
    @Autowired
    private PrivateMessageService privateMessageService;

    @Override
    public void process(List<IMSendResult<PrivateMessageVO>> results) {
        Set<Long> messageIds = new HashSet<>();
        for (IMSendResult<PrivateMessageVO> result : results) {
            PrivateMessageVO messageInfo = result.getData();
            // 更新消息状态,这里只处理成功消息，失败的消息继续保持未读状态
            if (result.getCode().equals(IMSendCode.SUCCESS.code()) && !Objects.isNull(messageInfo.getId())) {
                if (result.getReceiver().getId().equals(messageInfo.getRecvId())) {
                    messageIds.add(messageInfo.getId());
                    log.info("消息送达，消息id:{}，发送者:{},接收者:{},终端:{}", messageInfo.getId(),
                        result.getSender().getId(), result.getReceiver().getId(), result.getReceiver().getTerminal());
                }
            }
        }
        // 批量修改状态
        if (CollUtil.isNotEmpty(messageIds)) {
            UpdateWrapper<PrivateMessage> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda().in(PrivateMessage::getId, messageIds)
                .eq(PrivateMessage::getStatus, MessageStatus.PENDING.code())
                .set(PrivateMessage::getStatus, MessageStatus.DELIVERED.code());
            privateMessageService.update(updateWrapper);
        }
    }
}
