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
    List<Map> findBySiteRoute();

    //查询垛位名称
    String findStackById(@Param("stackId") String stackId);

    //查询仓间名称
    String findWareAreaById(@Param("areaId") String areaId);

    //根据位置路由查询未处理的预警信息
    List<Map> findWarningInfoBySiteRoute(@Param("siteRoute") String siteRoute);
}
