package com.bx.implatform.listener;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bx.imclient.IMClient;
import com.bx.imclient.annotation.IMListener;
import com.bx.imclient.listener.MessageListener;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.enums.IMListenerType;
import com.bx.imcommon.enums.IMSendCode;
import com.bx.imcommon.model.IMPrivateMessage;
import com.bx.imcommon.model.PrivateMessageInfo;
import com.bx.imcommon.model.SendResult;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.enums.MessageStatus;
import com.bx.implatform.enums.MessageType;
import com.bx.implatform.service.IPrivateMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;


@Slf4j
@IMListener(type = IMListenerType.PRIVATE_MESSAGE)
public class PrivateMessageListener implements MessageListener {

    @Autowired
    private IPrivateMessageService privateMessageService;

    @Autowired
    private IMClient imClient;

    @Override
    public void process(SendResult result){
        PrivateMessageInfo messageInfo = (PrivateMessageInfo) result.getData();
        IMSendCode resultCode = IMSendCode.fromCode(result.getCode());
        // 提示类数据不记录
        if(messageInfo.getType().equals(MessageType.TIP.code())){
            return;
        }
        // 视频通话信令不记录
        if(messageInfo.getType() >= MessageType.RTC_CALL.code() && messageInfo.getType()< MessageType.RTC_CANDIDATE.code()){
            // 通知用户呼叫失败了
            if(messageInfo.getType().equals(MessageType.RTC_CALL.code())
                    && !resultCode.equals(IMSendCode.SUCCESS)){
                PrivateMessageInfo msgInfo = new PrivateMessageInfo();
                msgInfo.setRecvId(messageInfo.getSendId());
                msgInfo.setSendId(messageInfo.getRecvId());
                msgInfo.setType(MessageType.RTC_FAILED.code());
                msgInfo.setContent(resultCode.description());
                msgInfo.setSendTime(new Date());

                IMPrivateMessage sendMessage = new IMPrivateMessage();
                sendMessage.setSendId(messageInfo.getSendId());
                sendMessage.setRecvId(messageInfo.getRecvId());
                sendMessage.setSendTerminal(result.getRecvTerminal());
                sendMessage.setSendToSelf(false);
                sendMessage.setDatas(Arrays.asList(messageInfo));
                imClient.sendPrivateMessage(sendMessage);
            }
            return;
        }
        // 更新消息状态,这里只处理成功消息，失败的消息继续保持未读状态
        if(resultCode.equals(IMSendCode.SUCCESS)){
            UpdateWrapper<PrivateMessage> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda().eq(PrivateMessage::getId,messageInfo.getId())
                    .eq(PrivateMessage::getStatus, MessageStatus.UNREAD.code())
                    .set(PrivateMessage::getStatus, MessageStatus.ALREADY_READ.code());
            privateMessageService.update(updateWrapper);
            log.info("消息已读，消息id:{}，发送者:{},接收者:{}",messageInfo.getId(),messageInfo.getSendId(),messageInfo.getRecvId());
        }
    }

}
