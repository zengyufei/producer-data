package com.zyf.producer.base.bean;

import cn.hutool.core.util.ClassUtil;
import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * 抽象实体
 */
@Data
public class BaseBeanContext implements Serializable {
    private boolean isDone;

    public void clear() {
        // 通过反射，清理所有属性的值，设置为 null
        final Field[] declaredFields = this.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (ClassUtil.isBasicType(field.getType())) {
                continue;
            }
            try {
                field.setAccessible(true);
                field.set(this, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
