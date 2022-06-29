package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import com.rpzh.entity.Result;
import com.rpzh.pojo.NodeData;
import com.rpzh.util.MonitorCondition;
import com.rpzh.vo.EarlyWarningVo;
import com.rpzh.vo.NodeDataVo;
import com.rpzh.vo.NodeVo;
import com.rpzh.vo.WeatherDataVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface MonitoringCenterService {
    PageInfo<NodeDataVo> findMonitoringData(MonitorCondition monitorCondition);

    List<EarlyWarningVo> findTroubleNode();

    Map<String, String> findWarningThreshold();

    Map<String,String> findAlarmValue(NodeData nodeData);

    List<WeatherDataVo> findWeatherData(MonitorCondition monitorCondition);

    List<HashMap<String, String>>findWeatherDataByDay(MonitorCondition monitorCondition);

    WeatherDataVo findNowWeather();

    Map putCacheWarningColor();
}
