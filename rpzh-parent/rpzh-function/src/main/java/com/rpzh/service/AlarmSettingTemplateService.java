package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.AlarmSettingTemplateDto;
import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.dto.LogAlarmSettingDto;
import com.rpzh.dto.SettingColorAndDayDto;
import com.rpzh.entity.Result;
import com.rpzh.vo.AlarmSettingTemplateVo;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.SettingColorAndDayVo;

import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/06/07/10:19
 * @Description: 功能
 */
public interface AlarmSettingTemplateService {

    PageInfo<AlarmValueSettingVo> findAlarmSettingTemplateTable(AlarmValueSettingDto alarmValueSettingDto);

    Result deleteAlarmSettingTemplate(List<String> materialCodes);

    void insertAlarmSettingTemplate(AlarmSettingTemplateDto alarmSettingTemplateDto);

    SettingColorAndDayVo findColorAndAlertUpgradeDay();

    Result updateOrInsertColorAndAlertUpgradeDay(SettingColorAndDayDto settingColorAndDayDto);

    AlarmSettingTemplateVo findAlarmSettingTemplateByMaterialCode(String materialCode,String maintenceStyle);

    void updateAlarmSettingTemplate(AlarmSettingTemplateDto alarmSettingTemplateDto);

    Result findLogData(LogAlarmSettingDto logAlarmSettingDto);
}
