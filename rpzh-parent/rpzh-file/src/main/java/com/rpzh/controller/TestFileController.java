package com.rpzh.controller;

import com.rpzh.dao.DbTest;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

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

    @Autowired
    private DbTest dbTest;


    @RequestMapping("/test")
    public String test(){
        return "发送成功";
    }

    /**
     * http://localhost:16900/maintain/test/test2
     * @return
     */
    @RequestMapping("/testredis")
    public String test2(@RequestParam("username") String username){
//        redisTemplate.opsForHash().put("USER_"+"rpzh","nickname","超级管理员");
//        redisTemplate.opsForHash().put("USER_"+"rpzh","phonenumber","13156555668");
        String nickname1 = (String) redisTemplate.opsForHash().get("USER_"+username, "nickname");
        String phonenumber1 = (String) redisTemplate.opsForHash().get("USER_"+username, "phonenumber");
        System.out.println(nickname1);
        System.out.println(phonenumber1);
        System.out.println("---------------------");
        return "成功";
    }

    /**
     * http://localhost:15888/file/testOracle
     * @return
     */
    @RequestMapping("/testOracle")
    public Result testOracle(){
        String name = dbTest.findName();
        return new Result(false, StatusCode.OK,"发送成功",name);
    }



}