package com.rpzh.dao;

import com.rpzh.dto.PestBoardDto;
import com.rpzh.dto.PestDataDto;
import com.rpzh.dto.PestMonitoringRuleDto;
import com.rpzh.entity.Result;
import com.rpzh.pojo.PestData;
import com.rpzh.util.MonitorCondition;
import com.rpzh.vo.EarlyWarningVo;
import com.rpzh.vo.PestBoardVo;
import com.rpzh.vo.PestDataVo;
import com.rpzh.vo.PestMonitoringRuleVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper
public interface PestMonitoringDao {

    Integer addPestBoard(@Param("pestBoardDto") PestBoardDto pestBoardDto);

    Integer deletePestBoard(@Param("ids") List<Integer> ids);

    Integer updatePestMonitor(@Param("pestMonitoringRuleDto") PestMonitoringRuleDto pestMonitoringRuleDto);

    Integer updateWeekPatrol(@Param("map") Map<String, Integer> map);

    List<String> findSpecialSiteRoute(@Param("siteRoutes") List<String> asList);

    Integer insertSpecialSiteRoute(@Param("siteRoute") String siteRoute, @Param("valueOne")String valueOne,@Param("valueTwo")String valueTwo,@Param("valueThree")String valueThree,@Param("siteName") String siteName);

    Integer addSpecialSiteRoute(@Param("siteRoute") String siteRoute, @Param("valueOne")String valueOne,@Param("valueTwo")String valueTwo,@Param("valueThree")String valueThree,@Param("siteName") String siteName);

    Map<String,Integer> findWeekPatrol();

    Map<String, Object> findPestMonitorValue();

    List<Map<String, Object>> findSpecialSite();

    Integer delSpecialSite();

    Integer exportPestData(@Param("pestDataList") List<PestData> pestDataList,@Param("siteType") String siteType);

    List<Map<String ,String>> findByPrincipalName(@Param("principalName") String principalName);

    PestBoardVo findPestBoardByPestBoardId(@Param("pestBoardId") String pestBoardId);

    List<PestBoardVo> findPestBoard(@Param("pestBoardDto") PestBoardDto pestBoardDto);

    List<String> findPestBoardId(@Param("monitorCondition") MonitorCondition monitorCondition);

    List<PestDataVo> findPestDataHistoryByOne(@Param("pestDataDto") PestDataDto pestDataDto);

    Integer addPestHistory(@Param("pestDataDto") PestDataDto pestDataDto);

    Integer findPestBoardCount(@Param("pestBoardId") String pestBoardId);

    Result findPestBoardByRfid(@Param("rfid") String rfid);

    Map<String, String> findPestWarningColor();

    EarlyWarningVo findPestStackMonitor(@Param("pidRoute") String pidRoute);

    EarlyWarningVo findPestMonitor();


}
