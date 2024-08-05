package com.bx.imclient.task;

import com.bx.imcommon.util.ThreadPoolExecutorFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;

@Slf4j
public abstract class AbstractMessageResultTask implements CommandLineRunner {

    private static final ExecutorService EXECUTOR_SERVICE = ThreadPoolExecutorFactory.getThreadPoolExecutor();

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
                    Thread.sleep(100);
                    EXECUTOR_SERVICE.execute(this);
                }
            }
        });
    }


    @PreDestroy
    public void destroy() {
        log.info("{}线程任务关闭", this.getClass().getSimpleName());
        EXECUTOR_SERVICE.shutdown();
    }

    public abstract void pullMessage() throws InterruptedException;
}
