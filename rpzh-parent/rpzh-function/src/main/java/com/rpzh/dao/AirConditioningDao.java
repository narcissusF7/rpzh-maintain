package com.rpzh.dao;

import com.rpzh.pojo.NodeData;
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
    List<NodeData> findAirConditioningData(@Param("wareAreaId")Integer wareAreaId);
}
