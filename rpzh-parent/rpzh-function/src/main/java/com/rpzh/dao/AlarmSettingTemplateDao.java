package com.rpzh.dao;

import com.rpzh.vo.AlarmValueSettingVo;
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
    AlarmValueSettingVo findAlarmSettingTemplate(@Param("earlyWarningRuleName")String earlyWarningRuleName);
}
