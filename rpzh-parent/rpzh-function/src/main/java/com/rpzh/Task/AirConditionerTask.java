package com.rpzh.Task;

import com.alibaba.fastjson.JSONObject;
import com.rpzh.dao.NodeDao;
import com.rpzh.handler.DataHandler;
import com.rpzh.parse.ParseData;
import com.rpzh.util.PortDataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AirConditionerTask {
    @Autowired
    NodeDao nodeDao;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    DataHandler dataHandler;

    //TODO 需要接口地址
    private static String weatherUrl = "";
//    @Scheduled(cron="${time.cron}")
    void handleAirConditionerData() {

        String s = PortDataUtils.GetPortData(weatherUrl);
        JSONObject dataOfJson = JSONObject.parseObject(s);
        //数据整理成parseData
        String siteRoute="";
        dataHandler.handlerData(new ParseData(),siteRoute);
    }
}
