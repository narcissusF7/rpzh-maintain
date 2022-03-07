package com.rpzh.controller;

import com.github.pagehelper.PageInfo;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.MaintainPlanService;
import dto.MaintainPlanDto;
import org.springframework.web.bind.annotation.*;
import vo.MaintainPlanVo;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;

/**
 * @author wbl
 * @description [养护计划控制层]
 */
@RestController
@RequestMapping("/maintainPlan")
public class MaintainPlanController {

    /**
     * 养护服务层
     */
    @Resource
    private MaintainPlanService maintainPlanService;


    /**
     * 插入一条养护计划
     *
     * @param maintainPlanDto 前端传入添加信息
     * @return success or failure
     * @createUser wbl
     */
    @PostMapping(value = "/insert")
    public Result<Integer> insertMaintainPlan(MaintainPlanDto maintainPlanDto) throws
            InvocationTargetException,
            NoSuchMethodException,
            InstantiationException,
            IllegalAccessException {
        // 烟叶代码是否为null
        if (ToolUtil.isEmpty(maintainPlanDto.getTobaccoCode())) {
            return new Result<>(false, StatusCode.ERROR, "烟叶代码没有填写!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getTobaccoName())) {
            return new Result<>(false, StatusCode.ERROR, "烟叶名称没有填写!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getYearOfTobacco())) {
            return new Result<>(false, StatusCode.ERROR, "烟叶年份没有填写!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getStorageWarehouse())) {
            return new Result<>(false, StatusCode.ERROR, "仓库位置没有填写!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getMaintainStage())) {
            return new Result<>(false, StatusCode.ERROR, "养护阶段没有选择!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getMaintainNumberOfDays())) {
            return new Result<>(false, StatusCode.ERROR, "养护天数没有填写!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getMaintainDates())) {
            return new Result<>(false, StatusCode.ERROR, "调样日期没有填写!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getMaintainsAmount())) {
            return new Result<>(false, StatusCode.ERROR, "养护数量没有填写!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getPrincipal())) {
            return new Result<>(false, StatusCode.ERROR, "负责人没有填写!");
        }

        int result = maintainPlanService.insert(maintainPlanDto);
        return new Result<>(true, StatusCode.OK, "养护计划添加成功", result);
    }

    /**
     * 分页查询养护计划
     *
     * @param maintainPlanDto 前端传入添加信息
     * @return success or failure
     * @createUser wbl
     */
    @GetMapping(value = "/findMaintainPlanInfo")
    public Result<MaintainPlanVo> findMaintainPlanInfo(MaintainPlanDto maintainPlanDto) {
        PageInfo<MaintainPlanVo> maintainPlanInfo = maintainPlanService.findMaintainPlanInfo(maintainPlanDto);
        return new Result<>(true, StatusCode.OK, "查询成功!", maintainPlanInfo);
    }

    /**
     * 删除养护计划
     *
     * @param id 养护计划id
     * @return success or failure
     * @createUser wbl
     */
    @PostMapping("/deleteMaintainPlanInfo")
    public Result<Integer> deleteMaintainPlanInfo(@RequestParam("id") Integer id) {
        Integer result = maintainPlanService.deleteMaintainPlanInfo(id);
        return new Result<>(true, StatusCode.OK, "删除成功!", result);
    }


    @PostMapping("/alterNodeLocation")
    public Result<Integer> alterNodeLocation() {
        return null;
    }

}
