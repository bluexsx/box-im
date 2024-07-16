package com.bx.imcommon.mq;

import java.util.List;

/**
 * redis 队列消费者抽象类
 */
public abstract class RedisMQConsumer<T> {

     public void onMessage(T data){}

     public void onMessage(List<T> datas){}

     public String generateKey(){
          // 默认队列名就是redis的key
          RedisMQListener annotation = this.getClass().getAnnotation(RedisMQListener.class);
          return annotation.queue();
     }
}
