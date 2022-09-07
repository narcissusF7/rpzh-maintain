package com.rpzh.Task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rpzh.dao.NodeDao;
import com.rpzh.dao.WeatherDao;
import com.rpzh.util.PortDataUtils;
import com.rpzh.vo.WeatherDataVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import springfox.documentation.spring.web.json.Json;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class WeatherTask {

    @Autowired
    WeatherDao weatherDao;

    @Autowired
    RedisTemplate redisTemplate;

    private static String weatherUrl = "https://devapi.qweather.com/v7/weather/now?location=101210401&key=8fdc54a3ac93474cbe6ae1efa42493fd";

    /**
     * 天气数据redis 存放规则  redisTemplate.opsForValue().set("MAINTAIN_WEATHER",weatherDataVo);
     */
    @Scheduled(cron="${time.cron}")
    void insertWeatherData() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String s = PortDataUtils.GetPortData(weatherUrl);
        JSONObject dataOfJson = JSONObject.parseObject(s); // json天气数据
        JSONObject nowWeather = JSONObject.parseObject(dataOfJson.getString("now"));

        Double temp = nowWeather.getDouble("temp");
        String weatherName = nowWeather.getString("text");
        Double humidity = nowWeather.getDouble("humidity");
        Double precip = nowWeather.getDouble("precip");

        weatherDao.insertWeatherData(weatherName,temp,humidity,precip);

        WeatherDataVo weatherDataVo = new WeatherDataVo();
        weatherDataVo.setTemp(temp);
        weatherDataVo.setWeatherName(weatherName);
        weatherDataVo.setHumidity(humidity);
        weatherDataVo.setPrecip(precip);
        weatherDataVo.setTransferTime(sdf.format(new Date()));

        redisTemplate.opsForValue().set("MAINTAIN_WEATHER", JSON.toJSONString(weatherDataVo));

    }
}
