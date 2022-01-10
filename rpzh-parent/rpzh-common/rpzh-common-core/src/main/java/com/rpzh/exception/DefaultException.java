package com.rpzh.exception;

import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理
 * @Author liubc成
 * @create 2021/3/29 20:20
 */
@ControllerAdvice
public class DefaultException {
    /**
     * 捕获全局异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody //需要跟controller返回值一样
    public Result error(Exception e){
        e.printStackTrace();
        return new Result(false, StatusCode.ERROR,e.getMessage());
    }
}