package com.zyf.producer.base.sql;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.handler.HandleHelper;
import com.zyf.producer.base.状态;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public abstract class 公共Sql获产者<T extends BaseSqlContext> extends 公共Sql生产者<T> {

    private static volatile boolean isMock = false;
    protected final BlockingQueue<T> contextQueue = new ArrayBlockingQueue<>(1000);

    public void 流式查询数据(Db db, String sql, int rate, Function<Entity, T> consumer) throws Exception {
        AtomicInteger size = new AtomicInteger(0);
        CountDownLatch cd = new CountDownLatch(1);
        final Thread thread = new Thread(() -> {
            log.info("执行查询sql!");
            final String sqlStr = StrUtil.format(sql);
            try {
                查询(db, sqlStr, rate, resultSet -> {
                    log.info("sql 查询完毕, 开始处理数据......");
                    try {
                        final ResultSetMetaData meta = resultSet.getMetaData();
                        final int columnCount = meta.getColumnCount();
                        boolean isDown = false;
                        // 如果 buffer 中的数据已经被全部消费完了,JDBC 驱动就会自动从数据库中再次拉取 1000 条数据到 buffer 中
                        while (resultSet.next()) {
                            final int i = size.incrementAndGet();
                            if (i > rate / 2) {
                                cd.countDown();
                                isDown = true;
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("游标查询第{}条数据", i);
                            }
                            // 处理每条记录
                            final Entity entity = HandleHelper.handleRow(columnCount, meta, resultSet, Entity.class);
                            final T t = consumer.apply(entity);
                            推值(t);
                            if (isDone.get()) {
                                log.info("数据库查询执行完毕!!!");
                                isDone.set(true);
                                break;
                            }
                            if (isMock) {
                                try {
                                    TimeUnit.MILLISECONDS.sleep(RandomUtil.randomInt(300, 1000));
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        log.info("数据已经查询完!!!");
                        if (!isDone.get()) {
                            isDone.set(true);
                        }
                        if (!isDown) {
                            cd.countDown();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
        cd.await();
    }

    private void 查询(Db db, String sql, int rate, Consumer<ResultSet> resultSetConsumer) throws SQLException {
        log.info("执行sql: {}", sql);
        // 此处游标查询
        // 创建 PreparedStatement  对象, 设置 ResultSet 类型为 TYPE_FORWARD_ONLY 和 CONCUR_READ_ONLY
        try (PreparedStatement preparedStatement = db.getConnection().prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            // 设置 fetchSize 属性, 控制每次从数据库获取的数据量
            preparedStatement.setFetchSize(rate);
            // StatementUtil.fillParams(preparedStatement, new Object[]{});
            // 执行查询
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSetConsumer.accept(resultSet);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void 推值(T context) {
        try {
            contextQueue.offer(context, 1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected 状态 拉值(Consumer<T> consumer) throws InterruptedException {
        final T t = contextQueue.poll(1, TimeUnit.SECONDS);
        if (t != null) {
            if (log.isDebugEnabled()) {
                log.debug("从队列中取出1条数据执行!");
            }
            consumer.accept(t);
            return 状态.push;
        }
        if (log.isDebugEnabled()) {
            log.debug("无法从队列中取出数据!!!");
        }
        if (isDone.get()) {
            if (log.isDebugEnabled()) {
                log.debug("队列结束!!");
            }
            return 状态.stop;
        }
        return 状态.none;
    }

    @Override
    protected 状态 数据设置属性(int seqNo, T context) throws Exception {
        return 拉值(推送前设置值(context));
    }

    protected abstract Consumer<T> 推送前设置值(T context) throws Exception;
}
