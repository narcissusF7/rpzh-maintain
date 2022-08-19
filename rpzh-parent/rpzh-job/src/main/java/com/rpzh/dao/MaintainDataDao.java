package com.rpzh.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Author: zyq
 * @Date: 2022/07/28/14:31
 * @Description: 预警、垛位、仓间、养护计划等信息
 */
@Mapper
public interface MaintainDataDao {

    //获取所有未处理的位置路由
    List<String> findBySiteRoute();

    //根据位置路由查询未处理的个数
    Integer findCountEarly(@Param("siteRoute") String siteRoute);

    //根据位置路由和预警等级查询预警个数
    Integer findCountWarningGrade(@Param("siteRoute") String siteRoute, @Param("warningGrade") String warningGrade);

    //根据预警类型等查询预警信息的设备id
    List<String> findEarlyData(@Param("siteRoute") String siteRoute, @Param("warningGrade") String warningGrade, @Param("warningType") String warningType);

    String findVal(@Param("siteRoute") String siteRoute, @Param("warningGrade") String warningGrade, @Param("warningType") String warningType);
    //查询垛位名称、负责人账号
    Map<String, String> findStackById(@Param("stackId") Integer stackId);

    //查询仓间名称、负责人账号、层id
    Map<String, String> findWareAreaPrincipalName(@Param("areaId") String areaId);

    //查询层负责人账号、楼栋id
    Map<String, String> findFloorPrincipalName(@Param("floorId") String floorId);

    //查询楼栋负责人账号、仓库id
    Map<String, String> findBuildingPrincipalName(@Param("buildingId") String buildingId);

    //查询仓库负责人账号
    String findWarehousePrincipalName(@Param("warehouseId")String warehouseId);
}
