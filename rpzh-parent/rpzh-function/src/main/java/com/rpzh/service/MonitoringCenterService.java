package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import com.rpzh.util.MonitorCondition;
import com.rpzh.vo.NodeDataVo;
import com.rpzh.vo.NodeVo;

import java.util.Map;

public interface MonitoringCenterService {
    PageInfo<NodeDataVo> findMonitoringData(MonitorCondition var1);

    PageInfo<NodeVo> findTroubleNode();

    Integer updateWarningThreshold(Map<String, Integer> var1);

    Map<String, String> findWarningThreshold();
}
