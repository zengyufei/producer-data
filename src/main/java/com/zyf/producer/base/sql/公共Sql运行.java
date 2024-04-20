package com.zyf.producer.base.sql;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.GlobalDbConfig;
import com.lmax.disruptor.RingBuffer;
import com.zyf.producer.base.状态;
import com.zyf.producer.utils.DataUtil;
import com.zyf.producer.utils.PoolUtil;
import com.zyf.producer.utils.RingBufferUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public class 公共Sql运行<T extends BaseSqlContext, S extends 公共Sql生产者<T>> {

    protected final static int producerCount = 20;
    protected final static AtomicInteger producerCounter = new AtomicInteger(1);
    protected static volatile int total = 1;
    protected final ThreadPoolExecutor producerPool = PoolUtil.getProducerPoolExecutorService(producerCount);
    protected final ThreadPoolExecutor consumerPool = PoolUtil.getConsumerPoolExecutorService(2);

    public static <T extends 公共Sql消费者> T[] 创建消费者数组(int size, AtomicInteger counter, Supplier<T> supplier) {
        final T t = supplier.get();
        T[] consumers = (T[]) Array.newInstance(t.getClass(), size);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (i == 0) {
                t.setConsumerCounter(counter); // 假设每个消费者都有一个设置计数器的方法
                consumers[i] = t;
            }
            if (i != 0) {
                int finalI = i;
                final Thread thread = new Thread(() -> {
                    T consumer = supplier.get(); // 使用供应商创建新实例
                    consumer.setConsumerCounter(counter); // 假设每个消费者都有一个设置计数器的方法
                    consumers[finalI] = consumer;
                });
                threads.add(thread);
                thread.start();
            }
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return consumers;
    }

    public void 执行(String[] args, Supplier<S> producerSupplier, Supplier<T> beanSupplier, Consumer<Sql执行方式<T>> consumer) throws InterruptedException {
        loadConfig();

        // 因为随机数工具需要加载耗时7s，所以用多线程提前加载
        new Thread(DataUtil::随机省份).start();

        String name = ManagementFactory.getRuntimeMXBean().getName();
        System.out.println(name);

        total = getDataTotalCount(args);
        // String[] tableNames = getTableNames(args);
        final StopWatch stopWatch = StopWatch.create("执行");
        stopWatch.start();
        最终执行(producerSupplier, beanSupplier, consumer);
        stopWatch.stop();
        System.out.println("花费总时长: " + stopWatch.getTotalTimeMillis());
    }

    private void 最终执行(Supplier<S> producerSupplier, Supplier<T> beanSupplier, Consumer<Sql执行方式<T>> consumer) throws InterruptedException {
        AtomicBoolean isProducerStopped = new AtomicBoolean(false);
        AtomicBoolean isConsumerStopped = new AtomicBoolean(false);
        try {
            log.info("创建 RingBuffer");
            final RingBuffer<T> ringBuffer = RingBufferUtil.getSqlRingBuffer(4096,
                    consumerPool,
                    beanSupplier,
                    consumer,
                    disruptor -> {
                        int x = 1;
                        while (!isProducerStopped.get()) {
                            if (x++ % 10 == 0) {
                                log.debug("每5秒检查一次是否生产完毕!");
                            }
                            try {
                                TimeUnit.MILLISECONDS.sleep(500);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        log.warn("生产全部完毕!");
                        disruptor.shutdown();
                        log.warn("关闭当前 disruptor shutdown!");
                    });

            new Thread(() -> {
                log.info("启动资源监控!");
                while (!isConsumerStopped.get()) {
                    Console.log("ringBuffer剩余量: {}/{}, 还有 {} 个没使用, 生产者已使用线程: {}/{}, 消费者已使用线程: {}/{}",
                            ringBuffer.getCursor(),
                            ringBuffer.getBufferSize(),
                            ringBuffer.remainingCapacity(),
                            producerPool.getActiveCount(), producerPool.getCorePoolSize(),
                            consumerPool.getActiveCount(), consumerPool.getCorePoolSize());
                    try {
                        TimeUnit.MILLISECONDS.sleep(1500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                log.warn("监控退出!");
            }).start();


            System.err.println("----------开始生产数据----------");
            final S producer = producerSupplier.get();
            producer.setRingBuffer(ringBuffer);
            for (int i = 0; i < producerCount; i++) {
                producerPool.execute(() -> {
                    ThreadLocal<Integer> countThreadLocal = new ThreadLocal<>();
                    while (true) {
                        synchronized (producerCounter) {
                            final int count = producerCounter.get();
                            countThreadLocal.set(count);
                            if (count <= total) {
                                producerCounter.incrementAndGet();
                            } else {
                                公共Sql生产者.isDone.set(true);
                                log.info("退出生产, 生产到达最大值: {}条!", total);
                                break;
                            }
                        }

                        final Integer seqNo = countThreadLocal.get();
                        try {
                            if (log.isDebugEnabled()) {
                                log.debug("执行生产数据第{}次", seqNo);
                            }
                            final 状态 status = producer.生产数据(seqNo);
                            if (status == 状态.push) {
                                synchronized (producerCounter) {
                                    final int count = producerCounter.get() - 1;
                                    log.info("生产第{}条数据!", count);
                                }
                            }
                            if (status == 状态.stop) {
                                log.info("status == 状态.stop, 将停止生产!");
                                公共Sql生产者.isDone.set(true);
                                break;
                            }
                            if (status == 状态.none) {
                                log.info("status == 状态.none, 产生空转,请检查原因!!!!");
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        } finally {
                            countThreadLocal.remove();
                        }
                    }
                });
            }
        } finally {
            // 等待所有任务执行完毕
            producerPool.shutdown();
            // 检测线程池的任务执行完
            while (!producerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("生产者线程池中还有任务在处理");
            }
            log.warn("生产者线程池已经关闭!");
            isProducerStopped.set(true); // 标记生产者线程已经停止
            // 等待所有任务执行完毕
            consumerPool.shutdown();
            // 检测线程池的任务执行完
            while (!consumerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("消费者线程池中还有任务在处理");
            }
            log.warn("消费者线程池已经关闭!");
            isConsumerStopped.set(true);
            log.warn("消费全部完毕!");
        }

    }

    private int getDataTotalCount(String[] args) {
        log.info("读取命令行参数!");
        int maxExecutions = total;
        if (ArrayUtil.isNotEmpty(args)) {
            if (args.length > 0) {
                final String numberCount = args[0];
                if (StrUtil.isNotBlank(numberCount) && NumberUtil.isNumber(numberCount)) {
                    maxExecutions = Convert.toInt(numberCount, 40000);
                    log.info("消费数量被修改为: {}", numberCount);
                }
            }
        }
        return maxExecutions;
    }

    private String getJarDirectory() {
        URL url = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        String jarPath = url.getPath();
        File file = new File(URLDecoder.decode(jarPath, StandardCharsets.UTF_8));
        return file.getParent();
    }

    private void loadConfig() {
        final String path = getJarDirectory() + File.separator + "db.setting";
        log.info("检查jar包外是否包含配置文件: {}", path);
        if (FileUtil.exist(path)) {
            log.info("读取配置文件: {}", "db.setting");
            GlobalDbConfig.setDbSettingPath(path);
        } else {
            log.info("从classpath读取配置文件: {}", "db.setting");
        }
    }
}
