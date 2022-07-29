package com.rpzh.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Author: zyq
 * @Date: 2022/07/28/14:31
 * @Description: 功能
 */
@Mapper
public interface MaintainDataDao {
    List<String> findBySiteRoute();

    Integer findCountEarly(@Param("siteRoute") String siteRoute);

    Integer findCountWarningGrade(@Param("siteRoute") String siteRoute, @Param("warningGrade") String warningGrade);

    List<String> findEarlyData(@Param("siteRoute") String siteRoute, @Param("warningGrade") String warningGrade, @Param("warningType") String warningType);

    Map<String, String> findStackById(@Param("stackId") Integer stackId);

    Map<String, String> findWareAreaPrincipalName(@Param("areaId") String areaId);

    Map<String, String> findFloorPrincipalName(@Param("floorId") String floorId);

    Map<String, String> findBuildingPrincipalName(@Param("buildingId") String buildingId);

    String findWarehousePrincipalName(@Param("warehouseId")String warehouseId);
}
