package com.rpzh.Task;

import com.alibaba.fastjson.JSONObject;
import com.rpzh.dao.NodeDao;
import com.rpzh.util.WeatherUtils;
import com.rpzh.vo.WeatherDataVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WeatherTask {

    @Autowired
    NodeDao nodeDao;

    @Autowired
    RedisTemplate redisTemplate;

    @Scheduled(cron="${time.cron}")
    void insertWeatherData() {
        String s = WeatherUtils.GetWeatherData();
        JSONObject dataOfJson = JSONObject.parseObject(s); // json天气数据
        JSONObject nowWeather = JSONObject.parseObject(dataOfJson.getString("now"));

        Double temp = nowWeather.getDouble("temp");
        String weatherName = nowWeather.getString("text");
        Double humidity = nowWeather.getDouble("humidity");
        Double precip = nowWeather.getDouble("precip");

        nodeDao.insertWeatherData(weatherName,temp,humidity,precip);

        WeatherDataVo weatherDataVo = new WeatherDataVo();
        weatherDataVo.setTemp(temp);
        weatherDataVo.setWeatherName(weatherName);
        weatherDataVo.setHumidity(humidity);
        weatherDataVo.setPrecip(precip);

        redisTemplate.opsForValue().set("MAINTAIN_WEATHER",weatherDataVo);

    }
}
