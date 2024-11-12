package com.bx.imclient.task;

import cn.hutool.core.util.StrUtil;
import com.bx.imcommon.mq.RedisMQConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public abstract class AbstractMessageResultTask<T> extends RedisMQConsumer<T> {

    @Value("${spring.application.name}")
    private String appName;

    @Override
    public String generateKey() {
        return StrUtil.join(":", super.generateKey(), appName);
    }



}
