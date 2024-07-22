package com.bx.imcommon.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 创建单例线程池
 *
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
    private static final int CORE_POOL_SIZE =
        Math.min(ThreadPoolExecutorFactory.MAX_IMUM_POOL_SIZE, Runtime.getRuntime().availableProcessors() * 2);
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
    private static volatile ThreadPoolExecutor threadPoolExecutor = null;

    /**
     * 构造方法私有化
     */
    private ThreadPoolExecutorFactory() {
        if (null == threadPoolExecutor) {
            threadPoolExecutor = ThreadPoolExecutorFactory.getThreadPoolExecutor();
        }
    }

    /**
     * 重写readResolve方法
     */
    private Object readResolve() {
        //重写readResolve方法，防止序列化破坏单例
        return ThreadPoolExecutorFactory.getThreadPoolExecutor();
    }

    /**
     * 双检锁创建线程安全的单例
     */
    public static ThreadPoolExecutor getThreadPoolExecutor() {
        if (null == threadPoolExecutor) {
            synchronized (ThreadPoolExecutorFactory.class) {
                if (null == threadPoolExecutor) {
                    threadPoolExecutor = new ThreadPoolExecutor(
                        //核心线程数
                        CORE_POOL_SIZE,
                        //最大线程数，包含临时线程
                        MAX_IMUM_POOL_SIZE,
                        //临时线程的存活时间
                        KEEP_ALIVE_TIME,
                        //时间单位(毫秒)
                        TimeUnit.MILLISECONDS,
                        //等待队列
                        new LinkedBlockingQueue<>(QUEUE_SIZE),
                        //拒绝策略
                        new ThreadPoolExecutor.CallerRunsPolicy());
                }
            }
        }
        return threadPoolExecutor;
    }

    /**
     * 关闭线程池
     */
    public void shutDown() {
        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdown();
        }
    }

    public void execute(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        threadPoolExecutor.execute(runnable);
    }

}
