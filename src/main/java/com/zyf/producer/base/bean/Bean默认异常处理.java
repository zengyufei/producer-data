package com.zyf.producer.base.bean;

import com.lmax.disruptor.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Bean默认异常处理
 *
 * @param <T>
 */
@Slf4j
public class Bean默认异常处理<T extends BaseBeanContext> implements ExceptionHandler<T> {
    protected ThreadPoolExecutor consumerPool;

    public Bean默认异常处理() {
    }


    public Bean默认异常处理(ThreadPoolExecutor consumerPool) {
        this.consumerPool = consumerPool;
    }

    @Override
    public void handleEventException(Throwable ex, long sequence, T event) {
        if (!公共Bean消费者.isPrintError.get()) {
            synchronized (公共Bean消费者.class) {
                if (!公共Bean消费者.isPrintError.get()) {
                    公共Bean消费者.isPrintError.set(true);
                    if (ex instanceof ArithmeticException) {
                        log.error("handleEventException {} {}", event, ex.getMessage());
                    } else {
                        log.error("handleEventException {}", event, ex);
                    }
                }
            }
        }
        if (consumerPool != null) {
            consumerPool.shutdown();
        }
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        if (!公共Bean消费者.isPrintError.get()) {
            synchronized (公共Bean消费者.class) {
                if (!公共Bean消费者.isPrintError.get()) {
                    公共Bean消费者.isPrintError.set(true);
                    if (ex instanceof ArithmeticException) {
                        log.error("handleEventException {}", ex.getMessage());
                    } else {
                        log.error("handleEventException", ex);
                    }
                }
            }
        }
        if (consumerPool != null) {
            consumerPool.shutdown();
        }
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        if (!公共Bean消费者.isPrintError.get()) {
            synchronized (公共Bean消费者.class) {
                if (!公共Bean消费者.isPrintError.get()) {
                    公共Bean消费者.isPrintError.set(true);
                    if (ex instanceof ArithmeticException) {
                        log.error("handleEventException {}", ex.getMessage());
                    } else {
                        log.error("handleEventException", ex);
                    }
                }
            }
        }
        if (consumerPool != null) {
            consumerPool.shutdown();
        }
    }
}
