package com.rpzh.dao;

import com.rpzh.vo.AlarmValueSettingVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WareAreaAlarmSettingDao {
    AlarmValueSettingVo findBySite(@Param("site") String site);
}
