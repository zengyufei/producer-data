package com.zyf.producer.utils;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommonUtil {

    public static void timerExcute(Supplier<Boolean> ExcuteInterface, Consumer<Long> timerMethod) throws Exception {
        long beginTime = System.currentTimeMillis();
        final Boolean aBoolean = ExcuteInterface.get();
        long endTime = System.currentTimeMillis();
        if (aBoolean) {
            long totalTime = endTime - beginTime;
            timerMethod.accept(totalTime);
        }
    }
}
