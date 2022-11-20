package com.bx.imserver.netty.processor;


import io.netty.channel.ChannelHandlerContext;

public abstract class MessageProcessor<T> {

    public void process(ChannelHandlerContext ctx,T data){}

    public void process(T data){}

     public T transForm(Object o){
         return (T)o;
     }


}
