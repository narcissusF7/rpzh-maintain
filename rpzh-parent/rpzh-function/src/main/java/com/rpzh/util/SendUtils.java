package com.rpzh.util;
import com.rpzh.dao.EarlyWarningDao;
import com.rpzh.dao.MaintainPlanDao;
import com.rpzh.dao.WarehouseDao;
import com.rpzh.entity.ToolUtil;
import com.rpzh.vo.BuildingVo;
import com.rpzh.vo.FloorVo;
import com.rpzh.vo.WareAreaVo;
import com.rpzh.vo.WarehouseVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class SendUtils implements Job {

    //redis中用户key前缀
    private static final String USER = "USER_ID_";
    private static final String USER_ID = "MAINTIAN_USER_ID_USERNAME";
    RedisUtil redisUtil = SpringUtils.getBean(RedisUtil.class);
    WarehouseDao warehouseDao = SpringUtils.getBean(WarehouseDao.class);
    MaintainPlanDao maintainPlanDao = SpringUtils.getBean(MaintainPlanDao.class);
    EarlyWarningDao earlyWarningDao = SpringUtils.getBean(EarlyWarningDao.class);
    RocketMQTemplate rocketMQTemplate =  SpringUtils.getBean(RocketMQTemplate.class);

    //mq主题
    private String topic = "messageTopic";
    @Override
    public void execute(JobExecutionContext jobExecutionContext){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //TODO   换成修改的标记flag
        if(5<4){
            log.info("111111");
            Scheduler scheduler = null;
            try {
                scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.deleteJob(new JobKey("jobTwo", "warningCountTwo"));
                scheduler.deleteJob(new JobKey("jobOne", "warningCountOne"));
                JobDetail jobDetail = JobBuilder.newJob(SendUtils.class) //加载任务类，与HelloJob完成绑定，要求HelloJob实现Job接口
                        .withIdentity("jobTwo", "warningCountTwo") //参数1：任务的名称（唯一实例）；参数2：任务组的名称
                        .build();
                //TODO   将cron改成从数据库中查询
                CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("triggerTwo", "warningCountTwo").withSchedule(CronScheduleBuilder.cronSchedule("0 0 6 * * ?"))
                        .build();
                scheduler.scheduleJob(jobDetail, trigger);
                scheduler.start();
                JobDetail jobDetail1 = JobBuilder.newJob(SendUtils.class) //加载任务类，与HelloJob完成绑定，要求HelloJob实现Job接口
                        .withIdentity("jobOne", "warningCountOne") //参数1：任务的名称（唯一实例）；参数2：任务组的名称
                        .build();
                //TODO   将cron改成从数据库中查询
                CronTrigger trigger1 = TriggerBuilder.newTrigger().withIdentity("triggerOne", "warningCountOne").withSchedule(CronScheduleBuilder.cronSchedule("0 0 7 * * ?"))
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
            Integer countEarly = earlyWarningDao.findCountEarly(siteRoute);
            Integer oneWarningGrade = earlyWarningDao.findCountWarningGrade(siteRoute, "1");
            Integer twoWarningGrade = earlyWarningDao.findCountWarningGrade(siteRoute, "2");
            Integer threeWarningGrade = earlyWarningDao.findCountWarningGrade(siteRoute, "3");
            List<Map<String,String>> phonenumber = new ArrayList<>();
            if(siteRoute.contains("05")){
                int stackId = Integer.parseInt(siteRoute.split("05:")[1].split("]")[0]);
                siteName = warehouseDao.findStackById(stackId).getSiteName();
                List<String> principalIds = Arrays.asList(maintainPlanDao.findCurrentMaintainPlan(stackId).split(","));
                for (String pid : principalIds) {
                    Map map = new HashMap();
                    String principalName = (String)redisUtil.hget(USER_ID, USER+pid);
                    map.put("number",(String)redisUtil.hget("USER_"+principalName,"phonenumber"));
                    map.put("username",principalName);
                    phonenumber.add(map);
                }
            }else{
                int areaId = Integer.parseInt(siteRoute.split("04:")[1].split("]")[0]);
                siteName = warehouseDao.findWareArea(areaId).getSiteName();
                WareAreaVo wareAreaVo = warehouseDao.findWareAreaPrincipalName(areaId);
                String id;
                if (ToolUtil.isNotEmpty(wareAreaVo.getPrincipalId())) {
                    id =  wareAreaVo.getPrincipalId()+"";
                }else{
                    FloorVo floorVo = warehouseDao.findFloorPrincipalName(wareAreaVo.getFloorId());
                    if (ToolUtil.isNotEmpty(floorVo.getPrincipalId())) {
                        id =  floorVo.getPrincipalId()+"";
                    }else{
                        BuildingVo buildingVo = warehouseDao.findBuildingPrincipalName(floorVo.getBuildingId());
                        if (ToolUtil.isNotEmpty(buildingVo.getPrincipalId())) {
                            id =  buildingVo.getPrincipalId() + "";
                        }else{
                            WarehouseVo warehouseVo = warehouseDao.findWarehousePrincipalName(buildingVo.getWarehouseId());
                            id =  warehouseVo.getPrincipalId() + "";
                        }
                    }
                }
                String principalName = (String)redisUtil.hget(USER_ID, USER+id);
                Map map = new HashMap();
                map.put("number",(String)redisUtil.hget("USER_"+principalName,"phonenumber"));
                map.put("username",principalName);
                phonenumber.add(map);
            }
            String message = "位置:"+ siteName +"。预警数:"+countEarly +"。";
            if(oneWarningGrade>0){
                message = message + "一级预警有:"+ oneWarningGrade +",其中:";
                message = getMessage(message,"1",siteRoute);
            }if(twoWarningGrade>0){
                message = message + "二级预警有:"+ twoWarningGrade +",其中:";
                message = getMessage(message,"2",siteRoute);
            }if(threeWarningGrade>0){
                message = message + "三级预警有:"+ threeWarningGrade +",其中:";
                message = getMessage(message,"3",siteRoute);
            }
            for (Map number : phonenumber) {
                Map map = new HashMap();
                map.put("username",number.get("username"));
                map.put("type", "16");
                map.put("data", "发送养护预警短信内容");
                map.put("phone", number.get("number"));
                map.put("createTime",sdf.format(new Date()));
                if(message.length()<=460){
                    get(message,map,number.get("number").toString());
                }else if(message.length()>460 && message.length()<=920){
                    String oneMessage = message.substring(0,460);
                    get(oneMessage,map,number.get("number").toString());
                    String twoMessage = message.substring(460,message.length());
                    get(twoMessage,map,number.get("number").toString());
                }else if(message.length()>920 && message.length()<=1380){
                    String oneMessage = message.substring(0,460);
                    get(oneMessage,map,number.get("number").toString());
                    String twoMessage = message.substring(460,960);
                    get(twoMessage,map,number.get("number").toString());
                    String threeMessage = message.substring(960,message.length());
                    get(threeMessage,map,number.get("number").toString());
                }
            }
            log.info(message);
        }
    }

    public void get(String message,Map map,String number){
        try {
            map.put("content",message);
            SendMessageUtil.sendSms(number, message, "h2");
            //系统标识,标识是哪个系统发送的消息,养护系统发送
            //rocketMQTemplate.send(topic + ":" , MessageBuilder.withPayload(map).build()) ;
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public  String getMessage(String message,String warningGrade,String siteRoute){
        List<String> oneData = earlyWarningDao.findEarlyData(siteRoute, warningGrade, "warning_type in ('01','02','03')");
        if(ToolUtil.isNotEmpty(oneData)){
            message = message + "设备故障有" + String.join(",",oneData)+"。";
        }
        List<String> twoData = earlyWarningDao.findEarlyData(siteRoute, warningGrade, "warning_type = '04'");
        if(ToolUtil.isNotEmpty(twoData)){
            message = message + "垛内甲虫有" + String.join(",",twoData)+"。";
        }
        List<String> threeData = earlyWarningDao.findEarlyData(siteRoute, warningGrade, "warning_type = '05'");
        if(ToolUtil.isNotEmpty(threeData)){
            message = message + "仓间甲虫有" + String.join(",",threeData)+"。";
        }
        List<String> fourData = earlyWarningDao.findEarlyData(siteRoute, warningGrade, "warning_type = '06'");
        if(ToolUtil.isNotEmpty(fourData)){
            message = message + "垛内氧气有" + String.join(",",fourData)+"。";
        }
        List<String> fiveData = earlyWarningDao.findEarlyData(siteRoute, warningGrade, "warning_type = '07'");
        if(ToolUtil.isNotEmpty(fiveData)){
            message = message + "仓间氧气有" + String.join(",",fiveData)+"。";
        }
        List<String> sixData = earlyWarningDao.findEarlyData(siteRoute, warningGrade, "warning_type = '08'");
        if(ToolUtil.isNotEmpty(sixData)){
            message = message + "垛内温度有" + String.join(",",sixData)+"。";
        }
        List<String> sevenData = earlyWarningDao.findEarlyData(siteRoute, warningGrade, "warning_type = '09'");
        if(ToolUtil.isNotEmpty(sevenData)){
            message = message + "仓间温度有" + String.join(",",sevenData)+"。";
        }
        List<String> eightData = earlyWarningDao.findEarlyData(siteRoute, warningGrade, "warning_type = '10'");
        if(ToolUtil.isNotEmpty(eightData)){
            message = message + "垛内湿度有" + String.join(",",eightData)+"。";
        }
        List<String> nineData = earlyWarningDao.findEarlyData(siteRoute, warningGrade, "warning_type = '11'");
        if(ToolUtil.isNotEmpty(nineData)){
            message = message + "仓间湿度有" + String.join(",",nineData)+"。";
        }
        List<String> tenData = earlyWarningDao.findEarlyData(siteRoute, warningGrade, "warning_type = '12'");
        if(ToolUtil.isNotEmpty(tenData)){
            message = message + "包芯温度有" + String.join(",",tenData)+"。";
        }
        List<String> elevenData = earlyWarningDao.findEarlyData(siteRoute, warningGrade, "warning_type = '13'");
        if(ToolUtil.isNotEmpty(elevenData)){
            message = message + "平均包芯高于环境有" + String.join(",",elevenData)+"。";
        }
        List<String> twelveData = earlyWarningDao.findEarlyData(siteRoute, warningGrade, "warning_type = '14'");
        if(ToolUtil.isNotEmpty(twelveData)){
            message = message + "仓间粉螟有" + String.join(",",twelveData)+"。";
        }
        return message;
    }
}
