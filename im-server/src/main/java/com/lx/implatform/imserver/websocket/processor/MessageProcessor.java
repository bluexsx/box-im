package com.lx.implatform.imserver.websocket.processor;

import io.netty.channel.ChannelHandlerContext;

public interface MessageProcessor<T> {

    void process(ChannelHandlerContext ctx,T data);

}
