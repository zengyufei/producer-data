package com.zyf.producer.utils;

import cn.hutool.core.thread.NamedThreadFactory;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.zyf.producer.base.BaseSqlContext;
import com.zyf.producer.base.Sql执行方式;
import com.zyf.producer.base.默认Sql异常处理;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RingBufferUtil {

    private RingBufferUtil() {
    }

    public static <T extends BaseSqlContext> RingBuffer<T> getRingBuffer(int rate,
                                                                         ThreadPoolExecutor consumerPool,
                                                                         Supplier<T> supplier,
                                                                         Consumer<Sql执行方式<T>> consumer,
                                                                         Consumer<Disruptor<T>> closeFn) {
        final WaitStrategy waitStrategy = new BlockingWaitStrategy();
//        final WaitStrategy waitStrategy = new BusySpinWaitStrategy();
        Disruptor<T> disruptor = new Disruptor<>(supplier::get, rate, consumerPool,
                ProducerType.MULTI, waitStrategy);
        // 消费 Disruptor
        final Sql执行方式 t = new Sql执行方式(disruptor);
        consumer.accept(t);
        final int total = t.getTotal();
        final int maximumPoolSize = total * 2;
        if (maximumPoolSize < total) {
            consumerPool.setCorePoolSize(maximumPoolSize);
            consumerPool.setMaximumPoolSize(maximumPoolSize);
        } else {
            consumerPool.setMaximumPoolSize(maximumPoolSize);
            consumerPool.setCorePoolSize(maximumPoolSize);
        }
        // 设置全局的异常处理器类
        disruptor.setDefaultExceptionHandler(new 默认Sql异常处理<>(consumerPool));
        // 启动disruptor线程
        disruptor.start();
        Disruptor<T> finalDisruptor = disruptor;
        new Thread(() -> {
            closeFn.accept(finalDisruptor);
        }).start();
        // 创建RingBuffer
        return disruptor.getRingBuffer();
    }


    public static <T extends BaseSqlContext> RingBuffer<T> getRingBuffer(int rate,
                                                                         Supplier<T> supplier,
                                                                         Consumer<Sql执行方式<T>> consumer,
                                                                         Consumer<Disruptor<T>> closeFn) {
        final ThreadFactory threadFactory = new NamedThreadFactory("consumer-pool-", false);
        final WaitStrategy waitStrategy = new BlockingWaitStrategy();
//        final WaitStrategy waitStrategy = new BusySpinWaitStrategy();
        Disruptor<T> disruptor = new Disruptor<>(supplier::get, rate, threadFactory,
                ProducerType.MULTI, waitStrategy);
        // 消费 Disruptor
        consumer.accept(new Sql执行方式(disruptor));
        // 设置全局的异常处理器类
        disruptor.setDefaultExceptionHandler(new 默认Sql异常处理<>());
        // 启动disruptor线程
        disruptor.start();
        Disruptor<T> finalDisruptor = disruptor;
        new Thread(() -> {
            closeFn.accept(finalDisruptor);
        }).start();
        // 创建RingBuffer
        return disruptor.getRingBuffer();
    }


}
