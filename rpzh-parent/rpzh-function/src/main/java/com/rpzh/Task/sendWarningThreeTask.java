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

    @Scheduled(cron="0 0 0 * * ? ")
    public void configureTasks() {
        List<String> siteRoutes = earlyWarningDao.findThreeBySiteRoute();
        for (String siteRoute : siteRoutes) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String siteName = "";
            Integer threeWarningGrade = earlyWarningDao.findThreeWarningGradeCount(siteRoute);
            System.out.println(threeWarningGrade);
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
        List<Map> twoData = earlyWarningDao.findThreeEarlyData(siteRoute, "warning_type = '04' and warning_grade = '3'");
        if(ToolUtil.isNotEmpty(twoData)){
            List<String> nodeIds = twoData.stream().map((Map map) -> map.get("nodeId").toString()).collect(Collectors.toList());
            List<String> warningVals = twoData.stream().map((Map map) -> map.get("warningVal").toString()).collect(Collectors.toList());
            message = message + "垛内甲虫有" + String.join(",",nodeIds)+",当前值分别为:" + String.join(",",warningVals)+"。";
        }
        List<Map> threeData = earlyWarningDao.findThreeEarlyData(siteRoute, "warning_type = '05' and warning_grade = '3'");
        if(ToolUtil.isNotEmpty(threeData)){
            List<String> nodeIds = threeData.stream().map((Map map) -> map.get("nodeId").toString()).collect(Collectors.toList());
            List<String> warningVals = threeData.stream().map((Map map) -> map.get("warningVal").toString()).collect(Collectors.toList());
            message = message + "仓间甲虫有" + String.join(",",nodeIds)+",当前值分别为:" + String.join(",",warningVals)+"。";
        }
        List<Map> fourData = earlyWarningDao.findThreeEarlyData(siteRoute, "warning_type = '06' and warning_grade = '3'");
        if(ToolUtil.isNotEmpty(fourData)){
            List<String> nodeIds = fourData.stream().map((Map map) -> map.get("nodeId").toString()).collect(Collectors.toList());
            List<String> warningVals = fourData.stream().map((Map map) -> map.get("warningVal").toString()).collect(Collectors.toList());
            message = message + "垛内氧气有" + String.join(",",nodeIds)+",当前值分别为:" + String.join(",",warningVals)+"。";
        }
        List<Map> fiveData = earlyWarningDao.findThreeEarlyData(siteRoute, "warning_type = '07' and warning_grade = '3'");
        if(ToolUtil.isNotEmpty(fiveData)){
            List<String> nodeIds = fiveData.stream().map((Map map) -> map.get("nodeId").toString()).collect(Collectors.toList());
            List<String> warningVals = fiveData.stream().map((Map map) -> map.get("warningVal").toString()).collect(Collectors.toList());
            message = message + "仓间氧气有" + String.join(",",nodeIds)+",当前值分别为:" + String.join(",",warningVals)+"。";
        }
        List<Map> sixData = earlyWarningDao.findThreeEarlyData(siteRoute, "warning_type = '08' and warning_grade = '3'");
        if(ToolUtil.isNotEmpty(sixData)){
            List<String> nodeIds = sixData.stream().map((Map map) -> map.get("nodeId").toString()).collect(Collectors.toList());
            List<String> warningVals = sixData.stream().map((Map map) -> map.get("warningVal").toString()).collect(Collectors.toList());
            message = message + "垛内温度有" + String.join(",",nodeIds)+",当前值分别为:" + String.join(",",warningVals)+"。";
        }
        List<Map> sevenData = earlyWarningDao.findThreeEarlyData(siteRoute, "warning_type = '09' and warning_grade = '3'");
        if(ToolUtil.isNotEmpty(sevenData)){
            List<String> nodeIds = sevenData.stream().map((Map map) -> map.get("nodeId").toString()).collect(Collectors.toList());
            List<String> warningVals = sevenData.stream().map((Map map) -> map.get("warningVal").toString()).collect(Collectors.toList());
            message = message + "仓间温度有" + String.join(",",nodeIds)+",当前值分别为:" + String.join(",",warningVals)+"。";
        }
        List<Map> eightData = earlyWarningDao.findThreeEarlyData(siteRoute,  "warning_type = '10' and warning_grade = '3'");
        if(ToolUtil.isNotEmpty(eightData)){
            List<String> nodeIds = eightData.stream().map((Map map) -> map.get("nodeId").toString()).collect(Collectors.toList());
            List<String> warningVals = eightData.stream().map((Map map) -> map.get("warningVal").toString()).collect(Collectors.toList());
            message = message + "垛内温度有" + String.join(",",nodeIds)+",当前值分别为:" + String.join(",",warningVals)+"。";
        }
        List<Map> nineData = earlyWarningDao.findThreeEarlyData(siteRoute, "warning_type = '11' and warning_grade = '3'");
        if(ToolUtil.isNotEmpty(nineData)){
            List<String> nodeIds = nineData.stream().map((Map map) -> map.get("nodeId").toString()).collect(Collectors.toList());
            List<String> warningVals = nineData.stream().map((Map map) -> map.get("warningVal").toString()).collect(Collectors.toList());
            message = message + "仓间湿度有" + String.join(",",nodeIds)+",当前值分别为:" + String.join(",",warningVals)+"。";
        }
        List<Map> tenData = earlyWarningDao.findThreeEarlyData(siteRoute, "warning_type = '12' and warning_grade = '3'");
        if(ToolUtil.isNotEmpty(tenData)){
            List<String> nodeIds = tenData.stream().map((Map map) -> map.get("nodeId").toString()).collect(Collectors.toList());
            List<String> warningVals = tenData.stream().map((Map map) -> map.get("warningVal").toString()).collect(Collectors.toList());
            message = message + "包芯温度有" + String.join(",",nodeIds)+",当前值分别为:" + String.join(",",warningVals)+"。";
        }
        List<Map> elevenData = earlyWarningDao.findThreeEarlyData(siteRoute, "warning_type = '13' and warning_grade = '3'");
        if(ToolUtil.isNotEmpty(elevenData)){
            List<String> nodeIds = elevenData.stream().map((Map map) -> map.get("nodeId").toString()).collect(Collectors.toList());
            List<String> warningVals = elevenData.stream().map((Map map) -> map.get("warningVal").toString()).collect(Collectors.toList());
            message = message + "平均包芯高于环境有" + String.join(",",nodeIds)+",当前值分别为:" + String.join(",",warningVals)+"。";
        }
        List<Map> twelveData = earlyWarningDao.findThreeEarlyData(siteRoute,  "warning_type = '14' and warning_grade = '3'");
        if(ToolUtil.isNotEmpty(twelveData)){
            List<String> nodeIds = twelveData.stream().map((Map map) -> map.get("nodeId").toString()).collect(Collectors.toList());
            List<String> warningVals = twelveData.stream().map((Map map) -> map.get("warningVal").toString()).collect(Collectors.toList());
            message = message + "仓间粉螟有" + String.join(",",nodeIds)+",当前值分别为:" + String.join(",",warningVals)+"。";
        }
        return message;
    }
}
