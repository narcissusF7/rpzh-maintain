package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.PestBoardDto;
import com.rpzh.dto.PestDataDto;
import com.rpzh.dto.PestMonitoringRuleDto;
import com.rpzh.entity.Result;
import com.rpzh.util.MonitorCondition;
import com.rpzh.vo.PestBoardVo;
import com.rpzh.vo.PestDataVo;
import com.rpzh.vo.PestMonitoringRuleVo;

import java.util.List;
import java.util.Map;

/**
 * 虫情监控服务接口
 * @Author liujj
 * @create 2022-06-10 11:31
 * @Description: good good study,day day up
 */
public interface PestMonitoringService {

    Result addPestBoard(PestBoardDto pestBoardDto);

    Integer deletePestBoard(Integer[] ids);

    Integer updatePestRule(PestMonitoringRuleDto pestMonitoringRuleDto);

    PestMonitoringRuleVo findPestRule();

    Result exportPestData(List<Map<Integer, String>> pestData,String siteType);

    Result fullExport();

    List<PestBoardVo> findPestBoard(PestBoardDto pestBoardDto);

    PageInfo<PestDataVo> findPestDataHistory(MonitorCondition monitorCondition);

    List<PestDataVo> findPestDataHistoryByOne(PestDataDto pestDataDto);

    Integer addPestHistory(List<Map> maps);

    Result findPestBoardByRfid(String rfid);

    Map<String, Object> findPestWarningValue(String pestBoardId);

    int findCountByPestBoradId(String pestBoardId);
}
