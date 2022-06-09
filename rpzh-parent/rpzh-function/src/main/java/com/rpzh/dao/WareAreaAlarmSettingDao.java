package com.rpzh.dao;

import com.rpzh.dto.WareAreaAlarmSettingDto;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.WareAreaAlarmSettingVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WareAreaAlarmSettingDao {
    AlarmValueSettingVo findBySite(@Param("site") String site);

    int insertWareAreaAlarmSetting(@Param("wareAreaAlarmSettingDto") WareAreaAlarmSettingDto wareAreaAlarmSettingDto);

    int deleteIds(@Param("ids") List<Integer> ids);

    List<WareAreaAlarmSettingVo> findWareAreaAlarmSettingInfo(@Param("wareAreaAlarmSettingDto") WareAreaAlarmSettingDto wareAreaAlarmSettingDto);

    WareAreaAlarmSettingVo findWareAreaAlarmSettingById(@Param("wareAreaAlarmSettingDto") WareAreaAlarmSettingDto wareAreaAlarmSettingDto);

    int updateWareAreaAlarmSetting(@Param("wareAreaAlarmSettingDto") WareAreaAlarmSettingDto wareAreaAlarmSettingDto);
}
