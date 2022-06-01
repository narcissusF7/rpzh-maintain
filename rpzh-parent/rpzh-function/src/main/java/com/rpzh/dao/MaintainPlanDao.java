package com.rpzh.dao;

import com.rpzh.dto.MaintainPlanDto;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.rpzh.pojo.MaintainPlan;
import com.rpzh.vo.MaintainPlanVo;

import java.util.Date;
import java.util.List;

@Mapper
public interface MaintainPlanDao {

    int deleteMaintainPlanById(@Param("ids") List<Integer> ids);

    int deleteMaintainPlanStageById(@Param("ids") List<Integer> ids);

    List<MaintainPlanVo> findMaintainPlanInfo(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    MaintainPlanVo findMaintainPlan(@Param("id") Integer id);

    Integer updateMaintainPlan(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    Integer updateMaintainPlanStage(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    Integer insertMaintainPlan(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    Integer insertMaintainStage(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    Integer findMaxStageId(@Param("id") Integer id);

    Integer findMaxPlanId(@Param("stackId") Integer stackId);

    int updateMaintainPlanStageId(@Param("stageId") Integer stageId, @Param("id") Integer id);

    List<MaintainPlanVo> findMaintainCountReport(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    List<MaintainPlanVo> findMaintainMonthEndReport(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    List<MaintainPlanVo> findMaintainHistoryCollect(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    Integer endMaintainPlan(@Param("updateUser") String updateUser, @Param("updatedTime") Date updateTime, @Param("maintainPlanId") Integer maintainPlanId);

    List<Integer> findAlarmValueSettingId(@Param("ids") List<Integer> ids);

    MaintainPlanVo findVersion(@Param("stageId") Integer stageId);

    Integer findStageId(@Param("id") Integer id, @Param("version") Integer version);

    Integer updateChange(@Param("id") Integer id, @Param("change") String change);

    Integer updateStageId(@Param("id") Integer id, @Param("stageId") Integer stageId);

    List<MaintainPlanVo> findDetails(@Param("id") Integer id);

    MaintainPlanVo findDetailsNew(@Param("id") Integer id,@Param("stackId")Integer stackId);

    int changeMaintainPlanStage(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    List<MaintainPlanVo> findExpireCollectTable(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    Integer isExistStackId(@Param("stackId") Integer stackId);

    Integer isExistAlarmValueSetting(@Param("id") Integer id);

    Integer updateStackByStackId(@Param("id") Integer id, @Param("stackId") Integer stackId);

    Integer findStackCountByStackId(Integer stackId);

    MaintainPlanVo findMaintainStackId(@Param("stackId") Integer stackId);

    List<Integer> findId(@Param("ids") List<Integer> ids);
}
