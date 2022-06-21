package com.rpzh.dao;

import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.dto.LogAlarmSettingDto;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.LogAlarmSettingVo;
import com.rpzh.vo.SettingColorAndDayVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/05/30/10:43
 * @Description: 功能
 */
@Mapper
public interface AlarmSettingTemplateDao {
    //根据规则名称查询预警设置值
    AlarmValueSettingVo findAlarmSettingTemplate(@Param("earlyWarningRuleName") String earlyWarningRuleName);

    List<AlarmValueSettingVo> findAlarmSettingTemplateTable(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    Integer delete(@Param("ids") List<Integer> ids);

    List<Integer> findMaterialCodes(@Param("materialCodes") List<String> materialCodes);

    int insertAlarmSettingTemplate(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    SettingColorAndDayVo findColorAndAlertUpgradeDay();

    Integer updateColorAndAlertUpgradeDay(@Param("settingValue") Object settingValue, @Param("settingKey") String settingKey, @Param("settingName") String settingName);

    int insertColorAndAlertUpgradeDay(@Param("settingKey") String settingKey, @Param("settingValue") String settingValue, @Param("settingName") String settingName);

    Integer findCount();

    List<AlarmValueSettingVo> findAlarmSettingTemplateByMaterialCode(@Param("materialCode") String materialCode, @Param("maintenceStyle") String maintenceStyle);

    AlarmValueSettingVo findNameByMaintenceStyle(@Param("materialCode") String materialCode, @Param("maintenceStyle") String maintenceStyle);

    int updateAlarmSettingTemplate(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    AlarmValueSettingVo findAlarmSettingTemplateById(@Param("alarmValueSettingId") Integer id, @Param("materialCode") String materialCode, @Param("earlyWarningRuleName") String earlyWarningRuleName);

    int insertLogAlarmSetting(@Param("logAlarmSettingDto") LogAlarmSettingDto logAlarmSettingDto);

    List<LogAlarmSettingVo> findLogData(@Param("logAlarmSettingDto")LogAlarmSettingDto logAlarmSettingDto);
}
