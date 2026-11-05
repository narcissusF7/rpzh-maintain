package com.rpzh.mq;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 任务到期延时消息生产者
 * <p>
 * 原方案使用 RabbitMQ 的 TTL + 死信队列实现任意时长延时，现改为 RocketMQ 延迟消息：
 * RocketMQ 4.x 只支持 18 个固定延迟等级（最长 2 小时），因此这里按剩余时间就近选择一个
 * 不大于剩余时间的等级投递，消费端收到后若任务仍未到期会再次续投，直到真正到期为止。
 *
 * @Author liubc
 */
@Component
@Slf4j
public class TaskExpireProducer {

    /**
     * RocketMQ 4.x 默认 18 个延迟等级对应的毫秒数，下标 0 对应 level = 1
     * 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     */
    private static final long[] DELAY_LEVEL_MILLIS = {
            1000L, 5000L, 10000L, 30000L,
            60000L, 120000L, 180000L, 240000L, 300000L, 360000L, 420000L, 480000L, 540000L, 600000L,
            1200000L, 1800000L, 3600000L, 7200000L
    };

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 任务到期提醒的主题
     */
    @Value("${rocketmq.task-expire-topic:TaskExpireTopic}")
    private String topic;

    /**
     * 投递一条任务到期延时消息
     *
     * @param taskNum 任务编号
     * @param endTime 任务结束时间（到期时间）
     */
    public void sendTaskExpire(String taskNum, Date endTime) {
        long remainMs = endTime == null ? 0L : DateUtil.between(new Date(), endTime, DateUnit.MS);
        JSONObject payload = new JSONObject();
        payload.put("taskNum", taskNum);
        payload.put("expireAt", endTime == null ? System.currentTimeMillis() : endTime.getTime());
        String body = payload.toJSONString();

        int delayLevel = chooseDelayLevel(remainMs);
        try {
            if (delayLevel <= 0) {
                // 已到期或剩余时间不足 1 秒，直接投递
                rocketMQTemplate.syncSend(topic, MessageBuilder.withPayload(body).build());
                log.info("任务已到期,立即发送到期消息:任务编号:{},剩余毫秒:{}", taskNum, remainMs);
            } else {
                rocketMQTemplate.syncSend(topic, MessageBuilder.withPayload(body).build(), 3000, delayLevel);
                log.info("发送任务到期延时消息成功:任务编号:{},结束时间:{},剩余毫秒:{},延迟等级:{}",
                        taskNum, DateUtil.dateNew(endTime).toString(), remainMs, delayLevel);
            }
        } catch (Exception e) {
            log.error("发送任务到期延时消息失败:任务编号:{},剩余毫秒:{}", taskNum, remainMs, e);
        }
    }

    /**
     * 选择延迟等级：取"不超过剩余时间"的最大等级，避免提前触发；
     * 剩余时间超过最大等级（2 小时）时使用最大等级，由消费端续投。
     *
     * @param remainMs 剩余毫秒数
     * @return 延迟等级 1~18，返回 0 表示立即投递
     */
    public int chooseDelayLevel(long remainMs) {
        if (remainMs < DELAY_LEVEL_MILLIS[0]) {
            return 0;
        }
        int level = 1;
        for (int i = 0; i < DELAY_LEVEL_MILLIS.length; i++) {
            if (DELAY_LEVEL_MILLIS[i] <= remainMs) {
                level = i + 1;
            } else {
                break;
            }
        }
        return level;
    }
}
