package com.bx.imserver.websocket;

import io.netty.channel.ChannelHandlerContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class WebsocketChannelCtxHolder {

    /*
     *  维护userId和ctx的关联关系，格式:Map<userId,ctx>
     */
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
