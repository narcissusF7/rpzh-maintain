package com.rpzh.service;

import com.rpzh.dto.PestBoardDto;
import com.rpzh.dto.PestMonitoringRuleDto;
import com.rpzh.entity.Result;
import com.rpzh.vo.PestMonitoringRuleVo;

import java.util.List;
import java.util.Map;

public interface PestMonitoringService {

    Integer addPestBoard(PestBoardDto pestBoardDto);

    Integer deletePestBoard(Integer[] ids);

    Integer updatePestRule(PestMonitoringRuleDto pestMonitoringRuleDto);

    PestMonitoringRuleVo findPestRule();

    Result exportPestData(List<Map<Integer, String>> pestData,String siteType);

    Result fullExport();
}
