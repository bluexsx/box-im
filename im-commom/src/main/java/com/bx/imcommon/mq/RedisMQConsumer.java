package com.bx.imcommon.mq;

import java.util.List;

/**
 * redis 队列消费者抽象类
 */
public abstract class RedisMQConsumer<T> {

     public void onMessage(T data){}

     public void onMessage(List<T> datas){}
}
