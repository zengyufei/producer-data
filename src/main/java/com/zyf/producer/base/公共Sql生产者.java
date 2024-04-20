package com.zyf.producer.base;

import com.lmax.disruptor.RingBuffer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public abstract class 公共Sql生产者<T extends BaseSqlContext> {
    public static final AtomicBoolean isDonePrint = new AtomicBoolean(false);
    public final static AtomicBoolean isDone = new AtomicBoolean(false);
    @Getter
    @Setter
    protected RingBuffer<T> ringBuffer;

    public static void jucBool(AtomicBoolean value, Runnable runnable) {
        if (!value.get()) {
            synchronized (value) {
                if (!value.get()) {
                    runnable.run();
                    value.set(true);
                }
            }
        }
    }

    public 状态 生产数据(int seqNo) throws Exception {
        long nextSeq = ringBuffer.next();
        if (log.isDebugEnabled()) {
            log.debug("获取 nextSeq=={}", nextSeq);
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("获取 context=={}", nextSeq);
            }
            T context = ringBuffer.get(nextSeq);
            if (log.isDebugEnabled()) {
                log.debug("获取 nextSeq=={} 成功!! 开始清理 context", nextSeq);
            }
            context.clear();
            if (log.isDebugEnabled()) {
                log.debug("nextSeq=={} 清理 context 成功!! 开始设置属性!!!", nextSeq);
            }
            final 状态 status = 数据设置属性(seqNo, context);
            if (log.isDebugEnabled()) {
                log.debug("nextSeq=={} 设置属性成功! 返回状态: {}", nextSeq, status.name());
            }
            context.setDone(status == 状态.stop);
            if (status == 状态.stop) {
                if (!isDonePrint.get()) {
                    // synchronized (isDonePrint) {
                    //     if (!isDonePrint.get()) {
                    //         log.warn("结束生产!");
                    //         isDonePrint.set(true);
                    //     }
                    // }
                    jucBool(isDonePrint, () -> {
                        log.warn("结束生产!");
                    });
                }
            }
            if (status == 状态.push || status == 状态.stop) {
                if (log.isDebugEnabled()) {
                    log.debug("nextSeq=={}, 状态={} 开始 publish", nextSeq, status.name());
                }
                ringBuffer.publish(nextSeq);
                if (log.isDebugEnabled()) {
                    log.debug("nextSeq=={}, 状态={} publish 成功!!", nextSeq, status.name());
                }
            }
            return status;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract 状态 数据设置属性(int seqNo, T data) throws Exception;
}
