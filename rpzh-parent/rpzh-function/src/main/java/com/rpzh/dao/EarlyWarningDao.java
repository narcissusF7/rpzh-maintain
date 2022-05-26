package com.rpzh.dao;

import com.rpzh.dto.EarlyWarningDto;
import com.rpzh.pojo.EarlyWarning;
import com.rpzh.vo.EarlyWarningVo;
import com.rpzh.vo.WarningCountVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EarlyWarningDao {
    EarlyWarningVo findUndoEarlyWarningByNodeId(@Param("nodeId") String nodeId);
    EarlyWarningVo findUndoStackEarlyWarning(@Param("stackId") Integer stackId);

    List<String> findWarningThreshold(@Param("settingKey") String settingKey);

    Integer updateWarningThreshold(@Param("settingValue") Integer settingValue, @Param("settingName") String settingName);

    List<EarlyWarningVo> findUndoEarlyWarning();
    List<WarningCountVo> findEarlyWarningCount(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);
    EarlyWarningVo findEarlyWarning(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    EarlyWarningVo findEarlyWarningByStack(@Param("stackId") String stackId);

    Integer insertWarning(@Param("earlyWarning") EarlyWarning earlyWarning);

    Integer updateWarning(@Param("earlyWarning") EarlyWarning earlyWarning);

    Integer findUndoCount(@Param("nodeId") String nodeId, @Param("warningType") String warningType);
}
