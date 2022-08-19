package com.rpzh.run;
import com.rpzh.dao.MaintainDataDao;
import com.rpzh.entity.ToolUtil;
import com.rpzh.utils.*;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        log.info("预警定时发送短信任务开始执行");
        //时间格式化
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //获取所有未处理的位置路由
        List<String> siteRoutes = maintainDataDao.findBySiteRoute();
        //按位置分别查询各自的预警信息
        for (String siteRoute : siteRoutes) {
            //位置名称
            String siteName = "";
            //该位置未处理的个数
            Integer countEarly = maintainDataDao.findCountEarly(siteRoute);
            //存放用户账号、手机号
            List<Map<String, String>> phonenumber = new ArrayList<>();
            //判断该位置是仓间还是垛位
            if (siteRoute.contains("05")) {
                //获取垛位id
                int stackId = Integer.parseInt(siteRoute.split("05:")[1].split("]")[0]);
                //查询垛位名称、负责人账号
                Map<String, String> stackMap = maintainDataDao.findStackById(stackId);
                //垛位名称
                siteName = stackMap.get("siteName");
                //将负责人账号转为list类型
                List<String> names = Arrays.asList(stackMap.get("name").split(","));
                //将该垛位的所有负责人账号、手机号存入list中
                for (String name : names) {
                    Map map = new HashMap();
                    map.put("username",name);
                    //根据用户账号从redis中取出手机号
                    map.put("number",(String)redisUtil.hget("USER_"+name,"phonenumber"));
                    phonenumber.add(map);
                }
            } else {
                //获取仓间id
                int areaId = Integer.parseInt(siteRoute.split("04:")[1].split("]")[0]);
                //查询仓间名称、负责人账号
                Map<String, String> areaMap = maintainDataDao.findWareAreaPrincipalName(areaId+"");
                //垛位名称
                siteName = areaMap.get("siteName");
                //如果负责人账号为空,则向上找(仓间->层->楼栋->仓库)
                String principalName = areaMap.get("principalName");
                if (ToolUtil.isEmpty(principalName)) {
                    Map<String, String> floorMap = maintainDataDao.findFloorPrincipalName(areaMap.get("floorId"));
                    principalName = floorMap.get("principalName");
                    if (ToolUtil.isNotEmpty(principalName)) {
                        Map<String, String> buildingMap = maintainDataDao.findBuildingPrincipalName(floorMap.get("buildingId"));
                        principalName = buildingMap.get("principalName");
                        if (ToolUtil.isNotEmpty(principalName)) {
                            principalName = maintainDataDao.findWarehousePrincipalName(buildingMap.get("warehouseId"));
                        }
                    }
                }
                Map map = new HashMap();
                //根据用户账号从redis中取出手机号
                map.put("number", (String) redisUtil.hget("USER_" + principalName, "phonenumber"));
                map.put("username", principalName);
                //将该垛位的负责人账号、手机号存入list中
                phonenumber.add(map);
            }
            //短信内容
            String message = "位置:" + siteName + "。预警数:" + countEarly + "。";
            List<String> warningGrades = Arrays.asList("一级预警有:", "二级预警有:", "三级预警有:");
            for (int i = 1; i < 4; i++) {
                //根据位置路由和预警等级查询预警个数
                Integer warningGrade = maintainDataDao.findCountWarningGrade(siteRoute, i + "");
                //有预警则拼接短信内容
                if (warningGrade > 0) {
                    message = message + warningGrades.get(i - 1) + warningGrade + ",";
                    message = getMessage(message, i + "", siteRoute);
                }
            }
            for (Map number : phonenumber) {
                Map map = new HashMap();
                //用户账号
                map.put("username", number.get("username"));
                //短信类型
                map.put("type", "16");
                //发送目标手机号
                map.put("phone", number.get("number"));
                //发送短信时间
                map.put("createTime", sdf.format(new Date()));
                if (message.length() <= 460) {
                    //长度不大于460,发送一次短信
                    System.out.println(message);
                    System.out.println(number.get("number"));
                    System.out.println(map);
                    get(message, map, number.get("number").toString());
                } else if (message.length() > 460 && message.length() <= 920) {
                    //长度大于460,不大于920,发送两次短信
                    String oneMessage = message.substring(0, 460);
                    get(oneMessage, map, number.get("number").toString());
                    String twoMessage = message.substring(460, message.length());
                    get(twoMessage, map, number.get("number").toString());
                } else if (message.length() > 920 && message.length() <= 1380) {
                    //长度大于920,不大于1380,发送三次短信
                    String oneMessage = message.substring(0, 460);
                    get(oneMessage, map, number.get("number").toString());
                    String twoMessage = message.substring(460, 960);
                    get(twoMessage, map, number.get("number").toString());
                    String threeMessage = message.substring(960, message.length());
                    get(threeMessage, map, number.get("number").toString());
                }
            }
        }
        log.info("预警定时发送短信任务结束执行");
    }
    /*
    * 发送短信内容
    * */
    public void get(String message,Map map,String number){
        try {
            //短信内容
            map.put("content",message);
            //将预警短信信息发送到对应的手机号中
            //SendMessageUtil.sendSms(number, message, "h2");
            //短信间隔1.5s
            Thread.sleep(1500);
            //将预警信息通过rocketMQ发送到短信管理中,再在短信管理中将信息存入数据库中
            rocketMQTemplate.send(topic + ":" , MessageBuilder.withPayload(map).build()) ;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
    * 拼接短信内容
    * */
    public String getMessage(String message,String warningGrade,String siteRoute){
        //查询设备故障的设备id
        List<String> oneData = maintainDataDao.findEarlyData(siteRoute, warningGrade, "warning_type in ('01','02','03')");
        //有设备故障信息则拼接短信
        if(ToolUtil.isNotEmpty(oneData)){
            message = message + "设备故障有" + String.join(",",oneData)+"。";
        }
        List<String> types = Arrays.asList("垛内虫数有", "仓间甲虫有", "垛内氧气有", "仓间氧气有", "垛内温度有", "仓间温度有", "垛内湿度有", "仓间湿度有", "包芯温度有","平均包芯高于环境",  "仓间粉螟有");
        for (int i = 4; i <=14 ; i++) {
            if(i==13){
                continue;
            }
            String warningType = "0" + i;
            if(i>=10){
                warningType = i + "";
            }
            //根据预警类型查询设备id（除设备故障外）
            List<String> data = maintainDataDao.findEarlyData(siteRoute,warningGrade, "warning_type = '"+warningType + "'");
            //有预警信息则拼接短信
            if(ToolUtil.isNotEmpty(data)){
                message = message + types.get(i-4) + String.join(",",data)+"。";
            }
        }
        String highVal = maintainDataDao.findVal(siteRoute, warningGrade, "13");
        if(ToolUtil.isNotEmpty(highVal)){
            message = message + "平均包芯高于环境的数值为:" + highVal+"。";
        }
        String lowVal = maintainDataDao.findVal(siteRoute, warningGrade, "19");
        if(ToolUtil.isNotEmpty(lowVal)){
            message = message + "平均包芯低于环境的数值为:" + lowVal+"。";
        }
        return message;
    }
}