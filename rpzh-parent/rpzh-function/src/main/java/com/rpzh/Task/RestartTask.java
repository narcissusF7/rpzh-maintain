package com.rpzh.Task;

import com.rpzh.util.SendUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;
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

        @Override
        public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
            taskRegistrar.addTriggerTask(() -> {
                log.info("111");
            }, triggerContext -> {
                Scheduler scheduler = null;
                List<String> jobGroupNames = null;
                try {
                    scheduler = StdSchedulerFactory.getDefaultScheduler();
                    jobGroupNames = scheduler.getJobGroupNames();
                    if (!jobGroupNames.contains("warningCountOne")) {
                        JobDetail jobDetail = JobBuilder.newJob(SendUtils.class) //加载任务类，与HelloJob完成绑定，要求HelloJob实现Job接口
                                .withIdentity("jobOne", "warningCountOne") //参数1：任务的名称（唯一实例）；参数2：任务组的名称
                                .build();
                        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("triggerOne", "warningCountOne").withSchedule(CronScheduleBuilder.cronSchedule("0 13 15 * * ?"))
                                .build();
                        scheduler.scheduleJob(jobDetail, trigger);
                        scheduler.start();
                    }
                    if (!jobGroupNames.contains("warningCountTwo")) {
                        JobDetail jobDetail = JobBuilder.newJob(SendUtils.class) //加载任务类，与HelloJob完成绑定，要求HelloJob实现Job接口
                                .withIdentity("warningCountTwo", "warningCountTwo") //参数1：任务的名称（唯一实例）；参数2：任务组的名称
                                .build();
                        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("triggerTwo", "warningCountTwo").withSchedule(CronScheduleBuilder.cronSchedule("0 0 7 * * ?"))
                                .build();
                        scheduler.scheduleJob(jobDetail, trigger);
                        scheduler.start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            });
        }
}
