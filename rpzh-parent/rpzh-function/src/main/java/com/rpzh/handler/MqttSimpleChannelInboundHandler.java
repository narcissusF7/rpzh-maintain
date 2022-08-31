package com.rpzh.handler;


import com.alibaba.fastjson.JSONObject;
import com.rpzh.entity.ToolUtil;
import com.rpzh.parse.ParseData;
import com.rpzh.util.RedisUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author wbl
 * @version 0.1.0
 * @create 2021-06-29 16:17
 * @description mqtt入站
 */
@Component
@Slf4j
public class MqttSimpleChannelInboundHandler implements MessageHandler {
    private static final String RPZH_MAINTAIN_FCNT = "RPZH_MAINTAIN_FCNT_";
    @Resource
    private RedisUtil redisUtil;

    @Resource
    private DataHandler dataHandler;

    /**
     * 接收MQTT传入的消息
     *
     * @param message 消息
     * @throws MessagingException 消息异常
     */
    @SneakyThrows
    @Override
    @ServiceActivator(inputChannel = "mqttInputChannelMaintain")
    @Async
    public void handleMessage(Message<?> message) throws MessagingException {
        MessageHeaders messageHeaders = message.getHeaders();
        List<String> mqttReceivedTopics = Arrays.asList(Objects.requireNonNull(messageHeaders.get("mqtt_receivedTopic")).
                toString().split("/"));
        Map map = JSONObject.parseObject(message.getPayload().toString(), Map.class);
        if ("uplink".equals(mqttReceivedTopics.get(1))) {
            ParseData parseData = new ParseData(map,mqttReceivedTopics.get(2));

            //获取缓存中的帧计数器
            Object redisFcnt = redisUtil.get(RPZH_MAINTAIN_FCNT + map.get("deviceAddress"));
            if (ToolUtil.isNotEmpty(redisFcnt)){
                //当前的帧计数器
                int nowfCnt = Integer.parseInt(map.get("fCnt").toString());
                //缓存中的帧计数器
                int oldfCnt = Integer.parseInt(redisFcnt.toString());
                //相减的值-1为丢包个数
                parseData.setPacketLossNum(nowfCnt-oldfCnt);
            }else {

                parseData.setPacketLossNum(0);
            }
            //将帧计数器放置到缓存中
            redisUtil.set(RPZH_MAINTAIN_FCNT + map.get("deviceAddress"),map.get("fCnt"));
//                dataHandler.handlerData(parseData);
        }
    }
}