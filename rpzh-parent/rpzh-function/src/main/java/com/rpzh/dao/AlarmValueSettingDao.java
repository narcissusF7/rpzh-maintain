package com.rpzh.dao;

import org.apache.ibatis.annotations.Mapper;
import pojo.AlarmValueSetting;

@Mapper
public interface AlarmValueSettingDao {
     int deleteByPrimaryKey(Integer id);

     int insert(AlarmValueSetting record);

     int insertSelective(AlarmValueSetting record);

     AlarmValueSetting selectByPrimaryKey(Integer id);

     int updateByPrimaryKeySelective(AlarmValueSetting record);

     int updateByPrimaryKey(AlarmValueSetting record);
}
