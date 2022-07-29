package com.rpzh.run;
import com.rpzh.dao.MaintainDataDao;
import com.rpzh.entity.ToolUtil;
import com.rpzh.utils.*;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 预警任务
 * @Author liubc
 * @create 2022-07-26 13:57
 * @Description: good good study,day day up
 */
@Component
public class EarlyWarning {

    //redis中用户key前缀
    private static final String USER = "USER_ID_";
    private static final String USER_ID = "MAINTIAN_USER_ID_USERNAME";
    RedisUtil redisUtil = SpringUtils.getBean(RedisUtil.class);
    MaintainDataDao maintainDataDao = SpringUtils.getBean(MaintainDataDao.class);
    RocketMQTemplate rocketMQTemplate =  SpringUtils.getBean(RocketMQTemplate.class);

    //mq主题
    private String topic = "messageTopic";

    public void work(){
        System.out.println("预警任务执行");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<String> siteRoutes = maintainDataDao.findBySiteRoute();
        for (String siteRoute : siteRoutes) {
            String siteName = "";
            Integer countEarly = maintainDataDao.findCountEarly(siteRoute);
            List<Map<String, String>> phonenumber = new ArrayList<>();
            if (siteRoute.contains("05")) {
                int stackId = Integer.parseInt(siteRoute.split("05:")[1].split("]")[0]);
                Map<String, String> stackMap = maintainDataDao.findStackById(stackId);
                siteName = stackMap.get("siteName");
                List<String> principalIds = Arrays.asList(stackMap.get("ids").split(","));
                for (String pid : principalIds) {
                    Map map = new HashMap();
                    String principalName = (String) redisUtil.hget(USER_ID, USER + pid);
                    map.put("number", (String) redisUtil.hget("USER_" + principalName, "phonenumber"));
                    map.put("username", principalName);
                    phonenumber.add(map);
                }
            } else {
                int areaId = Integer.parseInt(siteRoute.split("04:")[1].split("]")[0]);
                Map<String, String> areaMap = maintainDataDao.findWareAreaPrincipalName(areaId+"");
                siteName = areaMap.get("siteName");
                String principalName = areaMap.get("pName");
                if (ToolUtil.isEmpty(principalName)) {
                    Map<String, String> floorMap = maintainDataDao.findFloorPrincipalName(areaMap.get("floorId"));
                    principalName = floorMap.get("siteName");
                    if (ToolUtil.isNotEmpty(principalName)) {
                        Map<String, String> buildingMap = maintainDataDao.findBuildingPrincipalName(floorMap.get("buildingId"));
                        principalName = buildingMap.get("pName");
                        if (ToolUtil.isNotEmpty(principalName)) {
                            principalName = maintainDataDao.findWarehousePrincipalName(buildingMap.get("warehouseId"));
                        }
                    }
                }
                Map map = new HashMap();
                map.put("number", (String) redisUtil.hget("USER_" + principalName, "phonenumber"));
                map.put("username", principalName);
                phonenumber.add(map);
            }
            String message = "位置:" + siteName + "。预警数:" + countEarly + "。";
            List<String> warningGrades = Arrays.asList("一级预警有:", "二级预警有:", "三级预警有:");
            for (int i = 1; i < 4; i++) {
                Integer warningGrade = maintainDataDao.findCountWarningGrade(siteRoute, i + "");
                if (warningGrade > 0) {
                    message = message + warningGrades.get(i - 1) + warningGrade + ",其中:";
                    message = getMessage(message, i + "", siteRoute);
                }
            }
            for (Map number : phonenumber) {
                Map map = new HashMap();
                map.put("username", number.get("username"));
                map.put("type", "16");
                map.put("data", "发送养护预警短信内容");
                map.put("phone", number.get("number"));
                map.put("createTime", sdf.format(new Date()));
                if (message.length() <= 460) {
                    get(message, map, number.get("number").toString());
                } else if (message.length() > 460 && message.length() <= 920) {
                    String oneMessage = message.substring(0, 460);
                    get(oneMessage, map, number.get("number").toString());
                    String twoMessage = message.substring(460, message.length());
                    get(twoMessage, map, number.get("number").toString());
                } else if (message.length() > 920 && message.length() <= 1380) {
                    String oneMessage = message.substring(0, 460);
                    get(oneMessage, map, number.get("number").toString());
                    String twoMessage = message.substring(460, 960);
                    get(twoMessage, map, number.get("number").toString());
                    String threeMessage = message.substring(960, message.length());
                    get(threeMessage, map, number.get("number").toString());
                }
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

    public String getMessage(String message,String warningGrade,String siteRoute){
        List<String> oneData = maintainDataDao.findEarlyData(siteRoute, warningGrade, "warning_type in ('01','02','03')");
        if(ToolUtil.isNotEmpty(oneData)){
            message = message + "设备故障有" + String.join(",",oneData)+"。";
        }
        List<String> types = Arrays.asList("垛内虫数有", "仓间甲虫有", "垛内氧气有", "仓间氧气有", "垛内温度有", "仓间温度有", "垛内湿度有", "仓间湿度有", "包芯温度有", "平均包芯高于环境有", "仓间粉螟有");
        for (int i = 4; i <=14 ; i++) {
            String warningType = "0" + i;
            if(i>=10){
                warningType = i + "";
            }
            List<String> data = maintainDataDao.findEarlyData(siteRoute,warningGrade, "warning_type = '"+warningType + "'");
            if(ToolUtil.isNotEmpty(data)){
                message = message + types.get(i-4) + String.join(",",data)+"。";
            }
        }
        return message;
    }
}