package com.rpzh.controller;

import com.rpzh.dao.TestDao;
import org.springframework.beans.factory.annotation.Autowired;
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


    @Autowired
    private TestDao testDao;

    @RequestMapping("/test")
    public String test(){
        String str = testDao.findTest();
        return "发送成功定时任务"+str;
    }
}