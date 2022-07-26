package com.rpzh.util;

import com.rpzh.dao.EarlyWarningDao;
import com.rpzh.dao.MaintainPlanDao;
import com.rpzh.dao.WarehouseDao;
import com.rpzh.feign.UserFeign;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
public class SendUtils implements Job {

    UserFeign userFeign = SpringUtils.getBean(UserFeign.class);
    WarehouseDao warehouseDao = SpringUtils.getBean(WarehouseDao.class);
    MaintainPlanDao maintainPlanDao = SpringUtils.getBean(MaintainPlanDao.class);
    EarlyWarningDao earlyWarningDao = SpringUtils.getBean(EarlyWarningDao.class);
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //TODO   换成修改的标记flag
        if(maintainPlanDao.findStageId(32,1)<4){
            log.info("111111");
            Scheduler scheduler = null;
            try {
                scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.deleteJob(new JobKey("job1", "group1"));
                scheduler.deleteJob(new JobKey("job2", "group2"));
                JobDetail jobDetail = JobBuilder.newJob(SendUtils.class) //加载任务类，与HelloJob完成绑定，要求HelloJob实现Job接口
                        .withIdentity("job1", "group1") //参数1：任务的名称（唯一实例）；参数2：任务组的名称
                        .build();
                //TODO   将cron改成从数据库中查询
                CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "group1").withSchedule(CronScheduleBuilder.cronSchedule("0 0 6 * * ?"))
                        .build();
                scheduler.scheduleJob(jobDetail, trigger);
                scheduler.start();
                JobDetail jobDetail1 = JobBuilder.newJob(SendUtils.class) //加载任务类，与HelloJob完成绑定，要求HelloJob实现Job接口
                        .withIdentity("job2", "group2") //参数1：任务的名称（唯一实例）；参数2：任务组的名称
                        .build();
                //TODO   将cron改成从数据库中查询
                CronTrigger trigger1 = TriggerBuilder.newTrigger().withIdentity("trigger2", "group2").withSchedule(CronScheduleBuilder.cronSchedule("0 0 7 * * ?"))
                        .build();
                scheduler.scheduleJob(jobDetail1, trigger1);
                scheduler.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        List<String> siteRoutes = earlyWarningDao.findBySiteRoute();
        for (String siteRoute : siteRoutes) {
            String siteName = "";
            if(siteRoute.contains("05")){
                siteName = warehouseDao.findStackById(Integer.parseInt(siteRoute.split("05:")[1].split("]")[0])).getSiteName();
            }else{
                siteName = warehouseDao.findWareArea(Integer.parseInt(siteRoute.split("04:")[1].split("]")[0])).getSiteName();
            }
            Integer countEarly = earlyWarningDao.findCountEarly(siteRoute);
            List<String> oneEarlyData = earlyWarningDao.findEarlyData(siteRoute, "1");
            List<String> twoEarlyData = earlyWarningDao.findEarlyData(siteRoute, "2");
            List<String> threeEarlyData = earlyWarningDao.findEarlyData(siteRoute, "3");
            String message = "位置:"+ siteName +"。预警个数:"+countEarly +"。一级预警有:"+ oneEarlyData.size()+",分别是:"+ oneEarlyData + "。二级预警有:"+
                    twoEarlyData.size()+",分别是:"+ twoEarlyData + "。三级预警有:"+ threeEarlyData.size()+",分别是:"+ threeEarlyData + "。";
            log.info(message);
        }
            /*String principalId = maintainPlanDao.findPrincipalId(stackId);
            String number = userFeign.findPhoneNumberById(principalId).getData().toString();*/
                    /*MessageVo messageVo = SendMessageUtil.sendSms(number, "每天第一个固定时间点推送短信", info);
                    //当为0的时候发送成功
                    log.info(messageVo.getResult());*/
    }

    public String getWarningType(String type){
        switch (type){
            case "04": return "垛内甲虫预警";
            case "05": return "仓间甲虫预警";
            case "06": return "垛内氧气预警";
            case "07": return "仓间氧气预警";
            case "08": return "垛内温度预警";
            case "09": return "仓间温度预警";
            case "10": return "垛内湿度预警";
            case "11": return "仓间湿度预警";
            case "12": return "包芯温度预警";
            case "13": return "平均包芯高于环境预警";
            case "14": return "仓间粉螟预警";
            default: return "";
        }
    }
    public String getNodeType(String type){
        switch (type){
            case "01": return "氧气传感器";
            case "03": return "温湿度传感器";
            case "04": return "包芯温度传感器";
            default: return "";
        }
    }
    public String getWarningGrade(Integer warningGrade){
        switch (warningGrade){
            case 1: return "一级预警";
            case 2: return "二级预警";
            case 3: return "三级预警";
            default: return "";
        }
    }
}
