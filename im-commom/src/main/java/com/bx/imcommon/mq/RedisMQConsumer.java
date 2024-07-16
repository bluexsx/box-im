package com.bx.imcommon.mq;

import java.util.List;

/**
 * redis 队列消费者抽象类
 */
public abstract class RedisMQConsumer<T> {

     /**
      * 消费消息回调(单条)
      */
     public void onMessage(T data){}

     /**
      * 消费消息回调(批量)
      */
     public void onMessage(List<T> datas){}

     /**
      * 生成redis队列完整key
      */
     public String generateKey(){
          // 默认队列名就是redis的key
          RedisMQListener annotation = this.getClass().getAnnotation(RedisMQListener.class);
          return annotation.queue();
     }

     /**
      * 队列是否就绪，返回true才会开始消费
      */
     public Boolean isReady(){
          return true;
     }
}
