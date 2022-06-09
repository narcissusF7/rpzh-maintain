package com.rpzh.dao;

import com.rpzh.pojo.GeneralSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GeneralSettingDao {

//    Integer insertGeneralSetting(@Param("generalSetting") GeneralSetting generalSetting);

//    Integer deleteGeneralSetting(@Param("generalSetting") GeneralSetting generalSetting);

    Integer updateGeneralSetting(@Param("generalSetting") GeneralSetting generalSetting);

    List<GeneralSetting> findGeneralSetting(@Param("generalSetting") GeneralSetting generalSetting);

}
