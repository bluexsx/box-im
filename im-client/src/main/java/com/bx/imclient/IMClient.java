package com.bx.imclient;

import com.bx.common.model.im.GroupMessageInfo;
import com.bx.common.model.im.PrivateMessageInfo;
import com.bx.imclient.listener.MessageListenerMulticaster;
import com.bx.imclient.sender.IMSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class IMClient {

    @Autowired
    private MessageListenerMulticaster listenerMulticaster;

    @Autowired
    private IMSender imSender;

    public void sendPrivateMessage(Long userId, PrivateMessageInfo... messageInfo){
        imSender.sendPrivateMessage(userId,messageInfo);
    }

    public void sendGroupMessage(List<Long> userTokens, GroupMessageInfo... messageInfo){
        imSender.sendGroupMessage(userTokens,messageInfo);
    }


}
