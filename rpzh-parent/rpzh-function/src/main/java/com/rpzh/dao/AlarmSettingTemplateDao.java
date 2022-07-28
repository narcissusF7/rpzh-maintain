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
    AlarmValueSettingVo findAlarmSettingTemplate(@Param("earlyWarningRuleName") String earlyWarningRuleName, @Param("phases") String phases);

    //根据条件查询预警设置列表
    List<AlarmValueSettingVo> findAlarmSettingTemplateTable(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    //删除预警模板设置
    Integer delete(@Param("ids") List<Integer> ids);

    //创建预警规则
    int insertAlarmSettingTemplate(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    //查询升级规则
    SettingColorAndDayVo findColorAndAlertUpgradeDay();

    //添加升级规则
    Integer updateColorAndAlertUpgradeDay(@Param("settingValue") Object settingValue, @Param("settingKey") String settingKey, @Param("settingName") String settingName);

    //修改升级规则
    int insertColorAndAlertUpgradeDay(@Param("settingKey") String settingKey, @Param("settingValue") String settingValue, @Param("settingName") String settingName);

    //判断是否存在升级规则
    Integer findCount();

    ///根据物料代码和养护方式查询预警规则的基本信息
    AlarmValueSettingVo findNameByMaintenceStyle(@Param("materialCode") String materialCode, @Param("earlyWarningRuleName") String earlyWarningRuleName);

    //更新预警规则
    int updateAlarmSettingTemplate(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    //根据预警规则id查询预警信息
    AlarmValueSettingVo findAlarmSettingTemplateById(@Param("alarmValueSettingId") Integer id);

    //新增预警规则
    int insertLogAlarmSetting(@Param("logAlarmSettingDto") LogAlarmSettingDto logAlarmSettingDto);

    //查询操作日志
    List<LogAlarmSettingVo> findLogData(@Param("logAlarmSettingDto") LogAlarmSettingDto logAlarmSettingDto);

    //查询所有的预警规则名称
    List<String> findAlarmSettingTemplateList();

    AlarmValueSettingVo findAlarmSettingTemplateByMaterialCodeAndPhases(@Param("materialCode") String materialCode, @Param("earlyWarningRuleName") String earlyWarningRuleName, @Param("phases") String phases);

    Integer findSum(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    int deleteStage(@Param("ids") List<Integer> ids);

    int insertAlarmSettingTemplateStage(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    int updateAlarmSettingTemplateStage(@Param("alarmValueSettingDto") AlarmValueSettingDto alarmValueSettingDto);

    Integer findCountByName(@Param("earlyWarningRuleName")String earlyWarningRuleName);

    Integer findCountByNameAndPhases(@Param("earlyWarningRuleName")String earlyWarningRuleName,@Param("phases") String phases);
}
