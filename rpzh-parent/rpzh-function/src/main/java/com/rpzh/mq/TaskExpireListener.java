package com.rpzh.mq;

import com.alibaba.fastjson.JSONObject;
import com.rpzh.service.TaskManagementService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 任务到期监听器
 * <p>
 * 替代原 RabbitMQ 的 queue2 监听：收到消息后先判断任务是否真的到期，
 * 未到期则按剩余时间续投一条新的延迟消息（解决 RocketMQ 延迟等级最长只有 2 小时的问题），
 * 到期则把未完成的任务状态置为逾期。
 *
 * @Author liubc
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "${rocketmq.task-expire-topic:TaskExpireTopic}",
        consumerGroup = "task-expire-consumer-group",
        consumeMode = ConsumeMode.CONCURRENTLY
)
public class TaskExpireListener implements RocketMQListener<String> {

    /**
     * 提前触发容差：小于该值认为已到期，避免毫秒级误差导致反复续投
     */
    private static final long TOLERANCE_MS = 1000L;

    @Autowired
    private TaskManagementService taskManagementService;

    @Autowired
    private TaskExpireProducer taskExpireProducer;

    @Override
    public void onMessage(String message) {
        log.info("收到任务到期消息:{}", message);
        JSONObject payload;
        try {
            payload = JSONObject.parseObject(message);
        } catch (Exception e) {
            log.error("任务到期消息格式错误,丢弃:{}", message, e);
            return;
        }
        String taskNum = payload.getString("taskNum");
        Long expireAt = payload.getLong("expireAt");
        if (taskNum == null || expireAt == null) {
            log.error("任务到期消息缺少必要字段,丢弃:{}", message);
            return;
        }
        long remainMs = expireAt - System.currentTimeMillis();
        if (remainMs > TOLERANCE_MS) {
            // 还没到期：按剩余时间续投
            log.info("任务尚未到期,续投延时消息:任务编号:{},剩余毫秒:{}", taskNum, remainMs);
            taskExpireProducer.sendTaskExpire(taskNum, new Date(expireAt));
            return;
        }
        // 已到期：只有未完成(status='0')的任务会被置为逾期(status='2')
        int updateFlag = taskManagementService.mqUpdateStatus(taskNum);
        if (updateFlag > 0) {
            log.info("任务已过期,状态已更新为逾期:任务编号:{}", taskNum);
        } else {
            log.info("任务已处理或不存在,跳过:任务编号:{}", taskNum);
        }
    }
}
