package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.dto.SettingColorAndDayDto;
import com.rpzh.entity.Result;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.SettingColorAndDayVo;

import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/05/07/14:59
 * @Description: 功能
 */
public interface AlarmValueSettingService {
    AlarmValueSettingVo findMaintainPlanInfo(AlarmValueSettingDto alarmValueSettingDto);
    PageInfo<AlarmValueSettingVo> findAlarmValueSettingTable(AlarmValueSettingDto alarmValueSettingDto);
    Result changeAlarmValueSettingStatus(Integer id);
    Result deleteAlarmValueSetting(List<Integer> ids);

    List<String> findYear(String materialCode);

    void insertAlarmValueSetting(AlarmValueSettingDto alarmValueSettingDto);

    Integer isExistEarlyWarningRuleName(String earlyWarningRuleName);
    AlarmValueSettingVo findAlarmValueSettingById(Integer id);

    Integer updateAlarmValueSetting(AlarmValueSettingDto alarmValueSettingDto);

    String findEarlyWarningRuleName(Integer id);

    SettingColorAndDayVo findColorAndAlertUpgradeDay();

    Integer updateColorAndAlertUpgradeDay(SettingColorAndDayDto settingColorAndDayDto);
}
