package com.rpzh.Task;

import com.rabbitmq.client.Channel;
import com.rpzh.service.TaskManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * RabbitMq中任务中心监听,收到消息修改任务的状态,状态修改成过期
 * @Author liubc
 * @create 2022-06-06 11:46
 * @Description: good good study,day day up
 */
@Component
@RabbitListener(queues = "queue2")
@Slf4j
public class MqTaskListener {

    @Autowired
    private TaskManagementService taskManagementService;

    @RabbitHandler
    public void orderhandler(String msg) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = sdf.format(new Date());
        log.info("获取到消息:" + msg + ":时间为:" + format);
        //根据收到的任务编号修改任务状态
        int updateFlag = taskManagementService.mqUpdateStatus(msg);
    }
}