package com.rpzh.service;

import com.rpzh.entity.ScheduleJob;

/**
 * 定时任务
 * @Author liubc
 * @create 2022-07-26 9:54
 * @Description: good good study,day day up
 */
public interface SchedulerService {

    /**
     * 添加定时任务
     * @param scheduleJob
     */
    void addTask(ScheduleJob scheduleJob);

    /**
     * 查询任务名称是否存在
     * @param jobGroup
     * @return
     */
    int findJobName(String jobGroup);

    /**
     * 修改任务状态
     * @param jobId
     * @param cmd
     */
    void changeJobStatus(Long jobId, String cmd);

    /**
     * 修改定时任务执行时间
     * @param jobId
     * @param cron
     */
    void updateCron(Long jobId, String cron);
}
