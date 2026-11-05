package com.rpzh.controller;

import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.MaintainPlanService;
import com.rpzh.service.MaterialService;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.MaterialVo;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zyq
 * @description [物料控制层]
 */
@RestController
@RequestMapping("/maintain/material")
@CrossOrigin
@Slf4j
public class MaterialController {
    @Resource
    private MaterialService materialService;

    @Resource
    private MaintainPlanService maintainPlanService;

    /**
     * 通过位置id查找物料信息
     * @param stackId
     * @return
     */
    @PostMapping(value = "/findMaterialInfo")
    @ApiOperation(value = "通过位置id查找物料信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "stackId", value = "垛位Id", required = true, dataType = "Integer")
    })
    public Result findMaterialInfo(@RequestParam("stackId") Integer stackId){
        if(ToolUtil.isEmpty(stackId)){
            return new Result(false, StatusCode.NULLERROR, "垛位id不能为空!");
        }
        //查询该垛位是否新建养护计划
        Integer stackIdCount = maintainPlanService.isExistStackId(stackId);
        if(stackIdCount>0){
            return new Result(false, StatusCode.EXISTERROR, "该垛位已存在养护计划!");
        }
        MaterialVo materialVo = materialService.findMaterialInfo(stackId);
        if(!ToolUtil.isEmpty(materialVo)) {
            return new Result(true, StatusCode.OK, "查询物料信息成功!", materialVo);
        }
        return new Result(false, StatusCode.ERROR, "该垛位暂无物料信息,请重新选择垛位!");
    }

    @PostMapping("/findAlarmValueSetting")
    @ApiOperation(value = "根据物流代码、养护方式、养护阶段查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pidRoute", value = "位置路由", required = true, dataType = "String"),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式", required = true, dataType = "String"),
            @ApiImplicitParam(name = "phases", value = "养护阶段", required = true, dataType = "String"),
            @ApiImplicitParam(name = "materialCodes", value = "物料代码集合", required = true, dataType = "List")
    })
    public Result findAlarmValueSetting(@ApiIgnore AlarmValueSettingDto alarmValueSettingDto){
        if(ToolUtil.isEmpty(alarmValueSettingDto.getMaterialCodes()) || ToolUtil.isEmpty(alarmValueSettingDto)){
            return new Result(false, StatusCode.ERROR, "物料代码不能为空!");
        }
        AlarmValueSettingVo alarmValueSettingVo = maintainPlanService.findAlarmValueSetting(alarmValueSettingDto);
        return new Result(true, StatusCode.OK, "查询预警设置值成功!", alarmValueSettingVo);
    }

    @PostMapping("/findAllMaterialCodes")
    @ApiOperation(value = "查询所有物料代码")
    public Result findAllMaterialCodes() {
        List<String> materialCodes = materialService.findAllMaterialCodes();
        return new Result(true, StatusCode.OK, "查询所有物料代码成功!", materialCodes);
    }
}
