package com.rpzh.dao;

import com.rpzh.dto.LogAlarmSettingDto;
import com.rpzh.dto.WareAreaAlarmSettingDto;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.EarlyWarningVo;
import com.rpzh.vo.LogAlarmSettingVo;
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

    WareAreaAlarmSettingVo findSiteIdAndSiteType(@Param("id")Integer id);

    int updateWareAreaAlarmSetting(@Param("wareAreaAlarmSettingDto") WareAreaAlarmSettingDto wareAreaAlarmSettingDto);

    List<LogAlarmSettingVo> findLogData(@Param("logAlarmSettingDto") LogAlarmSettingDto logAlarmSettingDto);

    Integer findCount(@Param("pidRoute") String pidRoute);

    List<Integer> findWarehouseIds();

    Integer findCountByWarehouseId();

    EarlyWarningVo findAlarmValueSettingByAreaId(@Param("column") String column, @Param("site") String site);

    WareAreaAlarmSettingVo findAlarmValueSettingBySiteRoute(@Param("siteRoute") String siteRoute, @Param("siteRoute1") String siteRoute1, @Param("siteRoute2") String siteRoute2, @Param("siteRoute3") String siteRoute3);
}
