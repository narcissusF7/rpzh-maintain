package com.rpzh.dao;

import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.EarlyWarningVo;
import com.rpzh.vo.MaintainPlanVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlarmValueSettingDao {

    AlarmValueSettingVo findAlarmValueSettingById(@Param("alarmValueSettingId") Integer alarmValueSettingId);

    Integer updateAlarmValueSetting(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    Integer insertAlarmValueSetting(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    Integer findIdByEarlyWarningRuleName(@Param("earlyWarningRuleName") String earlyWarningRuleName);

    int deleteIds(@Param("alarmValueSettingIds") List<Integer> alarmValueSettingIds);

    AlarmValueSettingVo findByMaintainStageId(@Param("id") Integer maintainStageId);

    AlarmValueSettingVo findByMaterialCode(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    MaintainPlanVo findByMaintainStackId(@Param("stackId") Integer stackId);

    EarlyWarningVo findAlarmValueSettingByStackId(@Param("column")String column, @Param("stackId") Integer stackId);
}
