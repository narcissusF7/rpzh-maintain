package com.rpzh.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 养护计划全链路统计（只读聚合，不改动任何业务表）。
 *
 * 关联口径：
 *  - 阶段转换：tb_maintain_plan_stage 每阶段一行
 *  - 预警：tb_early_warning.stack_id = 计划垛位；仓间级预警(05/07/09/11/14)再按 area_id 纳入
 *  - 虫害：tb_pest_data_history 通过诱捕板 site_id 或 site_route 末段的仓间 id 关联
 *  - 时间窗口：养护起止由阶段表推算，SQL 只接收格式化后的字符串边界
 */
@Mapper
public interface MaintainPlanStatisticsDao {

    /** 计划基础信息 + 垛位 + 所属仓间 */
    Map<String, Object> findPlanBase(@Param("id") Integer id);

    /** 阶段历史（按 id 升序，即转换顺序） */
    List<Map<String, Object>> findStages(@Param("id") Integer id);

    /** 窗口内该垛位（含仓间级）的预警明细 */
    List<Map<String, Object>> findWarnings(@Param("stackId") Integer stackId,
                                          @Param("areaId") Integer areaId,
                                          @Param("beginTime") String beginTime,
                                          @Param("endTime") String endTime);

    /** 窗口内该仓间的虫害采集明细 */
    List<Map<String, Object>> findPests(@Param("areaId") Integer areaId,
                                       @Param("beginTime") String beginTime,
                                       @Param("endTime") String endTime);

    /** 计划级养护结果与累计预警时长（一个计划可能有多行，取最新一条） */
    Map<String, Object> findEarlyTime(@Param("id") Integer id);
}
