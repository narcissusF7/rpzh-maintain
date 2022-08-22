package com.rpzh.Task;

import cn.hutool.core.date.DateUtil;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.TaskManagementService;
import com.rpzh.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.rpzh.util.SendMessageUtil;
import java.util.*;
import com.rpzh.vo.MessageVo;

/**
 * 每天9点推送过期的任务
 * @Author liubc
 * @create 2022-07-19 16:53
 * @Description: good good study,day day up
 */
@Component
@Slf4j
public class ExpiredTask {

    //redis中用户key前缀
    private static final String USER = "USER_";

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private TaskManagementService taskManagementService;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    //mq主题
    @Value("${rocketmq.topic}")
    private String topic;
    /**
     * 每天9点发送过期的任务
     */
    @Scheduled(cron="0 0 9 * * ?")
    public void sendExpiredTask(){
        //查询所有逾期的任务
        List<Map> overdueTaskList = taskManagementService.findAllOverdueTask();
        //没有过期的任务不推送
        if (ToolUtil.isNotEmpty(overdueTaskList)){
            //key:用户账号,value:任务编号
            Map<String,List<String>> mapList = new HashMap<>();
            for (Map map : overdueTaskList) {
                //任务负责人
                String principal = map.get("principal").toString();
                //任务编号
                List<String> stringList = new ArrayList<>();
                //去重后的,key:负责人,val:空
                mapList.put(principal,stringList);
            }
            for (Map map : overdueTaskList) {
                //任务编号
                String taskNum = map.get("taskNum").toString();
                //任务负责人
                String principal = map.get("principal").toString();
                List<String> list = mapList.get(principal);
                list.add(taskNum);
            }
            //所有的负责人账号
            Set<String> usernameSet = mapList.keySet();
            Iterator<String> iterator = usernameSet.iterator();
            while (iterator.hasNext()){
                //用户账号
                String username = iterator.next();
                //当前用户手机号
                String phonenumber = (String)redisUtil.hget(USER + username, "phonenumber");
                //当前用户所有的任务编号
                List<String> list = mapList.get(username);
                //所有的任务编号拼写字符串
                StringBuilder sb = new StringBuilder("智慧养护系统:你有过期的任务未处理,过期任务编号,");
                for (String taskNum : list) {
                    sb.append(taskNum).append(",");
                }
                sb.append("请及时处理");
                log.info("发送短信内容:"+sb.toString());
                MessageVo messageVo = SendMessageUtil.sendSms(phonenumber, sb.toString(), "h1");
                log.info("发送结果:"+messageVo.getResult());
                Map map = new HashMap();
                map.put("username", username);
                //短信类型,过期任务提醒
                map.put("type", "18");
                //发送内容
                map.put("content", sb.toString());
                //发送短信时间
                String nowTime = DateUtil.dateNew(new Date()).toString();
                map.put("createTime",nowTime);
                //发送短信手机号
                map.put("phone",phonenumber);
                rocketMQTemplate.send(topic + ":" , MessageBuilder.withPayload(map).build()) ;
                try {
                    //防止短信频繁发送失败
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("定时任务发送短信:"+"账号:"+username+",手机号:"+phonenumber);
            }
            /**
             * 01:虫情巡查  10:垛内湿度
             * 02:翻垛任务  11:仓间湿度
             * 03:移垛任务  12:包芯温度
             * 04:垛内甲虫  13:平均包芯高于环境
             * 05:仓间甲虫  14:仓间粉螟
             * 06:垛内氧气  15:仓库检查
             * 07:仓间氧气  16:其他
             * 08:垛内温度  17:找回密码
             * 09:仓间温度  18:任务逾期提醒
             */
        }
    }

}