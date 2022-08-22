package com.rpzh.Task;
import com.rpzh.dao.*;
import com.rpzh.entity.ToolUtil;
import com.rpzh.util.RedisUtil;
import com.rpzh.vo.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import com.rpzh.util.SendMessageUtil;
import org.springframework.messaging.support.MessageBuilder;
/**
 * @Author: zyq
 * @Date: 2022/06/16/16:27
 * @Description: 三级每两个小时推送一次至任务结束
 */
@Data
@Slf4j
@Component
public class EarlyWarningTask {

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

    @Scheduled(cron="0 42 14 * * ? ")
    public void configureTasks() {
        //获取所有未处理的三级预警的位置路由
        List<String> siteRoutes = earlyWarningDao.findSiteRoute();
        for (String siteRoute : siteRoutes) {
            String siteName = "";
            //拼接短信内容
            Map<String, String> messageInfo = getMessage(siteRoute);
            //判断该位置是仓间还是垛位
            if(siteRoute.contains("05")){
                //获取垛位id
                int stackId = Integer.parseInt(siteRoute.split("05:")[1].split("]")[0]);
                //查询垛位名称、负责人账号
                Map<String, String> stackMap = maintainPlanDao.findStackById(stackId);
                //垛位名称
                siteName = stackMap.get("siteName");
                //将负责人账号转为list类型
                List<String> names = Arrays.asList(stackMap.get("name").split(","));
                for (String principalName : names) {
                    //短信超过460个字进行处理,获取用户手机号码,发送短信,并且将短信信息存入数据库中
                    getSend(siteName,principalName,messageInfo);
                }
            }else{
                //获取仓间id
                int areaId = Integer.parseInt(siteRoute.split("04:")[1].split("]")[0]);
                //查询仓间名称、负责人账号
                WareAreaVo wareAreaVo = warehouseDao.findWareAreaPrincipalName(areaId);
                siteName = wareAreaVo.getSiteName();
                //如果负责人账号为空,则向上找(仓间->层->楼栋->仓库)
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
                //短信超过460个字进行处理,获取用户手机号码,发送短信,并且将短信信息存入数据库中
                getSend(siteName,principalName,messageInfo);
            }
        }
    }

    /*
    * 发送短信,并且将短信信息存入数据库中
    * */
    public void get(String message,Map map,String number){
        try {
            //要发送的短信内容
            map.put("content",message);
            //发送预警短信
            SendMessageUtil.sendSms(number, message, "h2");
            //将预警信息通过rocketMQ发送到短信管理中,再在短信管理中将信息存入数据库中
            rocketMQTemplate.send(topic + ":" , MessageBuilder.withPayload(map).build()) ;
            //短信间隔时间为2秒钟
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
    * 短信超过460个字进行处理,获取用户手机号码,发送短信,并且将短信信息存入数据库中
    * */
    public void getSend(String siteName,String name,Map<String, String> messageInfo){
        //时间格式化
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //该位置有预警信息
        if(!"0".equals(messageInfo.get("count"))){
            //最终要发送的短信内容
            String message = "位置:"+ siteName +"。三级预警有:"+ messageInfo.get("count") +",其中:"+messageInfo.get("message");
            Map map = new HashMap();
            String number = redisUtil.hget("USER_"+name,"phonenumber")+"";
            //用户名
            map.put("username",name);
            //短信类型,其他类型
            map.put("type", "16");
            map.put("data", "发送养护预警短信内容");
            //发送短信手机号
            map.put("phone", number);
            //发送短信时的时间
            map.put("createTime",sdf.format(new Date()));
            if(message.length()<=460){
                //长度不大于460,发送一次短信
                get(message,map,number);
            }else if(message.length()>460 && message.length()<=920){
                //长度大于460,不大于920,发送两次短信
                String oneMessage = message.substring(0,460);
                get(oneMessage,map,number);
                String twoMessage = message.substring(460,message.length());
                get(twoMessage,map,number);
            }else if(message.length()>920 && message.length()<=1380){
                //长度大于920,不大于1380,发送三次短信
                String oneMessage = message.substring(0,460);
                get(oneMessage,map,number);
                String twoMessage = message.substring(460,960);
                get(twoMessage,map,number);
                String threeMessage = message.substring(960,message.length());
                get(threeMessage,map,number);
            }
        }
    }

    /*
    * 拼接短信内容
    * */
    public Map<String,String> getMessage(String siteRoute){
        //该位置的三级的总预警数
        Integer count = 0;
        //短信内容
        String message = "";
        //获取有设备故障的设备id
        List<String> faultNodeIds = earlyWarningDao.findNodeFaultEarlyData(siteRoute);
        if(ToolUtil.isNotEmpty(faultNodeIds)){
            count = faultNodeIds.size();
            message = message + "设备故障有" + String.join(",",faultNodeIds)+"。";
        }
        List<String> warningTypes = Arrays.asList("04","05","06","07","08","09","10","11","12","14");
        List<String> types = Arrays.asList("垛内虫数有", "仓间甲虫有", "垛内氧气有", "仓间氧气有", "垛内温度有", "仓间温度有", "垛内湿度有", "仓间湿度有", "包芯温度有","仓间粉螟有");
        for (int i = 0; i < warningTypes.size(); i++) {
            //根据预警类型查询设备id（除设备故障、平均包芯高于环境预警、平均包芯低于环境预警外）
            List<Map> data = earlyWarningDao.findThreeEarlyData(siteRoute, warningTypes.get(i));
            if(ToolUtil.isNotEmpty(data)){
                count = count + data.size();
                //获取该预警类型的设备id
                List<String> nodeIds = data.stream().map((Map map) -> map.get("nodeId").toString()).collect(Collectors.toList());
                //获取该预警类型的数值
                List<String> warningVals = data.stream().map((Map map) -> map.get("warningVal").toString()).collect(Collectors.toList());
                message = message + types.get(i) + String.join(",",nodeIds)+",当前值分别为:" + String.join(",",warningVals)+"。";
            }
        }
        //查询平均包芯高于环境预警的数值
        String highVal = earlyWarningDao.findVal(siteRoute, "13");
        if(ToolUtil.isNotEmpty(highVal)){
            count = count + 1;
            message = message + "平均包芯高于环境的数值为:" + highVal+"。";
        }
        //查询平均包芯低于环境预警的数值
        String lowVal = earlyWarningDao.findVal(siteRoute,"19");
        if(ToolUtil.isNotEmpty(lowVal)){
            count = count + 1;
            message = message + "平均包芯低于环境的数值为:" + lowVal+"。";
        }
        Map<String,String> map = new HashMap<>();
        //要发送的短信内容
        map.put("message",message);
        //该位置的总预警数
        map.put("count",count+"");
        return map;
    }
}
