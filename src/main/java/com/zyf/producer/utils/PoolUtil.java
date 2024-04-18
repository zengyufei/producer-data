package com.zyf.producer.utils;

import cn.hutool.core.util.StrUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

public class PoolUtil {

    private static final int PRODUCER_QUEUE_CAPACITY = 10000000;
    private static final int CONSUMER_QUEUE_CAPACITY = 10000000;
    private static final int PRODUCER_POOL_SIZE = 50;
    private static final int CONSUMER_POOL_SIZE = 20;

    public static ThreadPoolExecutor getConsumerPoolExecutorService(int coreSize) {
        return getPoolExecutorService("consumer", coreSize);
    }

    public static ThreadPoolExecutor getProducerPoolExecutorService(int coreSize) {
        return getPoolExecutorService("producer", coreSize);
    }

    public static ThreadPoolExecutor getConsumerPoolExecutorService() {
        return getPoolExecutorService("consumer", null);
    }

    public static ThreadPoolExecutor getProducerPoolExecutorService() {
        return getPoolExecutorService("producer", null);
    }

    private static ThreadPoolExecutor getPoolExecutorService(String type, Integer coreSize) {
        // 定义线程工厂，用于设置线程名称等属性
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat(StrUtil.format("{}-pool-thread-%d", type))
                .setDaemon(true)
                .build();
        // 定义阻塞队列，这里使用有界队列防止任务堆积导致内存溢出
        int queueCapacity = PRODUCER_QUEUE_CAPACITY;
        if (StrUtil.equalsIgnoreCase(type, "consumer")) {
            queueCapacity = CONSUMER_QUEUE_CAPACITY;
        }
        int poolSize;
        if (coreSize != null) {
            poolSize = coreSize;
        } else {
            poolSize = PRODUCER_POOL_SIZE;
            if (StrUtil.equalsIgnoreCase(type, "consumer")) {
                poolSize = CONSUMER_POOL_SIZE;
            }
        }
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(queueCapacity);
        // 定义拒绝策略，当队列满时如何处理新提交的任务
        RejectedExecutionHandler rejectionHandler = new ThreadPoolExecutor.AbortPolicy();
        // 手动创建固定大小线程池
        return new ThreadPoolExecutor(
                poolSize,
                poolSize, // 核心线程数等于最大线程数，保持固定大小
                0L,                 // 空闲线程存活时间设为0，即核心线程不超时
                TimeUnit.MILLISECONDS, // 时间单位
                workQueue,             // 使用自定义的有界队列
                threadFactory,         // 使用自定义的线程工厂
                rejectionHandler      // 设置拒绝策略
        );
    }
}
