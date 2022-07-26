package com.rpzh.dao;

import com.rpzh.dto.EarlyWarningDto;
import com.rpzh.pojo.EarlyWarning;
import com.rpzh.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface EarlyWarningDao {
    List<WarningCountVo> findEarlyWarningCount(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<EarlyWarningVo> findEarlyWarningByOne(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    EarlyWarningVo findUndoEarlyWarningByNodeId(@Param("nodeId") String nodeId, @Param("warningType") String warningType);

    List<String> findWarningThreshold(@Param("settingKey") String settingKey);

    Integer insertWarning(@Param("earlyWarning") EarlyWarning earlyWarning);

    Integer updateWarning(@Param("earlyWarning") EarlyWarning earlyWarning);

    Integer findUndoCount(@Param("nodeId") String nodeId, @Param("warningType") String warningType);

    List<EarlyWarningVo> findTroubleNode();

    List<EarlyWarningVo> findHistoryWarningData(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<Integer> findIds(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<EarlyWarningVo> findEarlyWarningByNotOne(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<EarlyWarningVo> findByIds(@Param("ids") List<Integer> ids);

    Integer misreport(@Param("id") Integer id);

    List<EarlyWarningVo> findSendStack(@Param("stackId") Integer stackId);

    List<Integer> findStack();

    List<Integer> findArea();

    List<EarlyWarningVo> findSendArea(@Param("areaId") Integer areaId);

    List<EarlyWarningVo> findSendStackByWarningThree(@Param("stackId") Integer stackId);

    List<EarlyWarningVo> findSendAreaByWarningThree(@Param("areaId") Integer areaId);

    String findMaxTime(@Param("nodeId") String nodeId);

    Integer updateWarningType(@Param("nodeId") String nodeId, @Param("warningType") String warningType);

    List<Map> findWarningCountByDay(@Param("ids") List<Integer> ids, @Param("dateType") String dateType);

    List<Map> findWarningTypeCount(@Param("ids") List<Integer> ids);

    Map findIsDeal(@Param("ids") List<Integer> ids, @Param("count") Integer count);

    Integer findDealCountByDay(@Param("date") String date, @Param("ids") List<Integer> ids, @Param("dateType") String dateType);

    List<WarningCountVo> findEarlyWarningCountByOne(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<WarningCountVo> findEarlyWarningCountByNotOne(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    String findNewTime(@Param("affirm") String affirm, @Param("warningGrade") String warningGrade,
                       @Param("warningType") String warningType, @Param("siteRoute") String siteRoute);

    List<Map> findWarningDataWarningTypeIsOne(@Param("affirm") String affirm, @Param("siteRoute") String siteRoute);

    List<Map> findWarningDataWarningTypeIsNotOne(@Param("affirm") String affirm, @Param("warningType") String warningType, @Param("siteRoute") String siteRoute);

    int updateTackId(@Param("affirm") String affirm, @Param("warningType") String warningType, @Param("siteRoute") String siteRout,
                     @Param("id") Integer id, @Param("principal") String principal,@Param("dealTime") String dealTime);

    Map<String, String> findColor();

    String findPrincipal(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<RepairDataVo> findRepairData(@Param("stackId") Integer stackId, @Param("areaId") Integer areaId);

    int updateRepairId(@Param("affirm") String affirm, @Param("deviceId") String deviceId, @Param("id") String id,
                       @Param("principal") String principal,@Param("dealTime") String dealTime);
}
