package com.bx.imclient.task;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;

@Slf4j
public abstract class AbstractPullMessageTask implements CommandLineRunner {

    private int threadNum = 8;

    private ExecutorService executorService = Executors.newFixedThreadPool(threadNum);


    @Override
    public void run(String... args) throws Exception {
        // 初始化定时器
        for(int i=0;i<threadNum;i++){
            executorService.execute(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    try{
                        pullMessage();
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
