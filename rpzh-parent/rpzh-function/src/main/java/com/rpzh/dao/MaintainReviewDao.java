package com.rpzh.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.pojo.NodeData;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.EarlyWarningVo;
import com.rpzh.vo.MaintainPlanVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author: zyq
 * @Date: 2022/07/16/16:46
 * @Description: 功能
 */
@Mapper
public interface MaintainReviewDao {
    List<MaintainPlanVo> findReviewTable(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto, @Param("column") String column);

    List<MaintainPlanVo> findMaintainPlanReview(@Param("id") Integer id);

    MaintainPlanVo findMaintainBaseInfo(@Param("id") Integer id);

    @DS("clickhouse")
    List<Double> findOxygenOrCoreData(@Param("column") String column, @Param("table") String table, @Param("ids") Set<Integer> ids);

    @DS("clickhouse")
    List<Map<String, Double>> findEnvData(@Param("ids") Set<Integer> ids);

    MaintainPlanVo findStackIdAndTime(@Param("id") Integer id);

    List<EarlyWarningVo> findEarlyWarningReview(@Param("maintainPlanVo") MaintainPlanVo maintainPlanVo);

    @DS("clickhouse")
    List<Map> findDataReview(@Param("tableName")String tableName,@Param("columnName") String columnName,@Param("columnOne")String columnOne,@Param("startTime")String startTime,@Param("endTime")String endTime);

    Integer findAreaId(@Param("id")Integer id);
}
