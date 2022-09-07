package com.rpzh.dao;

import com.rpzh.vo.AirConditionVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Author: zyq
 * @Date: 2022/06/01/10:34
 * @Description: 功能
 */
@Mapper
public interface AirConditioningDao {
    List<String> findAirConditioningData(@Param("wareAreaId")Integer wareAreaId);

    Integer insertAirCondtionTags(@Param("maps") List<Map<String,String>> integerStringMap);

    List<AirConditionVo> findAllAirCondition();
}
