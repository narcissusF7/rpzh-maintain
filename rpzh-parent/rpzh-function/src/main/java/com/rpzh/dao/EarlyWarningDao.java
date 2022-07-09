package com.rpzh.dao;

import com.rpzh.dto.EarlyWarningDto;
import com.rpzh.pojo.EarlyWarning;
import com.rpzh.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface EarlyWarningDao {
    List<WarningCountVo> findEarlyWarningCount(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<EarlyWarningVo> findEarlyWarningByNode(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    EarlyWarningVo findUndoEarlyWarningByNodeId(@Param("nodeId") String nodeId, @Param("warningType") String warningType);

    List<String> findWarningThreshold(@Param("settingKey") String settingKey);

    Integer insertWarning(@Param("earlyWarning") EarlyWarning earlyWarning);

    Integer updateWarning(@Param("earlyWarning") EarlyWarning earlyWarning);

    Integer findUndoCount(@Param("nodeId") String nodeId, @Param("warningType") String warningType);

    List<EarlyWarningVo> findTroubleNode();

    List<EarlyWarningVo> findHistoryWarningInfo(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<EarlyWarningVo> findHistoryWarningData(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    Integer findSum(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<EarlyWarningVo> findEarlyWarningByStackType(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<EarlyWarningVo> findEarlyWarningByAreaType(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<EarlyWarningVo> findWarningDataByNode(@Param("affirm") String affirm, @Param("stackId") Integer stackId, @Param("areaId") Integer areaId, @Param("type") List<String> type);

    List<EarlyWarningVo> findWarningDataByStack(@Param("affirm") String affirm, @Param("stackId") Integer stackId, @Param("warningType") String warningType);

    List<EarlyWarningVo> findWarningDataByArea(@Param("affirm") String affirm, @Param("areaId") Integer areaId, @Param("warningType") String warningType);

    EarlyWarningVo findById(@Param("id") Integer id);

    Integer misreport(@Param("id") Integer id);

    List<EarlyWarningVo> findSendStack(@Param("stackId") Integer stackId);

    List<Integer> findStack();

    List<Integer> findArea();

    List<EarlyWarningVo> findSendArea(@Param("areaId") Integer areaId);

    List<EarlyWarningVo> findSendStackByWarningThree(@Param("stackId") Integer stackId);

    List<EarlyWarningVo> findSendAreaByWarningThree(@Param("areaId") Integer areaId);

    String findMaxTime(@Param("nodeId") String nodeId);

    Integer updateWarningType(@Param("nodeId") String nodeId, @Param("warningType") String warningType);

    List<Map> findWarningCountByDay(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto,@Param("dateType")String dateType);

    List<Map> findWarningTypeCount(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<Map> findIsDeal(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto, @Param("count") Integer count);

    Integer findNoDealCountByDay(@Param("date")String date,@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto,@Param("dateType")String dateType);
    Integer findDealCountByDay(@Param("date")String date,@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto,@Param("dateType")String dateType);
}
