package com.bx.imserver.task;

import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.model.IMForceLogoutInfo;
import com.bx.imcommon.mq.RedisMQListener;
import com.bx.imserver.netty.processor.AbstractMessageProcessor;
import com.bx.imserver.netty.processor.ProcessorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RedisMQListener(queue = IMRedisKey.IM_USER_FORCE_LOGOUT_QUEUE)
public class PullForceLogoutTask extends AbstractPullMessageTask<IMForceLogoutInfo> {

    @Override
    public void onMessage(IMForceLogoutInfo logoutInfo) {
        AbstractMessageProcessor processor = ProcessorFactory.createProcessor(IMCmdType.FORCE_LOGOUT);
        processor.process(logoutInfo);
    }

}
