package com.lx.implatform.task;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lx.common.contant.RedisKey;
import com.lx.common.enums.MessageStatusEnum;
import com.lx.implatform.entity.SingleMessage;
import com.lx.implatform.service.ISingleMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PullAlreadyReadMessageTask {

    private int threadNum = 8;

    private ExecutorService executorService = Executors.newFixedThreadPool(threadNum);

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private ISingleMessageService singleMessageService;

    @PostConstruct
    public void init(){
        for(int i=0;i<threadNum;i++){
            executorService.submit(new Task());
        }
    }

    protected class Task implements Runnable{
        @Override
        public void run() {
            try {
                String key = RedisKey.IM_ALREADY_READED_MESSAGE;
                Integer msgId =  (Integer)redisTemplate.opsForList().leftPop(key,1, TimeUnit.SECONDS);
                if(msgId!=null){
                    UpdateWrapper<SingleMessage> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.lambda().eq(SingleMessage::getId,msgId)
                            .set(SingleMessage::getStatus, MessageStatusEnum.ALREADY_READ.getCode());
                    singleMessageService.update(updateWrapper);
                    log.info("消息已读，id:{}",msgId);
                }
            }catch (Exception e){
                log.error(e.getMessage());
            }finally {
                // 下一次循环
                executorService.submit(this);
            }
        }
    }
}
