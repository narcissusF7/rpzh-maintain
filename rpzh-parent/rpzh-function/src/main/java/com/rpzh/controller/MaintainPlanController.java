package com.rpzh.controller;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.dto.NodeDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.MaintainPlanService;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.util.StringUtils;
import com.rpzh.vo.AlarmValueSettingVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import com.rpzh.vo.MaintainPlanVo;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author wbl
 * @description [养护计划控制层]
 */
@RestController
@RequestMapping("/maintain/maintainPlan")
@CrossOrigin
@Api(value = "养护计划", tags = "养护计划")
public class MaintainPlanController {

    /**
     * 养护服务层
     */
    @Resource
    private MaintainPlanService maintainPlanService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(true);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    @PostMapping("/findAlarmSettingTemplate")
    public Result findAlarmSettingTemplate(@Param("earlyWarningRuleName")String earlyWarningRuleName){
        AlarmValueSettingVo alarmSettingTemplate = maintainPlanService.findAlarmSettingTemplate(earlyWarningRuleName);
        return new Result<>(true, StatusCode.OK, "查询成功",alarmSettingTemplate);
    }
    /**
     * 插入一条养护计划
     *
     * @param maintainPlanDto 前端传入添加信息
     * @return success or failure
     * @createUser wbl
     */
    @PostMapping(value = "/insert")
    @ApiOperation(value = "新建养护计划")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "materialCodes", value = "物料代码集合", required = true, dataType = "String"),
            @ApiImplicitParam(name = "stackId", value = "垛位id", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "stackName", value = "垛位名称", required = true, dataType = "String"),
            @ApiImplicitParam(name = "principalIds", value = "负责人ids集合", required = true, dataType = "List"),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式", required = true, dataType = "String"),
            @ApiImplicitParam(name = "maintainDays", value = "养护天数", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "aftercareDateStart", value = "开始调养日期", required = true, dataType = "String"),
            @ApiImplicitParam(name = "oxygenReductionTime", value = "降氧完成时间", required = true, dataType = "String"),
            @ApiImplicitParam(name = "expireTime", value = "到期提醒时间", required = true, dataType = "String"),
            @ApiImplicitParam(name = "maintainCount", value = "养护数量", required = true, dataType = "Double"),
            @ApiImplicitParam(name = "materialBatchNum", value = "批次号", required = true, dataType = "String"),
            @ApiImplicitParam(name = "phases", value = "养护阶段", required = true, dataType = "String"),
            @ApiImplicitParam(name = "earlyWarningRuleName", value = "预警规则名称", required = true,dataType = "String"),
            @ApiImplicitParam(name = "stackOxygenUpperOne", value = "垛内氧气上限一级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "stackOxygenUpperTwo", value = "垛内氧气上限二级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "stackOxygenUpperThree", value = "垛内氧气上限三级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "stackOxygenLower", value = "垛内氧气下限",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "warehouseOxygenUpperOne", value = "仓间氧气上限一级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "warehouseOxygenUpperTwo", value = "仓间氧气上限二级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "warehouseOxygenUpperThree", value = "仓间氧气上限三级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "warehouseOxygenLower", value = "仓间氧气下限",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "tempCoreChipUpperOne", value = "包芯温度上限一级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "tempCoreChipUpperTwo", value = "包芯温度上限二级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "tempCoreChipUpperThree", value = "包芯温度上限三级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "tempCoreChipLower", value = "包芯温度下限",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "stackTempUpperOne", value = "垛内温度上限一级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "stackTempUpperTwo", value = "垛内温度上限二级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "stackTempUpperThree", value = "垛内温度上限三级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "stackTempLower", value = "垛内温度下限",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "stackMoistureUpperOne", value = "垛内湿度上限一级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "stackMoistureUpperTwo", value = "垛内湿度上限二级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "stackMoistureUpperThree", value = "垛内湿度上限三级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "stackMoistureLower", value = "垛内湿度下限",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "warehouseTempUpper0ne", value = "仓间温度上限一级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "warehouseTempUpperTwo", value = "仓间温度上限二级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "warehouseTempUpperThree", value = "仓间温度上限三级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "warehouseTempLower", value = "仓间温度下限",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "warehouseMoistureUpperOne", value = "仓间湿度上限一级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "warehouseMoistureUpperTwo", value = "仓间湿度上限二级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "warehouseMoistureUpperThree", value = "仓间湿度上限三级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "warehouseMoistureLower", value = "仓间湿度下限",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "coreTempHighEnvTempOne", value = "平均包芯高于环境一级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "coreTempHighEnvTempTwo", value = "平均包芯高于环境二级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "coreTempHighEnvTempThree", value = "平均包芯高于环境三级",required = true,dataType ="Double"),
            @ApiImplicitParam(name = "coreTempLowEnvTemp", value = "平均包芯低于环境三级",required = true,dataType ="Double")
    })
    public Result insertMaintainPlan(MaintainPlanDto maintainPlanDto){
        // 烟叶代码是否为null
        if (ToolUtil.isEmpty(maintainPlanDto.getMaterialCodes())) {
            return new Result(false, StatusCode.ERROR, "烟叶代码不能为空!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getStackName())) {
            return new Result(false, StatusCode.ERROR, "仓库位置不能为空!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getPhases()) || ToolUtil.isEmpty(maintainPlanDto.getMaintenceStyle())) {
            return new Result(false, StatusCode.ERROR, "养护方式或养护阶段没有选择!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getMaintainDays())) {
            return new Result(false, StatusCode.ERROR, "养护天数不能为空!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getAftercareDateStart()) || ToolUtil.isEmpty(maintainPlanDto.getOxygenReductionTime())
                || ToolUtil.isEmpty(maintainPlanDto.getExpireTime())) {
            return new Result(false, StatusCode.ERROR, "调养日期,降氧完成时间,到期提醒时间不能为空!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getMaintainCount())) {
            return new Result(false, StatusCode.ERROR, "养护数量没有填写!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getPrincipalIds())) {
            return new Result(false, StatusCode.ERROR, "负责人没有填写!");
        }
        AlarmValueSettingDto alarmValueSettingDto = new AlarmValueSettingDto(maintainPlanDto);
        String earlyWarningRuleName = alarmValueSettingDto.getEarlyWarningRuleName();
        if (ToolUtil.isEmpty(earlyWarningRuleName)) {
            return new Result(false, StatusCode.ERROR, "预警规则名称不能为空!");
        }
        Integer stackIdCount = maintainPlanService.isExistStackId(maintainPlanDto.getStackId());
        if(stackIdCount>0){
            return new Result(false, StatusCode.EXISTERROR, "该垛位已存在养护计划!");
        }
        int result = maintainPlanService.insertMaintainPlan(maintainPlanDto);
        return new Result<>(true, StatusCode.OK, "养护计划添加成功",result);
    }

    /**
     * 分页查询养护计划
     * @param maintainPlanDto 前端传入添加信息
     * @return Result
     * @createUser wbl
     * @updateUser zyq
     */
    @PostMapping(value = "/findMaintainPlanInfo")
    public Result findMaintainPlanInfo(MaintainPlanDto maintainPlanDto) {
        if(StringUtils.isNotEmpty(maintainPlanDto.getEndTime())){
            if(maintainPlanDto.getEndTime().equals("00:00:00")) {
                maintainPlanDto.setEndTime(maintainPlanDto.getEndTime().replace("00:00:00", "23:59:59"));
            }else {
                maintainPlanDto.setEndTime(maintainPlanDto.getEndTime() + " 23:59:59");
            }
        }
        PageInfo<MaintainPlanVo> maintainPlanInfo = maintainPlanService.findMaintainPlanInfo(maintainPlanDto);
        return new Result(true, StatusCode.OK, "查询成功!", maintainPlanInfo);
    }

    /**
     * 删除养护计划
     * @param stageIds  养护计划id
     * @return Result
     * @createUser wbl
     * @updateUser zyq
     */
    @PostMapping("/deleteMaintainPlanInfo")
    public Result<Integer> deleteMaintainPlanInfo(@RequestParam("stageIds") List<Integer> stageIds) {
        Result result = maintainPlanService.deleteMaintainPlanInfo(stageIds);
        return result;
    }

    /**
     * 根据养护id查询养护详情/更改养护计划时回显
     *
     * @param id 养护计划id
     * @return success or failure
     * @createUser wbl
     *//*
    @PostMapping("/findMaintainPlanById")
    public Result findMaintainPlanById(Integer id) {
        MaintainPlanVo maintainPlan = maintainPlanService.findMaintainPlan(id);
        return new Result<>(true, StatusCode.OK, "查询养护详情成功",maintainPlan);
    }

    *//**
     * 更改养护计划
     * @param maintainPlanDto
     * @return
     *//*
    @PostMapping("/updateMaintainPlan")
    public Result updateMaintainPlan(MaintainPlanDto maintainPlanDto) {
        Integer i = maintainPlanService.updateMaintainPlan(maintainPlanDto);
        return new Result<>(true, StatusCode.OK, "更改养护计划成功",i);
    }

    *//**
     * 养护统计报表
     * @param maintainPlanDto
     * @return
     *//*
    @PostMapping(value = "/findMaintainCountReport")
    public Result findMaintainCountReport(MaintainPlanDto maintainPlanDto) {
        PageInfo<MaintainPlanVo> maintainPlanVos = maintainPlanService.findMaintainCountReport(maintainPlanDto);
        return new Result(true, StatusCode.OK, "查询养护统计报表成功",maintainPlanVos);
    }

    *//**
     * 养护当月新建报表/月底养护报表
     * @param maintainPlanDto
     * @return
     *//*
    @GetMapping(value = "/findMaintainMonthEndReport")
    public Result findMaintainMonthEndReport(MaintainPlanDto maintainPlanDto) {
        PageInfo<MaintainPlanVo> maintainPlanVos = maintainPlanService.findMaintainMonthEndReport(maintainPlanDto);
        return new Result(true, StatusCode.OK, "查询养护统计报表成功",maintainPlanVos);
    }

    *//**
     * 历史汇总单
     * @param maintainPlanDto
     * @return
     *//*
    @PostMapping(value = "/findMaintainHistoryCollect")
    public Result findMaintainHistoryCollect(MaintainPlanDto maintainPlanDto) {
        PageInfo<MaintainPlanVo> maintainPlanVos = maintainPlanService.findMaintainHistoryCollect(maintainPlanDto);
        return new Result(true, StatusCode.OK, "查询历史汇总单成功",maintainPlanVos);
    }

    *//**
     * 开垛使用
     * @param maintainPlanDto
     * @return
     *//*
    @PostMapping(value = "/endMaintainPlan")
    public Result endMaintainPlan(MaintainPlanDto maintainPlanDto) {
        Integer i = maintainPlanService.endMaintainPlan(maintainPlanDto);
        return new Result<>(true, StatusCode.OK, "开垛成功",i);
    }

    *//**
     * 养护到期汇总中的删除
     * @param ids
     * @return
     *//*
    @PostMapping("/deleteMaintainPlanStage")
    public Result deleteMaintainStage(@RequestParam("ids")List<Integer> ids) {
        Integer i = maintainPlanService.deleteMaintainStage(ids);
        return new Result<>(true, StatusCode.OK, "删除成功",i);
    }

    *//**
     * 养护到期汇总中的详情
     * @param id
     * @return
     *//*
    @PostMapping("/findMaintainPlanDetails")
    public Result findMaintainPlanDetails(Integer id) {
        MaintainPlanVo maintainPlanVo = maintainPlanService.findMaintainPlanDetails(id);
        return new Result<>(true, StatusCode.OK, "查询成功",maintainPlanVo);
    }

    *//**
     * 转段
     * @param maintainPlanDto
     * @return
     *//*
    @PostMapping("/changeMaintainPlanStage")
    public Result changeMaintainPlanStage(MaintainPlanDto maintainPlanDto) {
        Integer result = maintainPlanService.changeMaintainPlanStage(maintainPlanDto);
        return new Result<>(true, StatusCode.OK, "转段成功",result);
    }

    @PostMapping("/findExpireCollectTable")
    public Result findExpireCollectTable(MaintainPlanDto maintainPlanDto) {
        Result maintainPlan = maintainPlanService.findExpireCollectTable(maintainPlanDto);
        return maintainPlan;
    }
    *//**
     * 移垛
     * @param maintainPlanDto
     * @return
     *//*
    @PostMapping({"/moveStack"})
    public Result<T> moveStack(@RequestBody com.rpzh.dto.MaintainPlanDto maintainPlanDto) {
        List<NodeDto> nodeDtoList = maintainPlanDto.getNodeDtoList();
        if (maintainPlanDto.getStackId().equals(((NodeDto) nodeDtoList.get(0)).getStackId())) {
            return new Result<>(false, 20001, "垛位没有移动!");
        } else {
            Integer result = maintainPlanService.moveStack(maintainPlanDto);
            return result == -1 ? new Result<>(false, StatusCode.ERROR, "该垛位已有养护计划!") : new Result<>(true, StatusCode.OK, "移垛成功!", result);
        }
    }

    @PostMapping("/getPhases")
    public Result getPhases(String maintenceStyle) {
        List<String> phases = new ArrayList<>();
        phases.add("养护阶段");
        phases.add("充氮杀虫阶段");
        phases.add("磷化铝杀虫阶段");
        if("机械调控方式".equals(maintenceStyle)){
            phases.add("充氮保质阶段");
        }
        return new Result<>(true, StatusCode.OK, "查询养护阶段成功",phases);
    }*/
}
