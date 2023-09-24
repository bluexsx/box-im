package com.bx.imclient;

import com.bx.imclient.sender.IMSender;
import com.bx.imcommon.model.IMGroupMessage;
import com.bx.imcommon.model.IMPrivateMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;



@Configuration
public class IMClient {

    @Autowired
    private IMSender imSender;

    /**
     * 判断用户是否在线
     *
     * @param userId 用户id
     */
    public Boolean isOnline(Long userId){
        return imSender.isOnline(userId);
    }

    /**
     * 发送私聊消息（发送结果通过MessageListener接收）
     *
     * @param message 私有消息
     */
    public void sendPrivateMessage(IMPrivateMessage<?> message){
        imSender.sendPrivateMessage(message);
    }

    /**
     * 发送群聊消息（发送结果通过MessageListener接收）
     *
     * @param message 群聊消息
     */
    public void sendGroupMessage(IMGroupMessage<?> message){
        imSender.sendGroupMessage(message);
    }


}
