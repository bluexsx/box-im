package com.bx.imclient.task;

import com.bx.imcommon.util.ThreadPoolExecutorFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractMessageResultTask implements CommandLineRunner {

    private static final ScheduledThreadPoolExecutor EXECUTOR_SERVICE = ThreadPoolExecutorFactory.getThreadPoolExecutor();

    @Override
    public void run(String... args) {
        // 初始化定时器
        EXECUTOR_SERVICE.execute(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                try {
                    pullMessage();
                } catch (Exception e) {
                    log.error("任务调度异常", e);
                }
                if (!EXECUTOR_SERVICE.isShutdown()) {
                    EXECUTOR_SERVICE.schedule(this,100, TimeUnit.MICROSECONDS);
                }
            }
        });
    }


    public abstract void pullMessage() throws InterruptedException;
}
