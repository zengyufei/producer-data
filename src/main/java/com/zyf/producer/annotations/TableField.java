package com.zyf.producer.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface TableField {
    String value() default "";

    boolean exist() default true;

    String condition() default "";

    String update() default "";

    boolean select() default true;

    boolean keepGlobalFormat() default false;

    String property() default "";

    boolean javaType() default false;

    String numericScale() default "";
}
