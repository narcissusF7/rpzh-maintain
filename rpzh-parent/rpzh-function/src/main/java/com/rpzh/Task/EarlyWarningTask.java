package com.rpzh.Task;
import com.rpzh.dao.*;
import com.rpzh.entity.ToolUtil;
import com.rpzh.util.RedisUtil;
import com.rpzh.util.SiteRouteUtil;
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

    @Scheduled(cron="20 55 11 * * ? ")
    public void configureTasks() {
        //时间格式化
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //获取所有未处理的三级预警的位置路由
        List<Map> siteRouteInfos = earlyWarningDao.findSiteRoute();
        if(ToolUtil.isNotEmpty(siteRouteInfos)) {
            for (Map siteRouteInfo : siteRouteInfos) {
                String siteRoute = siteRouteInfo.get("siteRoute").toString();
                Map warningInfo = earlyWarningDao.findBySiteRoute(siteRoute);
                String siteName = "";
                //判断该位置是仓间还是垛位
                if (ToolUtil.isNotEmpty(warningInfo.get("stackId"))) {
                    //查询垛位名称
                    siteName = earlyWarningDao.findStackById(warningInfo.get("stackId").toString());
                } else {
                    //查询垛位名称
                    siteName = earlyWarningDao.findWareAreaById(warningInfo.get("areaId").toString());
                }
                List<String> ids = Arrays.asList(siteRouteInfo.get("id").toString().split(","));
                List<Map> warningInfos = earlyWarningDao.findWarningInfoBySiteRoute(ids, ids.size());
                //短信内容
                String message = "位置:" + siteName + "。";
                for (Map info : warningInfos) {
                    message = message + SiteRouteUtil.getWarningName(info.get("warningType").toString());
                    if (!"13".equals(info.get("warningType").toString()) && !"19".equals(info.get("warningType").toString())) {
                        message = message + "的设备有" + info.get("nodeId").toString();
                    }
                    if (!"01".equals(info.get("warningType").toString()) && !"02".equals(info.get("warningType").toString()) && !"02".equals(info.get("warningType").toString())) {
                        message = message + ",预警值为" + info.get("warningVal").toString() + "。";
                    }
                }
                //将负责人账号转为list类型
                List<String> names = Arrays.asList(warningInfo.get("principal").toString().split(","));
                for (String name : names) {
                    Map map = new HashMap();
                    //用户账号
                    map.put("username", name);
                    //短信类型
                    map.put("type", "16");
                    //发送目标手机号
                    map.put("number", (String) redisUtil.hget("USER_" + name, "phonenumber"));
                    //发送短信时间
                    map.put("createTime", sdf.format(new Date()));
                    if (message.length() <= 460) {
                        //长度不大于460,发送一次短信
                        get(message, map, map.get("number").toString());
                    } else {
                        //长度大于460,不大于920,发送两次短信
                        String oneMessage = message.substring(0, 460);
                        get(oneMessage, map, map.get("number").toString());
                        String twoMessage = message.substring(460, message.length());
                        get(twoMessage, map, map.get("number").toString());
                    }
                }
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
}
