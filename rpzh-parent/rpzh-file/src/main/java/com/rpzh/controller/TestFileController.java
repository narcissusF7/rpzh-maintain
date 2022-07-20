package com.rpzh.controller;

import com.rpzh.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
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
public class TestFileController {

    @Autowired
    private RedisTemplate redisTemplate;


    @RequestMapping("/test")
    public String test(){
        return "发送成功";
    }

    /**
     * http://localhost:16900/maintain/test/test2
     * @return
     */
    @RequestMapping("/testredis")
    public String test2(String username){
        redisTemplate.opsForHash().put("USER_"+"rpzh","nickname","超级管理员");
        redisTemplate.opsForHash().put("USER_"+"rpzh","phonenumber","13156555668");
        redisTemplate.opsForHash().put("USER_"+"rpzh","userId","1");
        String nickname = (String) redisTemplate.opsForHash().get("USER_"+username, "nickname");
        String phonenumber = (String) redisTemplate.opsForHash().get("USER_"+username, "phonenumber");
        System.out.println(nickname);
        System.out.println(phonenumber);
        System.out.println("---------------------");
        return "成功";
    }

}