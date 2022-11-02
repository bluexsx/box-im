package com.lx.implatform.imserver.task;

import com.lx.implatform.imserver.websocket.WebsocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public  abstract class AbstractPullMessageTask{

    private int threadNum = 1;
    private ExecutorService executorService;

    @Autowired
    private WebsocketServer WSServer;

    public  AbstractPullMessageTask(){
        this.threadNum = 1;
    }

    public  AbstractPullMessageTask(int threadNum){
        this.threadNum = threadNum;
    }

    @PostConstruct
    public void init(){
        // 初始化定时器
        executorService = Executors.newFixedThreadPool(threadNum);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    if(WSServer.isReady()){
                        pullMessage();
                    }
                    Thread.sleep(100);
                }catch (Exception e){
                    log.error("任务调度异常",e);
                }
                executorService.execute(this);
            }
        });
    }

    public abstract void pullMessage();
}
