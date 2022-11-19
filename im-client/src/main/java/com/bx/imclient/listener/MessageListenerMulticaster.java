package com.bx.imclient.listener;


import com.bx.common.enums.ListenerType;
import com.bx.common.model.im.SendResult;
import com.bx.imclient.annotation.IMListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class MessageListenerMulticaster {


    @Autowired(required = false)
    private List<MessageListener>  messageListeners  = Collections.emptyList();

    public  void multicast(ListenerType type, SendResult result){
        for(MessageListener listener:messageListeners){
            IMListener annotation = listener.getClass().getAnnotation(IMListener.class);
            if(annotation.type().equals(type)){
                listener.process(result);
            }
        }
    }
}
