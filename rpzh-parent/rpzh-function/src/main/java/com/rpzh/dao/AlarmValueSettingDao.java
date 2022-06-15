package com.rpzh.dao;

import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.vo.AlarmValueSettingVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlarmValueSettingDao {

    AlarmValueSettingVo findAlarmValueSettingById(@Param("alarmValueSettingId") Integer alarmValueSettingId);

    Integer updateAlarmValueSetting(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    Integer insertAlarmValueSetting(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    Integer findMaxId(@Param("earlyWarningRuleName")String earlyWarningRuleName);

    int deleteIds(@Param("alarmValueSettingIds") List<Integer> alarmValueSettingIds);

    AlarmValueSettingVo findByMaintainStageId(@Param("id") Integer maintainStageId);

    AlarmValueSettingVo findByMaterialCode(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    int changeStatus(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);
}
