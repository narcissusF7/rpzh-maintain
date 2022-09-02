package com.rpzh.run;
import com.rpzh.dao.MaintainDataDao;
import com.rpzh.entity.ToolUtil;
import com.rpzh.util.SiteRouteUtil;
import com.rpzh.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.*;
import com.rpzh.util.SendMessageUtil;

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
        List<Map> siteRouteInfos = maintainDataDao.findBySiteRoute();
        //按位置分别查询各自的预警信息
        if(ToolUtil.isNotEmpty(siteRouteInfos)) {
            for (Map siteRouteInfo : siteRouteInfos) {
                String siteName = "";
                String siteRoute = siteRouteInfo.get("route").toString();
                //判断该位置是仓间还是垛位
                if (ToolUtil.isNotEmpty(siteRouteInfo.get("stackId"))) {
                    //查询垛位名称
                    siteName = maintainDataDao.findStackById(siteRouteInfo.get("stackId").toString());
                } else {
                    //查询垛位名称
                    siteName =maintainDataDao.findWareAreaById(siteRouteInfo.get("areaId").toString());
                }
                List<Map> warningInfos = maintainDataDao.findWarningInfoBySiteRoute(siteRoute);
                //短信内容
                String message = "位置:" + siteName+"。";
                for (Map warningInfo : warningInfos) {
                    message = message + SiteRouteUtil.getWarningName(warningInfo.get("warningType").toString());
                    if(!"13".equals(warningInfo.get("warningType").toString()) && !"19".equals(warningInfo.get("warningType").toString())){
                        message = message + "的设备有" + warningInfo.get("nodeId").toString();
                    }
                    if(!"01".equals(warningInfo.get("warningType").toString()) && !"02".equals(warningInfo.get("warningType").toString()) && !"02".equals(warningInfo.get("warningType").toString())){
                        message = message + ",预警等级为:"+ warningInfo.get("warningGrade").toString() + "级,预警值为"+ warningInfo.get("warningVal").toString()+"。";
                    }
                }
                //将负责人账号转为list类型
                List<String> names = Arrays.asList(siteRouteInfo.get("principal").toString().split(","));
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