package com.rpzh.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author liubc
 * @create 2022-07-25 18:48
 * @Description: good good study,day day up
 */
@RestController
@RequestMapping("/quartz")
@CrossOrigin
public class TestConller {

    @RequestMapping("/test")
    public String test(){
        return "发送成功定时任务";
    }
}