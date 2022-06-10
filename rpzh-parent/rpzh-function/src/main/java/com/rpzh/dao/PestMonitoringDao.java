package com.rpzh.dao;

import com.rpzh.dto.PestBoardDto;
import com.rpzh.dto.PestMonitoringRuleDto;
import com.rpzh.pojo.PestData;
import com.rpzh.vo.PestBoardVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PestMonitoringDao {

    Integer addPestBoard(@Param("pestBoardDto") PestBoardDto pestBoardDto);

    Integer deletePestBoard(Integer[] ids);

    Integer updatePestMonitor(@Param("pestMonitoringRuleDto") PestMonitoringRuleDto pestMonitoringRuleDto);

    Integer updateWeekPatrol(@Param("map") Map<Integer, Integer> map);

    List<String> findSpecialSiteRoute(@Param("siteRoutes") List<String> asList);

    Integer updateSpecialSiteRoute(@Param("siteRoute") String siteRoute, @Param("valueOne")String valueOne,@Param("valueTwo")String valueTwo,@Param("valueThree")String valueThree,@Param("siteName") String siteName);

    Integer addSpecialSiteRoute(@Param("siteRoute") String siteRoute, @Param("valueOne")String valueOne,@Param("valueTwo")String valueTwo,@Param("valueThree")String valueThree,@Param("siteName") String siteName);

    Map<String,Integer> findWeekPatrol();

    Map<String, String> findPestMonitorValue();

    List<Map<String, String>> findSpecialSite();

    Integer delSpecialSite(@Param("ids") List<String> asList);

    Integer exportPestData(@Param("pestDataList") List<PestData> pestDataList,@Param("siteType") String siteType);

    List<Map<String ,String>> findByPrincipalName(@Param("principalName") String principalName);

    PestBoardVo findPestBoardByPestBoardId(@Param("pestBoardId") String pestBoardId);
}
