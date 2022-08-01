package com.rpzh.Task;
import com.rpzh.dao.*;
import com.rpzh.entity.ToolUtil;
import com.rpzh.util.RedisUtil;
import com.rpzh.util.SendMessageUtil;
import com.rpzh.vo.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
/**
 * @Author: zyq
 * @Date: 2022/06/16/16:27
 * @Description: 三级每两个小时推送一次至任务结束
 */
@Data
@Slf4j
@Component
public class sendWarningThreeTask{

    @Resource
    private EarlyWarningDao earlyWarningDao;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private WarehouseDao warehouseDao;

    @Resource
    private MaintainPlanDao maintainPlanDao;
    private static final String USER = "USER_ID_";
    private static final String USER_ID = "MAINTIAN_USER_ID_USERNAME";
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    //mq主题
    @Value("${rocketmq.topic}")
    private String topic;

    @Scheduled(cron="0 41 11 * * ? ")
    public void configureTasks() {
        List<String> siteRoutes = earlyWarningDao.findSiteRoute("3");
        for (String siteRoute : siteRoutes) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String siteName = "";
            Integer threeWarningGrade = earlyWarningDao.findThreeWarningGradeCount(siteRoute);
            List<Map<String,String>> phonenumber = new ArrayList<>();
            if(siteRoute.contains("05")){
                int stackId = Integer.parseInt(siteRoute.split("05:")[1].split("]")[0]);
                siteName = warehouseDao.findStackById(stackId).getSiteName();
                List<String> principalNames = Arrays.asList(maintainPlanDao.findCurrentMaintainPlan(stackId).split(","));
                for (String name : principalNames) {
                    Map map = new HashMap();
                    map.put("number",(String)redisUtil.hget("USER_"+name,"phonenumber"));
                    map.put("username",name);
                    phonenumber.add(map);
                }
            }else{
                int areaId = Integer.parseInt(siteRoute.split("04:")[1].split("]")[0]);
                siteName = warehouseDao.findWareArea(areaId).getSiteName();
                WareAreaVo wareAreaVo = warehouseDao.findWareAreaPrincipalName(areaId);
                String principalName = wareAreaVo.getPrincipalName();
                if (ToolUtil.isEmpty(principalName)) {
                    FloorVo floorVo = warehouseDao.findFloorPrincipalName(wareAreaVo.getFloorId());
                    principalName = floorVo.getPrincipalName();
                    if (ToolUtil.isNotEmpty(principalName)) {
                        BuildingVo buildingVo = warehouseDao.findBuildingPrincipalName(floorVo.getBuildingId());
                        principalName = buildingVo.getPrincipalName();
                        if (ToolUtil.isNotEmpty(principalName)) {
                            principalName = warehouseDao.findWarehousePrincipalName(buildingVo.getWarehouseId()).getPrincipalName();
                        }
                    }
                }
                Map map = new HashMap();
                map.put("number",(String)redisUtil.hget("USER_"+principalName,"phonenumber"));
                map.put("username",principalName);
                phonenumber.add(map);
            }
            if(threeWarningGrade>0){
                String message = "位置:"+ siteName +"。三级预警有:"+ threeWarningGrade +",其中:";
                message = getMessage(message,siteRoute);
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
    }

    public void get(String message,Map map,String number){
        try {
            map.put("content",message);
            //SendMessageUtil.sendSms(number, message, "h2");
            //系统标识,标识是哪个系统发送的消息,养护系统发送
            rocketMQTemplate.send(topic + ":" , MessageBuilder.withPayload(map).build()) ;
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public  String getMessage(String message,String siteRoute){
        List<Map> oneData = earlyWarningDao.findThreeEarlyData(siteRoute, "warning_type in ('01','02','03')");
        if(ToolUtil.isNotEmpty(oneData)){
            List<String> nodeIds = oneData.stream().map((Map map) -> map.get("nodeId").toString()).collect(Collectors.toList());
            message = message + "设备故障有" + String.join(",",nodeIds)+"。";
        }
        List<String> types = Arrays.asList("垛内虫数有", "仓间甲虫有", "垛内氧气有", "仓间氧气有", "垛内温度有", "仓间温度有", "垛内湿度有", "仓间湿度有", "包芯温度有", "平均包芯高于环境有", "仓间粉螟有");
        for (int i = 4; i <=14 ; i++) {
            String warningType = "0" + i;
            if(i>=10){
                warningType = i + "";
            }
            List<Map> data = earlyWarningDao.findThreeEarlyData(siteRoute, "warning_type = '"+warningType + "' and warning_grade = '3'");
            if(ToolUtil.isNotEmpty(data)){
                List<String> nodeIds = data.stream().map((Map map) -> map.get("nodeId").toString()).collect(Collectors.toList());
                List<String> warningVals = data.stream().map((Map map) -> map.get("warningVal").toString()).collect(Collectors.toList());
                message = message + types.get(i-4) + String.join(",",nodeIds)+",当前值分别为:" + String.join(",",warningVals)+"。";
            }
        }
        return message;
    }
}
