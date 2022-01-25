package com.rpzh.annotation;

import com.rpzh.enums.BesinessUserType;

import java.lang.annotation.*;

/**
 * 用户操作日志,用于同步物联网养护仓储用户信息
 * @Author liubc
 * @create 2022/1/17 16:35
 * @Description: good good study,day day up
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationUserLog {
    /**
     * 模块
     */
    String title() default "";
    /**
     * 操作类型
     */
    BesinessUserType businessUserType() default BesinessUserType.OTHER;
}
