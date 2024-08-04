package com.bx.imserver.netty.processor;

import com.bx.imcommon.enums.IMCmdType;
import com.bx.imserver.util.SpringContextHolder;

public class ProcessorFactory {

    public static AbstractMessageProcessor createProcessor(IMCmdType cmd) {
        AbstractMessageProcessor processor = null;
        switch (cmd) {
            case LOGIN:
                processor = SpringContextHolder.getApplicationContext().getBean(LoginProcessor.class);
                break;
            case HEART_BEAT:
                processor = SpringContextHolder.getApplicationContext().getBean(HeartbeatProcessor.class);
                break;
            case PRIVATE_MESSAGE:
                processor = SpringContextHolder.getApplicationContext().getBean(PrivateMessageProcessor.class);
                break;
            case GROUP_MESSAGE:
                processor = SpringContextHolder.getApplicationContext().getBean(GroupMessageProcessor.class);
                break;
            case SYSTEM_MESSAGE:
                processor = SpringContextHolder.getApplicationContext().getBean(SystemMessageProcessor.class);
                break;
            default:
                break;
        }
        return processor;
    }

}
