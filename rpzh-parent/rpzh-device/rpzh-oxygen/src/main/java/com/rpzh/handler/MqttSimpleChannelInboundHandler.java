//package com.rpzh.handler;
//
//
//import com.alibaba.fastjson.JSONObject;
//import com.rpzh.vo.O2DataVo;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.integration.annotation.ServiceActivator;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageHandler;
//import org.springframework.messaging.MessageHeaders;
//import org.springframework.messaging.MessagingException;
//import org.springframework.stereotype.Component;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//
//
///**
// * 接收氧气数据
// *
// * @author wbl
// * @create 2021/11/22 14:34
// */
//@Component
//@Slf4j
//public class MqttSimpleChannelInboundHandler implements MessageHandler {
//
//    @Override
//    @ServiceActivator(inputChannel = "mqttInputChannelHumiture")
//    public void handleMessage(Message<?> message) throws MessagingException {
//        MessageHeaders messageHeaders = message.getHeaders();
//        List<String> mqttReceivedTopics = Arrays.asList(Objects.requireNonNull(messageHeaders.get("mqtt_receivedTopic")).
//                toString().split("/"));
//        // 是否是第一次的时间相等
//        boolean isDateTimeEqOneTime = true;
//        // 日表json
//        JSONObject yesterdayJson = null;
//        // 接收到的数据
//        if ("uplink".equals(mqttReceivedTopics.get(1))) {
//
//        }
//    }
//    /**
//     * 获得设备id,网关,位置等信息
//     *
//     * @param
//     * @return
//     */
//    private O2DataVo disposeO2DataVo(Map<String, Object> map, String dataTime) {
//        //存放当前数据
//        O2DataVo O2DataVoNow = new O2DataVo();
//
//        String deviceAddress = map.get("deviceId").toString();
//        O2DataVoNow.setDeviceId(deviceAddress);
//        //获取设备备注
//        String remark = (String) map.get("remark");
//        O2DataVoNow.setRemark(remark);
//        //获取网关id
//        String gatewayAddress = map.get("gatewayAddress").toString();
//        O2DataVoNow.setGatewayId(gatewayAddress);
//        //获取设备采集位置
//        String gatherLocation = (String) map.get("gatherLocation");
//        O2DataVoNow.setGatherLocation(gatherLocation);
//        // 设置时间
//        O2DataVoNow.setCreateTime(dataTime);
//        return O2DataVoNow;
//    }
//}