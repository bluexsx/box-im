package com.bx.imserver.netty;

import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class UserChannelCtxMap {

    /*
     *  维护userId和ctx的关联关系，格式:Map<userId,ctx>
     */
    private static Map<Long, ChannelHandlerContext> channelMap = new ConcurrentHashMap();

    public static void  addChannelCtx(Long userId,ChannelHandlerContext ctx){
        channelMap.put(userId,ctx);
    }

    public static void  removeChannelCtx(Long userId){
        if(userId != null){
            channelMap.remove(userId);
        }
    }

    public static ChannelHandlerContext  getChannelCtx(Long userId){
        if(userId == null){
            return  null;
        }
        return channelMap.get(userId);
    }

}
