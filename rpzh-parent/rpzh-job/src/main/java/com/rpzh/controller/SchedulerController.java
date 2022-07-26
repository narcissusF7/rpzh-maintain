package com.rpzh.controller;

import com.rpzh.entity.Result;
import com.rpzh.entity.ScheduleJob;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.SchedulerService;
import com.rpzh.utils.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 定时任务管理
 * @Author liubc
 * @create 2022-07-26 9:37
 * @Description: good good study,day day up
 */
@RestController
@RequestMapping("/scheduler")
@CrossOrigin
public class SchedulerController {


    @Autowired
    private SchedulerService schedulerService;

    /**
     * 每天1点执行一次：   0 0 1 * * ?
     *
     * 每天23点执行一次：  0 0 23 * * ?
     *
     * 每天中午12点触发:   0 0 12 * * ?
     *
     */

    /**
     * 添加定时任务
     * @param scheduleJob
     * @return
     * http://localhost:15899/scheduler/addJob
     */
    @PostMapping("/addJob")
    @Transactional
    public Result addJob(ScheduleJob scheduleJob) throws Exception {
        String springId = scheduleJob.getSpringId();
        if (ToolUtil.isEmpty(springId)){
            return new Result(false,StatusCode.ERROR,"SpringId 不能为空");
        }
        //Object bean = SpringUtils.getBean(springId);
//        if (ToolUtil.isNotEmpty(bean)){
//            return new Result(false, StatusCode.ERROR, "bean已经存在,禁止添加");
//        }
        String jobGroup = scheduleJob.getJobGroup();
        String jobName = scheduleJob.getJobName();
        if (ToolUtil.isEmpty(jobGroup) || ToolUtil.isEmpty(jobName)){
            return new Result(false, StatusCode.ERROR, "工作名称和工作组合不能为空");
        }
        if (!(jobGroup.equals(jobName))){
            return new Result(false, StatusCode.ERROR, "名称和组不一样,禁止添加");
        }
        int count = schedulerService.findJobName(jobGroup);
        if (count > 0){
            return new Result(false, StatusCode.ERROR, "任务名称已经存在,禁止添加");
        }
        Object obj = null;
        Class clazz = null;
        try {
            clazz = Class.forName(scheduleJob.getBeanClass());
            obj = clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, StatusCode.ERROR, "Class名称有误,不能被解析");
        }
        schedulerService.addTask(scheduleJob);
        Object bean = SpringUtils.getBean(springId);
        if (ToolUtil.isEmpty(bean)){
            Object beanByType = SpringUtils.getBeanByType(clazz);
            SpringUtils.setBean(scheduleJob.getSpringId(), beanByType);
        }
        return new Result(true, StatusCode.OK, "添加定时任务成功");
    }


    /**
     * 修改任务状态
     * @param jobId
     * @param cmd
     * @return
     */
    @PostMapping("/changeJobStatus")
    public Result changeJobStatus(Long jobId, String cmd){
        if (ToolUtil.isEmpty(jobId) || ToolUtil.isEmpty(cmd)){
            return new Result(false, StatusCode.ERROR, "任务id和任务命令不能为空");
        }
        if (!("0".equals(cmd) || "1".equals(cmd))){
            return new Result(false, StatusCode.ERROR, "命令错误,0:停止,1:启动");
        }
        schedulerService.changeJobStatus(jobId,cmd);
        return new Result(true, StatusCode.OK, "修改任务状态成功");
    }


    /**
     * 修改定时任务执行时间
     * @param jobId
     * @param cron
     * @return
     */
    @PostMapping("/updateCron")
    public Result updateCron(Long jobId, String cron){
        if (ToolUtil.isEmpty(jobId) || ToolUtil.isEmpty(cron)){
            return new Result(false, StatusCode.ERROR, "任务id和表达式不能为空");
        }
        schedulerService.updateCron(jobId,cron);
        return new Result(true, StatusCode.OK, "修改定时任务成功");
    }

}