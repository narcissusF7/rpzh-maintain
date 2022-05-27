package com.rpzh.dao;

import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.dto.SettingColorAndDayDto;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.SettingColorAndDayVo;
import org.apache.ibatis.annotations.Mapper;
import com.rpzh.pojo.AlarmValueSetting;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Mapper
public interface AlarmValueSettingDao {

    AlarmValueSettingVo findAlarmValueSetting(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    AlarmValueSettingVo findAlarmValueSettingById(@Param("alarmValueSettingId") Integer alarmValueSettingId);

    Integer updateAlarmValueSetting(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto, @Param("alarmValueSettingId") Integer alarmValueSettingId);

    Integer insertAlarmValueSetting(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    Integer findMaxId();

    SettingColorAndDayVo findColorAndAlertUpgradeDay();

    int insertColorAndAlertUpgradeDay(@Param("settingKey") String settingKey, @Param("settingValue") String settingValue, @Param("settingName") String settingName);

    Integer updateStatus(@Param("alarmValueSettingId") Integer alarmValueSettingId);

    Integer delete(@Param("alarmValueSettingId") Integer alarmValueSettingId);

    int deleteIds(@Param("alarmValueSettingIds") List<Integer> alarmValueSettingIds);

    List<AlarmValueSettingVo> findAlarmValueSettingTable(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    Integer isExistEarlyWarningRuleName(@Param("earlyWarningRuleName") String earlyWarningRuleName);

    String findEarlyWarningRuleName(@Param("id")Integer id);

    Integer updateColorAndAlertUpgradeDay(@Param("settingValue")Object settingValue,@Param("settingKey")String settingKey,@Param("settingName")String settingName);

//    List<AlarmValueSetting> findByMaintainStageId(Set<Integer> set);
    AlarmValueSettingVo findByMaintainStageId(@Param("id") Integer maintainStageId);
}
