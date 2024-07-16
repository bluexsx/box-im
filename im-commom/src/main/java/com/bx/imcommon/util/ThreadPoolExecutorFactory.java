package com.bx.imcommon.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 创建单例线程池
 * @author Andrews
 * @date 2023/11/30 11:12
 */
@Slf4j
public final class ThreadPoolExecutorFactory {
    /**
     * 机器的CPU核数:Runtime.getRuntime().availableProcessors()
     * corePoolSize 池中所保存的线程数，包括空闲线程。
     * CPU 密集型：核心线程数 = CPU核数 + 1
     * IO 密集型：核心线程数 = CPU核数 * 2
     */
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    /**
     * maximumPoolSize - 池中允许的最大线程数(采用LinkedBlockingQueue时没有作用)。
     */
    private static final int MAX_IMUM_POOL_SIZE = 100;
    /**
     * keepAliveTime -当线程数大于核心时，此为终止前多余的空闲线程等待新任务的最长时间，线程池维护线程所允许的空闲时间
     */
    private static final int KEEP_ALIVE_TIME = 1000;
    /**
     * 等待队列的大小。默认是无界的，性能损耗的关键
     */
    private static final int QUEUE_SIZE = 200;

    /**
     * 线程池对象
     */
    private static volatile ScheduledThreadPoolExecutor threadPoolExecutor = null;

    /**
     * 构造方法私有化
     */
    private ThreadPoolExecutorFactory() {
        if (null == threadPoolExecutor) {
            threadPoolExecutor = ThreadPoolExecutorFactory.getThreadPoolExecutor();
        }
    }


    /**
     * 双检锁创建线程安全的单例
     */
    public static ScheduledThreadPoolExecutor getThreadPoolExecutor() {
        if (null == threadPoolExecutor) {
            synchronized (ThreadPoolExecutorFactory.class) {
                if (null == threadPoolExecutor) {
                    threadPoolExecutor = new ScheduledThreadPoolExecutor(
                            //核心线程数
                            CORE_POOL_SIZE,
                            //拒绝策略
                            new ThreadPoolExecutor.CallerRunsPolicy()
                    );
                }
            }
        }
        return threadPoolExecutor;
    }

    /**
     * 关闭线程池
     */
    public static void shutDown() {
        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdown();
        }
    }

    public static void execute(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        threadPoolExecutor.execute(runnable);
    }

}
