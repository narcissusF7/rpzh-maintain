package com.rpzh.dao;

import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.SettingColorAndDayVo;
import org.apache.ibatis.annotations.Mapper;
import com.rpzh.pojo.AlarmValueSetting;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlarmValueSettingDao {

    AlarmValueSettingVo findAlarmValueSetting(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    AlarmValueSettingVo findAlarmValueSettingById(@Param("alarmValueSettingId") Integer alarmValueSettingId);

    Integer updateAlarmValueSetting(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    Integer insertAlarmValueSetting(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    Integer findMaxId();

    int deleteIds(@Param("alarmValueSettingIds") List<Integer> alarmValueSettingIds);

    Integer isExistEarlyWarningRuleName(@Param("earlyWarningRuleName") String earlyWarningRuleName);

    String findEarlyWarningRuleName(@Param("id")Integer id);

    AlarmValueSettingVo findByMaintainStageId(@Param("id") Integer maintainStageId);

    AlarmValueSettingVo findByMaterialCode(@Param("mmaterialCode") String mmaterialCode);
}
