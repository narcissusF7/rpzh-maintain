package com.rpzh.annotation;

import java.lang.annotation.*;

/**
 * @author wbl
 * @version 0.1.0
 * @create 2021-05-25 18:34
 * @description 登录日志记录
 */
@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoginLogging {
}