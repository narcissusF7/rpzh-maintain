package com.rpzh.Task;

import com.alibaba.fastjson.JSONObject;
import com.rpzh.dao.AirConditioningDao;
import com.rpzh.dao.NodeDao;
import com.rpzh.handler.MaintainDataHandler;
import com.rpzh.parse.ParseData;
import com.rpzh.pojo.AirCondition;
import com.rpzh.util.PortDataUtils;
import com.rpzh.vo.AirConditionVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AirConditionerTask {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    MaintainDataHandler dataHandler;

    @Resource
    AirConditioningDao airConditioningDao;


    private static String TagDataUrl = "http://10.46.150.174/api/TagDatas";
    @Scheduled(cron="0 0 0/2 * * ?")
    public void handleAirConditionerData() {
        log.error("进来定时任务了");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        List<AirConditionVo> list = airConditioningDao.findAllAirCondition();
        list.forEach(airConditionVo -> {
            //设置属性
            HashMap<String, String> map = new HashMap<>();
            map.put("TagName",airConditionVo.getTempTag());
            map.put("SamplesCount","1");
            map.put("DtSt",sdf.format(new Date()));
            map.put("DtEnd",sdf.format(new Date()));
            //获取数据
            String tempData = PortDataUtils.sendPost(TagDataUrl,map);

            //转成json字符串获取value值
            JSONObject jsonObject = JSONObject.parseObject(tempData);
            String tempVal = jsonObject.getJSONArray("Data").getJSONObject(0).getString("Value");
            airConditionVo.setTempVal(Double.valueOf(String.format("%.2f",Double.valueOf(tempVal))));

            map.put("TagName",airConditionVo.getHumidityTag());

            //获取数据
            String humidityData = PortDataUtils.sendPost(TagDataUrl,map);

            //转成json字符串获取value值
            JSONObject jsonObject1 = JSONObject.parseObject(humidityData);
            String humidityVal = jsonObject1.getJSONArray("Data").getJSONObject(0).getString("Value");
            airConditionVo.setHumidityVal(Double.valueOf(String.format("%.2f",Double.valueOf(humidityVal))));

        });
        dataHandler.handlerData(list);
    }
}
