package com.rpzh.service;

import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.vo.AlarmValueSettingVo;

import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/05/07/14:59
 * @Description: 功能
 */
public interface AlarmValueSettingService {
    AlarmValueSettingVo findMaintainPlanInfo(AlarmValueSettingDto alarmValueSettingDto);
}
