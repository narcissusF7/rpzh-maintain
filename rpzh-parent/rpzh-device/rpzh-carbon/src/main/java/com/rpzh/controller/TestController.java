package com.rpzh.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author liubc
 * @create 2022/1/11 14:35
 * @Description: good good study,day day up
 */
@RestController
@CrossOrigin //允许跨域访问
@RequestMapping("/carbon")
public class TestController {


    @RequestMapping("/test")
    public String test(){
        return "发送成功";
    }
}