package com.lx.implatform.imserver.websocket.endecode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lx.common.model.im.SendInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;

public class MessageProtocolDecoder extends MessageToMessageDecoder<TextWebSocketFrame> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame, List<Object> list) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        SendInfo sendInfo = objectMapper.readValue(textWebSocketFrame.text(), SendInfo.class);
        list.add(sendInfo);
    }
}
