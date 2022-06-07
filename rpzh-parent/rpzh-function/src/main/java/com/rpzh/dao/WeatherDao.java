package com.rpzh.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.rpzh.util.MonitorCondition;
import com.rpzh.vo.WeatherDataVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WeatherDao {

    @DS("clickhouse")
    List<WeatherDataVo> findWeatherData(@Param("monitorCondition") MonitorCondition monitorCondition);

    @DS("clickhouse")
    Integer insertWeatherData(@Param("weatherName") String weatherName , @Param("temp") Double temp , @Param("humidity") Double humidity , @Param("precip") Double precip);

    WeatherDataVo findNowWeather();
}
