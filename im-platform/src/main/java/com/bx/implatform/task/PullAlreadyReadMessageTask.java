package com.bx.implatform.task;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bx.common.contant.RedisKey;
import com.bx.common.enums.MessageStatusEnum;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.service.IPrivateMessageService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
    private IPrivateMessageService privateMessageService;

    @PostConstruct
    public void init(){
        for(int i=0;i<threadNum;i++){
            executorService.execute(new Task());
        }
    }

    @PreDestroy
    public void destroy(){
        log.info("{}线程任务关闭",this.getClass().getSimpleName());
        executorService.shutdown();
    }


    protected class Task implements Runnable{
        @SneakyThrows
        @Override
        public void run() {
            try {
                String key = RedisKey.IM_READED_PRIVATE_MESSAGE_ID;
                Integer msgId =  (Integer)redisTemplate.opsForList().leftPop(key,10, TimeUnit.SECONDS);
                if(msgId!=null){
                    UpdateWrapper<PrivateMessage> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.lambda().eq(PrivateMessage::getId,msgId)
                            .set(PrivateMessage::getStatus, MessageStatusEnum.ALREADY_READ.getCode());
                    privateMessageService.update(updateWrapper);
                    log.info("消息已读，id:{}",msgId);
                }
            }catch (Exception e){
                log.error(e.getMessage());
                Thread.sleep(200);
            }finally {
                // 下一次循环
                executorService.submit(this);
            }
        }
    }
}
