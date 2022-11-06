package com.bx.imserver.task;

import com.bx.imserver.websocket.WebsocketServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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

        for(int i=0;i<threadNum;i++){
            executorService.execute(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    try{
                        if(WSServer.isReady()){
                            pullMessage();
                        }
                        Thread.sleep(100);
                    }catch (Exception e){
                        log.error("任务调度异常",e);
                        Thread.sleep(200);
                    }
                    if(!executorService.isShutdown()){
                        executorService.execute(this);
                    }
                }
            });
        }
    }

    @PreDestroy
    public void destroy(){
        log.info("{}线程任务关闭",this.getClass().getSimpleName());
        executorService.shutdown();
    }

    public abstract void pullMessage();
}
