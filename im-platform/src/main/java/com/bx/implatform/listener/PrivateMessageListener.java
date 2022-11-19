package com.bx.implatform.listener;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bx.imclient.annotation.IMListener;
import com.bx.imclient.listener.MessageListener;
import com.bx.imcommon.enums.IMListenerType;
import com.bx.imcommon.enums.IMSendStatus;
import com.bx.imcommon.model.PrivateMessageInfo;
import com.bx.imcommon.model.SendResult;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.enums.MessageStatus;
import com.bx.implatform.enums.MessageType;
import com.bx.implatform.service.IPrivateMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;


@Slf4j
@IMListener(type = IMListenerType.PRIVATE_MESSAGE)
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
        if(result.getStatus().equals(IMSendStatus.SUCCESS)){
            UpdateWrapper<PrivateMessage> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda().eq(PrivateMessage::getId,messageInfo.getId())
                    .eq(PrivateMessage::getStatus, MessageStatus.UNREAD.code())
                    .set(PrivateMessage::getStatus, MessageStatus.ALREADY_READ.code());
            privateMessageService.update(updateWrapper);
            log.info("消息已读，消息id:{}，发送者:{},接收者:{}",messageInfo.getId(),messageInfo.getSendId(),messageInfo.getRecvId());
        }
    }

}
