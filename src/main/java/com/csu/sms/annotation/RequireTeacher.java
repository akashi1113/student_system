package com.csu.sms.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要教师权限注解
 * 标记此注解的方法只有教师（role=1）可以访问
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireTeacher {
    String message() default "权限不足，只有教师可以访问";
} 