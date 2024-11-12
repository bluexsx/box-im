package com.bx.imserver.task;

import com.bx.imcommon.mq.RedisMQConsumer;
import com.bx.imserver.netty.IMServerGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class AbstractPullMessageTask<T> extends RedisMQConsumer<T> {

    @Autowired
    private IMServerGroup serverGroup;

    @Override
    public String generateKey() {
        return String.join(":",  super.generateKey(), IMServerGroup.serverId + "");
    }

    @Override
    public Boolean isReady() {
        return serverGroup.isReady();
    }
}
