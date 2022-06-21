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

    EarlyWarningVo findUndoStackEarlyWarning(@Param("stackId") Integer stackId);

    List<String> findWarningThreshold(@Param("settingKey") String settingKey);

    List<EarlyWarningVo> findUndoEarlyWarning();

    Integer insertWarning(@Param("earlyWarning") EarlyWarning earlyWarning);

    Integer updateWarning(@Param("earlyWarning") EarlyWarning earlyWarning);

    Integer findUndoCount(@Param("nodeId") String nodeId, @Param("warningType") String warningType);

    List<EarlyWarningVo> findTroubleNode();

    List<EarlyWarningVo> findHistoryWarningInfo(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<EarlyWarningVo> findHistoryWarningData(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<WarningOrDealVo> findCountByMonth(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    Integer findSum(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<WarningOrDealVo> findCountByWarningType(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<WarningOrDealVo> findCountByDeal(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<WarningOrDealVo> findCountByDay(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<WarningOrDealVo> findCountByIsDeal(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<EarlyWarningVo> findEarlyWarningByStackType(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<EarlyWarningVo> findEarlyWarningByAreaType(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<EarlyWarningVo> findWarningDataByNode(@Param("affirm") String affirm, @Param("stackId") Integer stackId, @Param("areaId") Integer areaId, @Param("type") List<String> type);

    List<EarlyWarningVo> findWarningDataByStack(@Param("affirm") String affirm, @Param("stackId") Integer stackId, @Param("warningType") String warningType);

    List<EarlyWarningVo> findWarningDataByArea(@Param("affirm") String affirm, @Param("areaId") Integer areaId, @Param("warningType") String warningType);

    EarlyWarningVo findById(@Param("id") Integer id);

    Integer misreport(@Param("id") Integer id);

    TimeVo findTime();

    int insertTime(@Param("firstTime") String firstTime, @Param("secondTime") String secondTime);

    int updateTime(@Param("firstTime") String firstTime, @Param("secondTime") String secondTime);

    List<EarlyWarningVo> findSendStack(@Param("stackId") Integer stackId);

    List<Integer> findStack();

    List<Integer> findArea();

    List<EarlyWarningVo> findSendArea(@Param("areaId") Integer areaId);

    List<EarlyWarningVo> findSendStackByWarningThree(@Param("stackId") Integer stackId);

    List<EarlyWarningVo> findSendAreaByWarningThree(@Param("areaId") Integer areaId);

    String findMaxTime(@Param("nodeId")String nodeId);
}
