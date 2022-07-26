package com.rpzh.run;

import org.springframework.stereotype.Component;

/**
 * 定时任务测试
 * @Date 2020/3/18 17:37
 * @Created by gangye
 */
@Component
public class AddNumWorker {

    public void work(){
        System.out.println("定时任务执行");
        //productNumService.productNumForAdd();
    }

}
