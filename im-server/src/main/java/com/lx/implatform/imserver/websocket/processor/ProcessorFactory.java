package com.lx.implatform.imserver.websocket.processor;

import com.lx.common.enums.WSCmdEnum;
import com.lx.common.util.SpringContextHolder;

public class ProcessorFactory {

    public static MessageProcessor createProcessor(WSCmdEnum cmd){
        MessageProcessor  processor = null;
        switch (cmd){
            case HEARTBEAT:
                processor = (MessageProcessor) SpringContextHolder.getApplicationContext().getBean("heartbeatProcessor");
                break;
            case SINGLE_MESSAGE:
                processor = (MessageProcessor)SpringContextHolder.getApplicationContext().getBean("singleMessageProcessor");
                break;
            default:
                break;
        }
        return processor;
    }

}
