package com.rpzh.controller;

import com.rpzh.entity.Result;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author liubc
 * @create 2022-05-31 9:46
 * @Description: good good study,day day up
 */
@RestController
@RequestMapping("/file")
@CrossOrigin
public class TestController {


    @RequestMapping("/test")
    public String test(){
        return "发送成功";
    }

}