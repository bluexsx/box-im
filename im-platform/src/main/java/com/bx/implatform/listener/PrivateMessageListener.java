package com.bx.implatform.listener;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bx.common.enums.ListenerType;
import com.bx.common.enums.MessageStatus;
import com.bx.common.enums.MessageType;
import com.bx.common.model.im.PrivateMessageInfo;
import com.bx.common.model.im.SendResult;
import com.bx.imclient.annotation.IMListener;
import com.bx.imclient.listener.MessageListener;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.service.IPrivateMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;


@Slf4j
@IMListener(type = ListenerType.PRIVATE_MESSAGE)
public class PrivateMessageListener implements MessageListener {

    @Autowired
    private IPrivateMessageService privateMessageService;

    @Override
    public void process(SendResult result){
        PrivateMessageInfo messageInfo = (PrivateMessageInfo) result.getMessageInfo();
        if(messageInfo.getType().equals(MessageType.TIP)){
            // 提示类数据不记录
            return;
        }
        // 更新消息状态
        UpdateWrapper<PrivateMessage> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(PrivateMessage::getId,messageInfo.getId())
                .eq(PrivateMessage::getStatus, MessageStatus.UNREAD.getCode())
                .set(PrivateMessage::getStatus, MessageStatus.ALREADY_READ.getCode());
        privateMessageService.update(updateWrapper);
        log.info("消息已读，消息id:{}，发送者:{},接收者:{}",messageInfo.getId(),messageInfo.getSendId(),messageInfo.getRecvId());
    }

}
