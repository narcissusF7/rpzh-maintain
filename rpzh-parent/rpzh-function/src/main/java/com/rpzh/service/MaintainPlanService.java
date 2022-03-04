package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import dto.MaintainPlanDto;
import vo.MaintainPlanVo;

import java.lang.reflect.InvocationTargetException;

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
    int insert(MaintainPlanDto maintainPlanDto) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

    /**
     * 查询养护计划
     * @param maintainPlanDto 养护计划信息
     * @return success or failure
     */
    PageInfo<MaintainPlanVo> findMaintainPlanInfo(MaintainPlanDto maintainPlanDto);

    /**
     * 删除养护计划
     * @param id 养护计划id
     * @return success or failure
     */
    Integer deleteMaintainPlanInfo(Integer id);

}
