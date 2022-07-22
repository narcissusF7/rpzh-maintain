package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.LogAlarmSettingDto;
import com.rpzh.dto.WareAreaAlarmSettingDto;
import com.rpzh.entity.Result;
import com.rpzh.vo.WareAreaAlarmSettingVo;

import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/06/08/10:50
 * @Description: 功能
 */
public interface WareAreaAlarmSettingService {
    Result insertWareAreaAlarmSetting(WareAreaAlarmSettingDto wareAreaAlarmSettingDto);

    Result deleteWareAreaAlarmSetting(List<Integer> ids);
    PageInfo<WareAreaAlarmSettingVo> findWareAreaAlarmSettingInfo(WareAreaAlarmSettingDto wareAreaAlarmSettingDto);
    Result findWareAreaAlarmSettingById(WareAreaAlarmSettingDto wareAreaAlarmSettingDto);
    Result updateWareAreaAlarmSetting(WareAreaAlarmSettingDto wareAreaAlarmSettingDto);
    Result findLogData(LogAlarmSettingDto logAlarmSettingDto);
}
