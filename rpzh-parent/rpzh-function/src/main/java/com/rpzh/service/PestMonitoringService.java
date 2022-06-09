package com.rpzh.service;

import com.rpzh.dto.PestBoardDto;
import com.rpzh.dto.PestMonitoringRuleDto;
import com.rpzh.vo.PestMonitoringRuleVo;

public interface PestMonitoringService {

    Integer addPestBoard(PestBoardDto pestBoardDto);

    Integer deletePestBoard(Integer[] ids);

    Integer updatePestRule(PestMonitoringRuleDto pestMonitoringRuleDto);

    PestMonitoringRuleVo findPestRule();
}
