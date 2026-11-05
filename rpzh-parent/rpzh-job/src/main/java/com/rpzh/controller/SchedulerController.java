package com.rpzh.controller;

import com.rpzh.dto.MessageIssueDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.ScheduleJob;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.SchedulerService;
import com.rpzh.utils.SpringUtils;
import org.quartz.CronScheduleBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;

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
     * 查询定时任务状态
     * @return
     */
    @PostMapping("/findStatus")
    public Result findStatus(){
        String status = schedulerService.findStatus();
        return new Result(true, StatusCode.OK, "查询状态成功", status);
    }


    /**
     * 修改任务状态
     * @param jobId
     * @param cmd
     * @return
     */
    @PostMapping("/changeJobStatus")
    public Result changeJobStatus(@RequestParam("jobId") Long jobId, @RequestParam("cmd") String cmd){
        if (ToolUtil.isEmpty(jobId) || ToolUtil.isEmpty(cmd)){
            return new Result(false, StatusCode.ERROR, "任务id和任务命令不能为空");
        }
        if (!("0".equals(cmd) || "1".equals(cmd))){
            return new Result(false, StatusCode.ERROR, "命令错误,0:停止,1:启动");
        }
        schedulerService.changeJobStatus(jobId,cmd);
        return new Result(true, StatusCode.OK, "修改状态成功");
    }

    /**
     * 改变定时任务状态
     * 定时任务是否生效0失效1生效
     * @param status
     * @return
     */
    @PostMapping("/changeStatus")
    public Result changeStatus(@RequestParam("status") String status){
        if (!("0".equals(status) || "1".equals(status))){
            return new Result(false, StatusCode.ERROR, "状态错误,状态只能传0和1");
        }
        //这是数据库中的定时任务id,不能修改
        Long jobId1 = 1L;
        Long jobId2 = 2L;
        schedulerService.changeJobStatus(jobId1,status);
        schedulerService.changeJobStatus(jobId2,status);
        return new Result(true, StatusCode.OK, "修改任务状态成功");
    }


    /**
     * 修改定时任务执行时间
     * @param jobId
     * @param cron
     * @return
     */
    @PostMapping("/updateCron")
    public Result updateCron(@RequestParam("jobId") Long jobId, @RequestParam("cron") String cron){
        if (ToolUtil.isEmpty(jobId) || ToolUtil.isEmpty(cron)){
            return new Result(false, StatusCode.ERROR, "任务id和表达式不能为空");
        }
        schedulerService.updateCron(jobId,cron);
        return new Result(true, StatusCode.OK, "修改定时任务成功");
    }

    /**
     * 短信下发设置
     * @param messageIssueDto
     * @return
     *  每天1点执行一次：   0 0 1 * * ?
     *
     *  每天0点执行一次：   0 0 0 * * ?
     *
     *  每天23点执行一次：  0 0 23 * * ?
     *
     *  每天中午12点触发:   0 0 12 * * ?
     */
    @PostMapping("/messageIssue")
    public Result messageIssue(MessageIssueDto messageIssueDto){
        //获取执行时间开始时间
        String startTime = messageIssueDto.getStartTime();
        //获取执行时间结束时间
        String endTime = messageIssueDto.getEndTime();
        if (ToolUtil.isEmpty(startTime) || ToolUtil.isEmpty(endTime)){
            return new Result(false, StatusCode.ERROR, "开始时间和结束时间不能为空");
        }
        //定时任务表达式
        String cronStart = "0 0 "+startTime+" * * ?";
        String cronEnd = "0 0 "+endTime+" * * ?";
        try {
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronStart);
            CronScheduleBuilder scheduleBuilder1 = CronScheduleBuilder.cronSchedule(cronEnd);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, StatusCode.ERROR, "表达式错误,不能解析");
        }
        //定时任务,每天两个任务执行,
        List<Long> idList = schedulerService.findJobId();
        Long jobId1 = idList.get(0);
        Long jobId2 = idList.get(1);
        schedulerService.updateCron(jobId1,cronStart);
        schedulerService.updateCron(jobId2,cronEnd);
        return new Result(true,StatusCode.OK,"发送成功");
    }


}