package com.bx.imserver.netty.processor;

import com.bx.imcommon.model.IMUserInfo;
import io.netty.channel.ChannelFuture;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PushTask {

    private final IMUserInfo receiver;

    private final ChannelFuture future;
}
