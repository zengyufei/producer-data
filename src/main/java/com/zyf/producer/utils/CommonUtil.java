package com.zyf.producer.utils;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.zyf.producer.annotations.TableField;
import com.zyf.producer.annotations.TableId;
import com.zyf.producer.annotations.TableName;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommonUtil {

    public static String getEntityTableName(Class<?> clazz) {
        final TableName tableNameAnnotation = clazz.getAnnotation(TableName.class);
        return tableNameAnnotation.value();
    }

    public static void consumerFields(Class<?> clazz, BiConsumer<String, String> consumer) {
        {
            final Field[] declaredFields = ClassUtil.getDeclaredFields(clazz);
            for (Field declaredField : declaredFields) {
                final String fieldName = declaredField.getName();
                if (StrUtil.contains(fieldName, "serialVersionUID")) {
                    continue;
                }
                String columnName;
                final TableId tableIdAnnotation = declaredField.getAnnotation(TableId.class);
                final TableField tableFieldAnnotation = declaredField.getAnnotation(TableField.class);
                if (tableFieldAnnotation != null) {
                    columnName = tableFieldAnnotation.value();
                } else if (tableIdAnnotation != null) {
                    columnName = tableIdAnnotation.value();
                } else {
                    throw new RuntimeException("未找到字段对应的列名");
                }
                if (StrUtil.isBlank(columnName)) {
                    continue;
                }
                consumer.accept(fieldName, columnName);
            }
        }
        {
            final Field[] declaredFields = clazz.getSuperclass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                String columnName;
                final String fieldName = declaredField.getName();
                if (StrUtil.contains(fieldName, "isDo")) {
                    continue;
                }
                final TableField tableFieldAnnotation = declaredField.getAnnotation(TableField.class);
                if (tableFieldAnnotation != null) {
                    columnName = tableFieldAnnotation.value();
                } else {
                    throw new RuntimeException("未找到字段对应的列名");
                }
                if (StrUtil.isBlank(columnName)) {
                    continue;
                }
                consumer.accept(fieldName, columnName);
            }
        }
    }

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
