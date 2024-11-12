package com.bx.imserver.netty.processor;

import com.bx.imcommon.enums.IMCmdType;
import com.bx.imserver.util.SpringContextHolder;

public class ProcessorFactory {

    public static AbstractMessageProcessor createProcessor(IMCmdType cmd) {
        return switch (cmd) {
            case LOGIN->SpringContextHolder.getApplicationContext().getBean(LoginProcessor.class);
            case HEART_BEAT -> SpringContextHolder.getApplicationContext().getBean(HeartbeatProcessor.class);
            case PRIVATE_MESSAGE->SpringContextHolder.getApplicationContext().getBean(PrivateMessageProcessor.class);
            case GROUP_MESSAGE->SpringContextHolder.getApplicationContext().getBean(GroupMessageProcessor.class);
            case SYSTEM_MESSAGE->SpringContextHolder.getApplicationContext().getBean(SystemMessageProcessor.class);
            default -> null;
        };
    }

}
