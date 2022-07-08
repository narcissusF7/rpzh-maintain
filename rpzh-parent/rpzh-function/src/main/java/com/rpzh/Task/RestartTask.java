package com.rpzh.Task;

import com.rpzh.entity.ToolUtil;
import com.rpzh.util.SendUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author zyq
 * @create 2022-07-07 13:36
 * @Description: 定时判断服务器是否被重启,如重启需重启预警的定时任务
 */

@Data
@Slf4j
@Component
public class RestartTask implements SchedulingConfigurer {

    @Resource
    private RedisTemplate redisTemplate;

        @Override
        public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
            /*taskRegistrar.addTriggerTask(() -> {
                try {
                    //1、调度器(Schedular)，从工厂中获取调度实例（默认：实例化new StdSchedulerFactory();)
                    Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
                    List<String> jobGroupNames = scheduler.getJobGroupNames();
                    if(!jobGroupNames.contains("group1")){
                        Object firstTime = redisTemplate.opsForValue().get("firstTime")
                        String cron = firstTime.toString();
                        JobDetail jobDetail= JobBuilder.newJob(SendUtils.class) //加载任务类，与HelloJob完成绑定，要求HelloJob实现Job接口
                                .withIdentity("job1","group1") //参数1：任务的名称（唯一实例）；参数2：任务组的名称
                                .build();
                        org.quartz.CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "group1").withSchedule(CronScheduleBuilder.cronSchedule(cron))
                                .build();
                        scheduler.scheduleJob(jobDetail,trigger);
                        scheduler.start();
                    }
                    if(!jobGroupNames.contains("group2")){
                        String cron = redisTemplate.opsForValue().get("secondTime").toString();
                        JobDetail jobDetail= JobBuilder.newJob(SendUtils.class)
                                .withIdentity("job2","group2")
                                .build();
                        org.quartz.CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger2", "group2").withSchedule(CronScheduleBuilder.cronSchedule(cron))
                                .build();
                        scheduler.scheduleJob(jobDetail,trigger);
                        scheduler.start();
                    }
                } catch (SchedulerException e) {
                    e.printStackTrace();
                }
            }, triggerContext -> {
                *//*return new CronTrigger("0 0 0/2 * * ?").nextExecutionTime(triggerContext);*//*
                return new CronTrigger("0/24 * * * * ?").nextExecutionTime(triggerContext);
            });*/
        }
}
