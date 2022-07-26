package com.rpzh.service.impl;

import com.rpzh.dao.SchedulerDao;
import com.rpzh.entity.ScheduleJob;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.SchedulerService;
import com.rpzh.task.QuartzJobFactory;
import com.rpzh.task.QuartzJobFactoryDisallowConcurrentExecution;
import com.rpzh.utils.LogUtils;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

/**
 * @Author liubc
 * @create 2022-07-26 9:54
 * @Description: good good study,day day up
 */
@Service
public class SchedulerServiceImpl implements SchedulerService {

    @Autowired
    private SchedulerDao schedulerDao;

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @Transactional(propagation = Propagation.REQUIRED)
    public void addJob(ScheduleJob job) throws Exception {
        if (job == null || !ScheduleJob.STATUS_RUNNING.equals(job.getJobStatus())) {
            return;
        }

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        LogUtils.info(scheduler + ".......................................................................................add");
        TriggerKey triggerKey = TriggerKey.triggerKey(job.getJobName(), job.getJobGroup());

        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

        // 不存在，创建一个
        if (null == trigger) {
            Class clazz = ScheduleJob.CONCURRENT_IS.equals(job.getIsConcurrent()) ? QuartzJobFactory.class : QuartzJobFactoryDisallowConcurrentExecution.class;

            JobDetail jobDetail = JobBuilder.newJob(clazz).withIdentity(job.getJobName(), job.getJobGroup()).build();

            jobDetail.getJobDataMap().put("scheduleJob", job);

            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());

            trigger = TriggerBuilder.newTrigger().withIdentity(job.getJobName(), job.getJobGroup()).withSchedule(scheduleBuilder).build();

            scheduler.scheduleJob(jobDetail, trigger);
        } else {
            // Trigger已存在，那么更新相应的定时设置
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());

            // 按新的cronExpression表达式重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();

            // 按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
        }
    }

    /**
     * 初始化执行定时任务,启动时加入定时任务
     */
    @PostConstruct
    public void init() throws Exception {
        List<ScheduleJob> jobList = schedulerDao.findAllJob();
        for (ScheduleJob scheduleJob : jobList) {
            addJob(scheduleJob);
        }
    }

    /**
     * 添加定时任务
     *
     * @param scheduleJob
     */
    @Override
    public void addTask(ScheduleJob scheduleJob) {
        scheduleJob.setCreateTime(new Date());
        scheduleJob.setUpdateTime(new Date());
        schedulerDao.insert(scheduleJob);
        try {
            addJob(scheduleJob);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询任务名称是否存在
     *
     * @param jobGroup
     * @return
     */
    @Override
    public int findJobName(String jobGroup) {
        int count = schedulerDao.findJobName(jobGroup);
        return count;
    }

    /**
     * 修改任务状态
     *
     * @param jobId
     * @param cmd
     */
    @Override
    public void changeJobStatus(Long jobId, String cmd) {
        //查询任务
        ScheduleJob scheduleJob = schedulerDao.findJobById(jobId);
        if (ToolUtil.isEmpty(scheduleJob)){
            return;
        }
        //0停止任务
        if ("0".equals(cmd)) {
            scheduleJob.setJobStatus(ScheduleJob.STATUS_NOT_RUNNING);
            try {
                deleteJob(scheduleJob);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        } else if ("1".equals(cmd)) {
            //1开启任务
            scheduleJob.setJobStatus(ScheduleJob.STATUS_RUNNING);
            try {
                addJob(scheduleJob);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        schedulerDao.updateStatus(jobId,cmd);
    }

    /**
     * 修改定时任务执行时间
     *
     * @param jobId
     * @param cron
     */
    @Override
    @Transactional
    public void updateCron(Long jobId, String cron) {
        //查询任务
        ScheduleJob scheduleJob = schedulerDao.findJobById(jobId);
        if (ToolUtil.isEmpty(scheduleJob)){
            return;
        }
        scheduleJob.setCronExpression(cron);
        if (ScheduleJob.STATUS_RUNNING.equals(scheduleJob.getJobStatus())) {
            try {
                updateJobCron(scheduleJob);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
        schedulerDao.updateJob(jobId,cron);
    }

    /**
     * 查询任务id
     *
     * @return
     */
    @Override
    public List<Long> findJobId() {
        List<Long> idList = schedulerDao.findJobId();
        return idList;
    }

    /**
     * 删除暂停任务
     * @param scheduleJob
     * @throws
     */
    public void deleteJob(ScheduleJob scheduleJob) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.deleteJob(jobKey);
    }

    /**
     * 更新job时间表达式
     *
     * @param scheduleJob
     * @throws
     */
    public void updateJobCron(ScheduleJob scheduleJob) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());

        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression());

        trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();

        scheduler.rescheduleJob(triggerKey, trigger);
    }






}