package com.lx.implatform.imserver.websocket.processor;

import com.lx.common.enums.WSCmdEnum;
import com.lx.common.util.SpringContextHolder;

public class ProcessorFactory {

    public static MessageProcessor createProcessor(WSCmdEnum cmd){
        MessageProcessor  processor = null;
        switch (cmd){
            case LOGIN:
                processor = (MessageProcessor) SpringContextHolder.getApplicationContext().getBean(LoginProcessor.class);
                break;
            case HEART_BEAT:
                processor = (MessageProcessor) SpringContextHolder.getApplicationContext().getBean(HeartbeatProcessor.class);
                break;
            case PRIVATE_MESSAGE:
                processor = (MessageProcessor)SpringContextHolder.getApplicationContext().getBean(PrivateMessageProcessor.class);
                break;
            case GROUP_MESSAGE:
                processor = (MessageProcessor)SpringContextHolder.getApplicationContext().getBean(GroupMessageProcessor.class);
                break;
            default:
                break;
        }
        return processor;
    }

}
