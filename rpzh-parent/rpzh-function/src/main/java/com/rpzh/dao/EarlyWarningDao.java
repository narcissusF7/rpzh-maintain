package com.rpzh.dao;

import com.rpzh.dto.EarlyWarningDto;
import com.rpzh.pojo.EarlyWarning;
import com.rpzh.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface EarlyWarningDao {
    List<EarlyWarningVo> findEarlyWarningByWarningType(@Param("ids")List<Integer> ids,@Param("size")Integer size,@Param("warningType")String warningType);

    EarlyWarningVo findUndoEarlyWarningByNodeId(@Param("nodeId") String nodeId, @Param("warningType") String warningType);

    List<String> findWarningThreshold(@Param("settingKey") String settingKey);

    Integer insertWarning(@Param("earlyWarning") EarlyWarning earlyWarning);

    Integer updateWarning(@Param("earlyWarning") EarlyWarning earlyWarning);

    Integer findUndoCount(@Param("nodeId") String nodeId, @Param("warningType") String warningType);

    List<EarlyWarningVo> findTroubleNode();

    List<EarlyWarningVo> findHistoryWarningData(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<Integer> findIds(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    Map findById(@Param("id")String id);

    Integer misreport(@Param("id") Integer id);

    Integer findTaskId(@Param("id") Integer id);

    Integer updateWarningType(@Param("nodeId") String nodeId, @Param("warningType") String warningType);

    List<Map> findWarningCountByDay(@Param("ids") List<Integer> ids, @Param("dateType") String dateType);

    List<Map> findWarningTypeCount(@Param("ids") List<Integer> ids);

    Map findIsDeal(@Param("ids") List<Integer> ids, @Param("count") Integer count);

    WarningCountVo findEarlyWarningCountByWarningType(@Param("ids")List<Integer> ids,@Param("size")Integer size);

    List<String> findWarningTypes(@Param("ids")List<Integer> ids);

    List<Map> findIdsByWarning(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    List<WarningCountVo> findEarlyWarningCountByNotOne(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);

    Map<String,String> findNewTime(@Param("affirm") String affirm, @Param("warningType") String warningType, @Param("siteRoute") String siteRoute);

    List<Map> findWarningDataWarningType(@Param("affirm") String affirm, @Param("siteRoute") String siteRoute,@Param("warningType") String warningType);

    List<Map> findsiteXYZByNodeIds(@Param("nodeIds")List<String> nodeIds);

    List<Map> findWarningDataWarningTypeIsNotOne(@Param("affirm") String affirm, @Param("warningType") String warningType, @Param("siteRoute") String siteRoute);

    int updateTackId(@Param("affirm") String affirm, @Param("taskId")String taskId, @Param("dealTime") String dealTime, @Param("principal") String principal);

    Map<String, String> findColor();

    List<RepairDataVo> findRepairData(@Param("stackId") Integer stackId, @Param("areaId") Integer areaId);

    int updateRepairId(@Param("earlyWarnings")List<EarlyWarning> earlyWarnings);

    String findMaxId(@Param("deviceId") String deviceId,@Param("warningType")String warningType);

    List<Map> findSiteRoute();

    //根据位置路由查询未处理的预警信息
    List<Map> findWarningInfoBySiteRoute(@Param("ids")List<String> ids,@Param("size")Integer size);

    //获取所有未处理的位置路由
    Map findBySiteRoute(@Param("siteRoute") String siteRoute);

    //查询垛位名称
    String findStackById(@Param("stackId") String stackId);

    //查询仓间名称
    String findWareAreaById(@Param("areaId") String areaId);

    String findDeviceId(@Param("id") String id);

    int findPestWarningCount(@Param("ids") Integer[] ids);

    Map<String,Integer> findAppCount();

    String findAppWarning(@Param("warningType")String warningType);

    Map<String,Object> findAppDeviceFault();

    String findAppCountByWarningType(@Param("warningType")String warningType, @Param("warningGrade") String warningGrade);

    EarlyWarningVo findUndoEarlyWarningBySiteRoute(@Param("siteRoute") String siteRoute, @Param("warningType") String coreTempOverrunStackWarning);

    Integer findUndoStatusByNodeId(@Param("nodeId") String nodeId);
}
