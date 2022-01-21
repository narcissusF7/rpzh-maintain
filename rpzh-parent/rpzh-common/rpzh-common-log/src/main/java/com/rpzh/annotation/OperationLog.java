package com.rpzh.annotation;

import com.rpzh.enums.BusinessType;

import java.lang.annotation.*;

/**
 * @author wbl
 * @version 0.1.0
 * @create 2021-05-31 14:14
 * @description 操作日志记录
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    /**
     * 模块
     */
    String title() default "";
    /**
     * 操作类型
     */
    BusinessType businessType() default BusinessType.OTHER;
}
