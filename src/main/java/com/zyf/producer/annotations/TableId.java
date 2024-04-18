package com.zyf.producer.annotations;

import com.zyf.producer.enums.IdType;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface TableId {
    String value() default "";

    IdType type() default IdType.NONE;
}
