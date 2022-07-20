package com.rpzh.Task;

import cn.hutool.core.date.DateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 每天9点推送过期的任务
 * @Author liubc
 * @create 2022-07-19 16:53
 * @Description: good good study,day day up
 */
@Component
public class ExpiredTask {

    /**
     * 每天9点发送过期的任务
     */
    @Scheduled(cron="0 0 9 * * ?")
    public void sendExpiredTask(){

    }

}