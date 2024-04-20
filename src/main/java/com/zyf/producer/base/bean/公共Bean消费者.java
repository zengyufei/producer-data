package com.zyf.producer.base.bean;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.WorkHandler;
import com.zyf.producer.utils.CommonUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class 公共Bean消费者<T extends BaseBeanContext> implements WorkHandler<T>, EventHandler<T>, LifecycleAware {

    private static volatile boolean isMock = false;

    public static final AtomicBoolean isPrintError = new AtomicBoolean(false);
    public static final AtomicBoolean isDonePrint = new AtomicBoolean(false);
    public final AtomicBoolean isInit = new AtomicBoolean(false);
    // 如果使用 synchronized (this) 是可以使用hashMap的
    protected final Map<String, String> propertyByColumnNameMap = new HashMap<>();
    protected final static Map<Class, Db> dbMap = new ConcurrentHashMap<>();
    protected final Set<String> propertys = new HashSet<>();
    @Setter
    @Getter
    protected AtomicInteger consumerCounter = new AtomicInteger(1);
    protected volatile String tableName;
    protected volatile Entity entity;
    protected volatile String key;
    private volatile Db db;
    private final ThreadLocal<Integer> countThreadLocal = new TransmittableThreadLocal<>();

    public 公共Bean消费者() {
        if (db == null) {
            synchronized (this.getClass()) {
                if (db == null) {
                    log.info("{} 初始化数据库链接...", this.getClass().getSimpleName());
//                    db = getDb();
                    db = dbMap.computeIfAbsent(this.getClass(), k -> getDb());
                    key = StrUtil.blankToDefault(getKey(), this.toString());
                }
            }
        }
    }

    public abstract Db getDb();

    @Override
    public void onEvent(T context) throws Exception {
        if (context.isDone()) {
            if (!isDonePrint.get()) {
                synchronized (isDonePrint) {
                    if (!isDonePrint.get()) {
                        log.info("{} 主动取消!!", key);
                        isDonePrint.set(true);
                    }
                }
            }
            return;
        }
        countThreadLocal.set(consumerCounter.get());
        try {
            CommonUtil.timerExcute(() -> {
                        try {
                            return 消费数据(context);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    },
                    totalTime -> {
                        synchronized (consumerCounter) {
                            int count = consumerCounter.getAndIncrement();
                            countThreadLocal.set(count);
                            log.info(StrUtil.format("{} 消费{}条数据耗时: {}ms={}s", key, count, totalTime, totalTime / 1000));
                        }
                    });
            if (log.isDebugEnabled()) {
                log.debug("{} 消费完毕!", key);
            }
        } finally {
            countThreadLocal.remove();
        }

    }

    @Override
    public void onEvent(T t, long sequence, boolean b) throws Exception {
        onEvent(t);
    }

    protected abstract boolean 消费数据(T data) throws Exception;

    protected String getKey() {
        return null;
    }

    protected <R> void 写入数据库(R r) {
        写入数据库(null, r);
    }

    protected <R> void 写入数据库(String inputTableName, R r) {
        final Integer count = countThreadLocal.get();
        final String simpleName = r.getClass().getSimpleName();
        if (log.isDebugEnabled()) {
            log.debug("第{}条, 调用 写入数据库!!", count);
        }

        // 因为本身会被并发调用，导致此处重复并发，修改propertys与循环propertys同时执行导致报错
        if (!isInit.get()) {
            synchronized (this) {
                if (!isInit.get()) {
                    if (log.isDebugEnabled()) {
                        log.debug("{} 结构解析开始...", simpleName);
                    }
                    final Class<?> rClass = r.getClass();
                    if (inputTableName == null) {
                        this.tableName = CommonUtil.getEntityTableName(rClass);
                    }
                    if (entity == null) {
                        entity = Entity.create(this.tableName);
                    }
                    entity = Entity.create(this.tableName);
                    CommonUtil.consumerFields(rClass, (fieldName, columnName) -> {
                        propertyByColumnNameMap.put(fieldName, columnName);
                        propertys.add(fieldName);
                    });
                    isInit.set(true);
                    if (log.isDebugEnabled()) {
                        log.debug("{} 结构解析完成!!", simpleName);
                    }
                }
            }
        }

        for (String property : propertys) {
            final String colunmName = propertyByColumnNameMap.get(property);
            final Object fieldValue = ReflectUtil.getFieldValue(r, property);
            entity.set(colunmName, fieldValue);
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("{} 第{}条, 执行新增操作...", simpleName, count);
            }
            db.insert(entity);
            if (log.isDebugEnabled()) {
                log.debug("{} 第{}条, 执行完毕!", simpleName, count);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (isMock) {
            try {
                TimeUnit.MILLISECONDS.sleep(RandomUtil.randomInt(3000, 7000));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onStart() {
        // synchronized (this) {
        //     final int count = consumerCounter.get();
        //     log.info("{} 第{}条, 开始执行...", this, count);
        // }
    }

    @Override
    public void onShutdown() {
        // synchronized (this) {
        //     final int count = consumerCounter.get();
        //     log.info("{} 第{}条, 执行完毕!", this, count);
        // }
    }
}
