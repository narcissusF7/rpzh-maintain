package com.rpzh.dao;

import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.vo.AlarmValueSettingVo;
import org.apache.ibatis.annotations.Mapper;
import com.rpzh.pojo.AlarmValueSetting;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlarmValueSettingDao {

    AlarmValueSettingVo findAlarmValueSetting(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    AlarmValueSettingVo findAlarmValueSettingById(@Param("alarmValueSettingId") Integer alarmValueSettingId);

    Integer updateAlarmValueSetting(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto, @Param("alarmValueSettingId") Integer alarmValueSettingId);

    Integer insertAlarmValueSetting(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    Integer findMaxId();

    AlarmValueSettingVo findColorAndAlertUpgradeDay();

    int insertColorAndAlertUpgradeDay(@Param("settingKey") String settingKey, @Param("settingValue") String settingValue, @Param("settingName") String settingName);

    Integer updateStatus(@Param("alarmValueSettingId") Integer alarmValueSettingId);

    Integer delete(@Param("alarmValueSettingId") Integer alarmValueSettingId);

    int deleteIds(@Param("alarmValueSettingIds") List<Integer> alarmValueSettingIds);
}
