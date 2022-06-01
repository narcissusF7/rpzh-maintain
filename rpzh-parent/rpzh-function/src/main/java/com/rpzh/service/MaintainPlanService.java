package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import com.rpzh.entity.Result;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.MaintainPlanVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wbl
 * @description [养护服务层]
 */
public interface MaintainPlanService {
    /**
     * 新增养护计划
     * @param maintainPlanDto 养护计划信息
     * @return success or failure
     */
    int insertMaintainPlan(MaintainPlanDto maintainPlanDto);

    AlarmValueSettingVo findAlarmSettingTemplate(String earlyWarningRuleName);

    Integer isExistStackId(Integer stackId);
    /**
     * 查询养护计划
     * @param maintainPlanDto 养护计划信息
     * @return success or failure
     */
    PageInfo<MaintainPlanVo> findMaintainPlanInfo(MaintainPlanDto maintainPlanDto);

    /**
     * 删除养护计划
     * @param stageIds 养护计划id
     * @return success or failure
     */
    Result deleteMaintainPlanInfo(List<Integer> stageIds);
    /*
    MaintainPlanVo findMaintainPlan(Integer id);

    *//**
     * 更改养护计划
     * @param maintainPlanDto
     * @return
     *//*
    Integer updateMaintainPlan(MaintainPlanDto maintainPlanDto);

    PageInfo<MaintainPlanVo> findMaintainCountReport(MaintainPlanDto maintainPlanDto);

    PageInfo<MaintainPlanVo> findMaintainMonthEndReport(MaintainPlanDto maintainPlanDto);

    PageInfo<MaintainPlanVo> findMaintainHistoryCollect(MaintainPlanDto maintainPlanDto);

    Integer endMaintainPlan(MaintainPlanDto maintainPlanDto);

    Integer deleteMaintainStage(List<Integer> ids);

    MaintainPlanVo findMaintainPlanDetails(Integer id);

    Integer changeMaintainPlanStage(MaintainPlanDto maintainPlanDto);

    Result findExpireCollectTable(MaintainPlanDto maintainPlanDto);

    Integer moveStack(MaintainPlanDto maintainPlanDto);*/
}
