package com.rpzh.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/06/01/10:34
 * @Description: 功能
 */
@Mapper
public interface AirConditioningDao {
    List<String> findAirConditioningData(@Param("wareAreaId")Integer wareAreaId);
}
