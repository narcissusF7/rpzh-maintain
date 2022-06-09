package com.rpzh.dao;

import com.rpzh.dto.PestBoardDto;
import com.rpzh.dto.PestMonitoringRuleDto;
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

    List<Integer> findSpecialStackId(@Param("stackIds") List<Integer> asList);

    Integer updateSpecialStack(@Param("stackId") Integer stackId, @Param("valueOne")String valueOne,@Param("valueTwo")String valueTwo,@Param("valueThree")String valueThree);

    Integer addSpecialStack(@Param("stackId") Integer stackId, @Param("valueOne")String valueOne,@Param("valueTwo")String valueTwo,@Param("valueThree")String valueThree);

    Map<String,Integer> findWeekPatrol();

    Map<String, String> findPestMonitorValue();

    List<Map<String, String>> findSpecialStack();

    Integer delSpecialStack(@Param("ids") List<String> asList);
}
