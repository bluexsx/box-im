package com.lx.implatform.imserver.websocket;

import io.netty.channel.ChannelHandlerContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketChannelCtxHloder {

    private static Map<Long, ChannelHandlerContext> channelMap = new ConcurrentHashMap();


    public static void  addChannelCtx(Long userId,ChannelHandlerContext ctx){
        channelMap.put(userId,ctx);
    }

    public static void  removeChannelCtx(Long userId){
        channelMap.remove(userId);
    }




    public static ChannelHandlerContext  getChannelCtx(Long userId){
        return channelMap.get(userId);
    }

    public static Set<Long> getAllChannelIds(){
        return channelMap.keySet();
    }


}
