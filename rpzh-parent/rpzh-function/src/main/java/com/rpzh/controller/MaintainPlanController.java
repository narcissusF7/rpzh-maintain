package com.rpzh.controller;

import com.github.pagehelper.PageInfo;
import com.rpzh.dao.MaintainPlanDao;
import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.MaintainPlanService;
import com.rpzh.dto.MaintainPlanDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import com.rpzh.vo.MaintainPlanVo;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author wbl
 * @description [养护计划控制层]
 */
@RestController
@RequestMapping("/maintain/maintainPlan")
@CrossOrigin
@Api(value = "养护计划类", tags = "养护计划类", description = "养护计划类")
public class MaintainPlanController {

    /**
     * 养护服务层
     */
    @Resource
    private MaintainPlanService maintainPlanService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setLenient(true);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }


    /**
     * 插入一条养护计划
     *
     * @param maintainPlanDto 前端传入添加信息
     * @return success or failure
     * @createUser wbl
     */
    @ApiOperation(value = "新建养护计划", tags = "新建养护计划")
    @PostMapping(value = "/insert")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jwt", value = "令牌",paramType = "请求头"),
            @ApiImplicitParam(name = "stackId", value = "仓库位置Id", required = true),
            @ApiImplicitParam(name = "stackName", value = "仓库位置名称", required = true),
            @ApiImplicitParam(name = "materialCode", value = "物料等级", required = true),
            @ApiImplicitParam(name = "tobaccoYear", value = "烟叶年份",required = true),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式",required = true),
            @ApiImplicitParam(name = "phases", value = "养护阶段",required = true),
            @ApiImplicitParam(name = "maintainDays", value = "养护天数",required = true),
            @ApiImplicitParam(name = "aftercareDateStart", value = "调养日期",required = true),
            @ApiImplicitParam(name = "oxygenReductionTime", value = "降氧完成时间",required = true),
            @ApiImplicitParam(name = "expireTime", value = "到期提醒时间",required = true),
            @ApiImplicitParam(name = "principalName", value = "负责人",required = true),
            @ApiImplicitParam(name = "maintainCount", value = "养护数量",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.earlyWarningRuleName", value = "规则名称",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackOxygenUpperOne", value = "垛内氧气上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackOxygenUpperTwo", value = "垛内氧气上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackOxygenUpperThree", value = "垛内氧气上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackOxygenLower", value = "垛内氧气下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseOxygenUpperOne", value = "仓间氧气上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseOxygenUpperTwo", value = "仓间氧气上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseOxygenUpperThree", value = "仓间氧气上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseOxygenLower", value = "仓间氧气下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.tempCoreChipUpperOne", value = "包芯温度上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.tempCoreChipUpperTwo", value = "包芯温度上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.tempCoreChipUpperThree", value = "包芯温度上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.tempCoreChipLower", value = "包芯温度下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackTempUpperOne", value = "垛内温度上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackTempUpperTwo", value = "垛内温度上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackTempUpperThree", value = "垛内温度上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackTempLower", value = "垛内温度下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackMoistureUpperOne", value = "垛内湿度上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackMoistureUpperTwo", value = "垛内湿度上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackMoistureUpperThree", value = "垛内湿度上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackMoistureLower", value = "垛内湿度下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseTempUpper0ne", value = "仓间温度上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseTempUpperTwo", value = "仓间温度上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseTempUpperThree", value = "仓间温度上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseTempLower", value = "仓间温度下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseMoistureUpperOne", value = "仓间湿度上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseMoistureUpperTwo", value = "仓间湿度上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseMoistureUpperThree", value = "仓间湿度上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseMoistureLower", value = "仓间湿度下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.coreTempHighEnvTempOne", value = "平均包芯高于环境一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.coreTempHighEnvTempTwo", value = "平均包芯高于环境二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.coreTempHighEnvTempThree", value = "平均包芯高于环境三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingId", value = "预警设置Id",required = true)
    })
    public Result insertMaintainPlan(MaintainPlanDto maintainPlanDto){
        // 烟叶代码是否为null
        if (ToolUtil.isEmpty(maintainPlanDto.getMaterialCode()) || ToolUtil.isEmpty(maintainPlanDto.getTobaccoYear())) {
            return new Result(false, StatusCode.ERROR, "烟叶代码或烟叶年份不能为空!");
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
        if (ToolUtil.isEmpty(maintainPlanDto.getPrincipalName())) {
            return new Result(false, StatusCode.ERROR, "负责人没有填写!");
        }
        AlarmValueSettingDto alarmValueSettingDto = maintainPlanDto.getAlarmValueSettingDto();
        String earlyWarningRuleName = alarmValueSettingDto.getEarlyWarningRuleName();
        if (ToolUtil.isEmpty(earlyWarningRuleName)) {
            return new Result(false, StatusCode.ERROR, "预警规则名称不能为空!");
        }
        int result = maintainPlanService.insertMaintainPlan(maintainPlanDto);
        return new Result<>(true, StatusCode.OK, "养护计划添加成功", result);
    }

    /**
     * 分页查询养护计划
     * @param maintainPlanDto 前端传入添加信息
     * @return Result
     * @createUser wbl
     * @updateUser zyq
     */
    @ApiOperation(value = "养护计划列表", tags = "养护计划列表")
    @GetMapping(value = "/findMaintainPlanInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jwt", value = "令牌",paramType = "请求头"),
            @ApiImplicitParam(name = "stackName", value = "仓库位置名称", required = false),
            @ApiImplicitParam(name = "materialCode", value = "物料等级", required = false),
            @ApiImplicitParam(name = "tobaccoYear", value = "烟叶年份",required = false),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式",required = false),
            @ApiImplicitParam(name = "phases", value = "养护阶段",required = false),
            @ApiImplicitParam(name = "startTime", value = "制定开始时间",required = false),
            @ApiImplicitParam(name = "endTime", value = "制定结束时间",required = false),
            @ApiImplicitParam(name = "size", value = "每页数量",required = true),
            @ApiImplicitParam(name = "page", value = "当前页",required = true)
    })
    public Result findMaintainPlanInfo(MaintainPlanDto maintainPlanDto) {
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
    @ApiOperation(value = "删除养护计划", tags = "删除养护计划")
    @PostMapping("/deleteMaintainPlanInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jwt", value = "令牌",paramType = "请求头"),
            @ApiImplicitParam(name = "stageIds", value = "养护阶段id", required = true)
    })
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
     */
    @ApiOperation(value = "根据养护id查询养护详情/更改养护计划时回显", tags = "根据养护id查询养护详情/更改养护计划时回显")
    @GetMapping("/findMaintainPlanById")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jwt", value = "令牌",paramType = "请求头"),
            @ApiImplicitParam(name = "id", value = "养护计划id", required = true)
    })
    public Result findMaintainPlanById(Integer id) {
        MaintainPlanVo maintainPlan = maintainPlanService.findMaintainPlan(id);
        return new Result<>(true, StatusCode.OK, "查询养护详情成功",maintainPlan);
    }

    /**
     * 更改养护计划
     * @param maintainPlanDto
     * @return
     */
    @ApiOperation(value = "修改养护计划", tags = "修改养护计划")
    @PostMapping("/updateMaintainPlan")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jwt", value = "令牌",paramType = "请求头"),
            @ApiImplicitParam(name = "id", value = "养护计划id", required = true),
            @ApiImplicitParam(name = "stackId", value = "仓库位置Id", required = true),
            @ApiImplicitParam(name = "stackName", value = "仓库位置名称", required = true),
            @ApiImplicitParam(name = "materialCode", value = "物料等级", required = true),
            @ApiImplicitParam(name = "tobaccoYear", value = "烟叶年份",required = true),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式",required = true),
            @ApiImplicitParam(name = "phases", value = "养护阶段",required = true),
            @ApiImplicitParam(name = "maintainDays", value = "养护天数",required = true),
            @ApiImplicitParam(name = "aftercareDateStart", value = "调养日期",required = true),
            @ApiImplicitParam(name = "oxygenReductionTime", value = "降氧完成时间",required = true),
            @ApiImplicitParam(name = "expireTime", value = "到期提醒时间",required = true),
            @ApiImplicitParam(name = "principalName", value = "负责人",required = true),
            @ApiImplicitParam(name = "maintainCount", value = "养护数量",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.earlyWarningRuleName", value = "规则名称",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackOxygenUpperOne", value = "垛内氧气上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackOxygenUpperTwo", value = "垛内氧气上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackOxygenUpperThree", value = "垛内氧气上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackOxygenLower", value = "垛内氧气下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseOxygenUpperOne", value = "仓间氧气上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseOxygenUpperTwo", value = "仓间氧气上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseOxygenUpperThree", value = "仓间氧气上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseOxygenLower", value = "仓间氧气下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.tempCoreChipUpperOne", value = "包芯温度上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.tempCoreChipUpperTwo", value = "包芯温度上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.tempCoreChipUpperThree", value = "包芯温度上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.tempCoreChipLower", value = "包芯温度下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackTempUpperOne", value = "垛内温度上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackTempUpperTwo", value = "垛内温度上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackTempUpperThree", value = "垛内温度上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackTempLower", value = "垛内温度下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackMoistureUpperOne", value = "垛内湿度上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackMoistureUpperTwo", value = "垛内湿度上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackMoistureUpperThree", value = "垛内湿度上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackMoistureLower", value = "垛内湿度下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseTempUpper0ne", value = "仓间温度上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseTempUpperTwo", value = "仓间温度上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseTempUpperThree", value = "仓间温度上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseTempLower", value = "仓间温度下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseMoistureUpperOne", value = "仓间湿度上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseMoistureUpperTwo", value = "仓间湿度上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseMoistureUpperThree", value = "仓间湿度上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseMoistureLower", value = "仓间湿度下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.coreTempHighEnvTempOne", value = "平均包芯高于环境一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.coreTempHighEnvTempTwo", value = "平均包芯高于环境二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.coreTempHighEnvTempThree", value = "平均包芯高于环境三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingId", value = "预警设置Id",required = true)
    })
    public Result updateMaintainPlan(MaintainPlanDto maintainPlanDto) {
        Integer i = maintainPlanService.updateMaintainPlan(maintainPlanDto);
        return new Result<>(true, StatusCode.OK, "更改养护计划成功",i);
    }

    /**
     * 养护统计报表
     * @param maintainPlanDto
     * @return
     */
    @ApiOperation(value = "养护统计报表", tags = "养护统计报表")
    @GetMapping(value = "/findMaintainCountReport")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jwt", value = "令牌",paramType = "请求头"),
            @ApiImplicitParam(name = "stackName", value = "仓库位置名称", required = false),
            @ApiImplicitParam(name = "materialCode", value = "物料等级", required = false),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式",required = false),
            @ApiImplicitParam(name = "phases", value = "养护阶段",required = false),
            @ApiImplicitParam(name = "size", value = "每页数量",required = true),
            @ApiImplicitParam(name = "page", value = "当前页",required = true)
    })
    public Result findMaintainCountReport(MaintainPlanDto maintainPlanDto) {
        PageInfo<MaintainPlanVo> maintainPlanVos = maintainPlanService.findMaintainCountReport(maintainPlanDto);
        return new Result(true, StatusCode.OK, "查询养护统计报表成功",maintainPlanVos);
    }

    /**
     * 养护当月新建报表/月底养护报表
     * @param maintainPlanDto
     * @return
     */
    @ApiOperation(value = "月底养护报表", tags = "月底养护报表")
    @GetMapping(value = "/findMaintainMonthEndReport")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jwt", value = "令牌",paramType = "请求头"),
            @ApiImplicitParam(name = "stackName", value = "仓库位置名称", required = false),
            @ApiImplicitParam(name = "materialCode", value = "物料等级", required = false),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式",required = false),
            @ApiImplicitParam(name = "phases", value = "养护阶段",required = false),
            @ApiImplicitParam(name = "startTime", value = "封垛开始时间",required = false),
            @ApiImplicitParam(name = "endTime", value = "封垛结束时间",required = false),
            @ApiImplicitParam(name = "size", value = "每页数量",required = true),
            @ApiImplicitParam(name = "page", value = "当前页",required = true)
    })
    public Result findMaintainMonthEndReport(MaintainPlanDto maintainPlanDto) {
        PageInfo<MaintainPlanVo> maintainPlanVos = maintainPlanService.findMaintainMonthEndReport(maintainPlanDto);
        return new Result(true, StatusCode.OK, "查询养护统计报表成功",maintainPlanVos);
    }

    /**
     * 历史汇总单
     * @param maintainPlanDto
     * @return
     */
    @ApiOperation(value = "历史汇总单", tags = "历史汇总单")
    @GetMapping(value = "/findMaintainHistoryCollect")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jwt", value = "令牌",paramType = "请求头"),
            @ApiImplicitParam(name = "stackName", value = "仓库位置名称", required = false),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式",required = false),
            @ApiImplicitParam(name = "phases", value = "养护阶段",required = false),
            @ApiImplicitParam(name = "changeStatus", value = "转段状态 0:未变更,1:已变更",required = false),
            @ApiImplicitParam(name = "size", value = "每页数量",required = true),
            @ApiImplicitParam(name = "page", value = "当前页",required = true)
    })
    public Result findMaintainHistoryCollect(MaintainPlanDto maintainPlanDto) {
        PageInfo<MaintainPlanVo> maintainPlanVos = maintainPlanService.findMaintainHistoryCollect(maintainPlanDto);
        return new Result(true, StatusCode.OK, "查询历史汇总单成功",maintainPlanVos);
    }

    /**
     * 开垛使用
     * @param maintainPlanDto
     * @return
     */
    @ApiOperation(value = "开垛使用", tags = "开垛使用")
    @PostMapping(value = "/endMaintainPlan")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jwt", value = "令牌",paramType = "请求头"),
            @ApiImplicitParam(name = "id", value = "养护计划id", required = true),
            @ApiImplicitParam(name = "alarmValueSettingId", value = "预警设置id",required = true)
    })
    public Result endMaintainPlan(MaintainPlanDto maintainPlanDto) {
        Integer i = maintainPlanService.endMaintainPlan(maintainPlanDto);
        return new Result<>(true, StatusCode.OK, "开垛成功",i);
    }

    /**
     * 养护到期汇总中的删除
     * @param ids
     * @return
     */
    @ApiOperation(value = "养护到期汇总中的删除", tags = "养护到期汇总中的删除")
    @PostMapping("/deleteMaintainPlanStage")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jwt", value = "令牌",paramType = "请求头"),
            @ApiImplicitParam(name = "ids", value = "养护计划id", required = true)
    })
    public Result deleteMaintainStage(@RequestParam("ids")List<Integer> ids) {
        Integer i = maintainPlanService.deleteMaintainStage(ids);
        return new Result<>(true, StatusCode.OK, "删除成功",i);
    }

    /**
     * 养护到期汇总中的详情
     * @param id
     * @return
     */
    @ApiOperation(value = "养护到期汇总中的详情", tags = "养护到期汇总中的详情")
    @GetMapping("/findMaintainPlanDetails")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jwt", value = "令牌",paramType = "请求头"),
            @ApiImplicitParam(name = "id", value = "养护计划id", required = true)
    })
    public Result findMaintainPlanDetails(Integer id) {
        MaintainPlanVo maintainPlanVo = maintainPlanService.findMaintainPlanDetails(id);
        return new Result<>(true, StatusCode.OK, "查询成功",maintainPlanVo);
    }

    /**
     * 转段
     * @param maintainPlanDto
     * @return
     */
    @ApiOperation(value = "转段", tags = "转段")
    @PostMapping("/changeMaintainPlanStage")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jwt", value = "令牌",paramType = "请求头"),
            @ApiImplicitParam(name = "id", value = "养护计划id", required = true),
            @ApiImplicitParam(name = "stackId", value = "仓库位置Id", required = true),
            @ApiImplicitParam(name = "stackName", value = "仓库位置名称", required = true),
            @ApiImplicitParam(name = "materialCode", value = "物料等级", required = true),
            @ApiImplicitParam(name = "tobaccoYear", value = "烟叶年份",required = true),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式",required = true),
            @ApiImplicitParam(name = "phases", value = "养护阶段",required = true),
            @ApiImplicitParam(name = "maintainDays", value = "养护天数",required = true),
            @ApiImplicitParam(name = "aftercareDateStart", value = "调养日期",required = true),
            @ApiImplicitParam(name = "oxygenReductionTime", value = "降氧完成时间",required = true),
            @ApiImplicitParam(name = "expireTime", value = "到期提醒时间",required = true),
            @ApiImplicitParam(name = "principalName", value = "负责人",required = true),
            @ApiImplicitParam(name = "maintainCount", value = "养护数量",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.earlyWarningRuleName", value = "规则名称",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackOxygenUpperOne", value = "垛内氧气上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackOxygenUpperTwo", value = "垛内氧气上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackOxygenUpperThree", value = "垛内氧气上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackOxygenLower", value = "垛内氧气下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseOxygenUpperOne", value = "仓间氧气上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseOxygenUpperTwo", value = "仓间氧气上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseOxygenUpperThree", value = "仓间氧气上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseOxygenLower", value = "仓间氧气下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.tempCoreChipUpperOne", value = "包芯温度上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.tempCoreChipUpperTwo", value = "包芯温度上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.tempCoreChipUpperThree", value = "包芯温度上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.tempCoreChipLower", value = "包芯温度下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackTempUpperOne", value = "垛内温度上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackTempUpperTwo", value = "垛内温度上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackTempUpperThree", value = "垛内温度上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackTempLower", value = "垛内温度下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackMoistureUpperOne", value = "垛内湿度上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackMoistureUpperTwo", value = "垛内湿度上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackMoistureUpperThree", value = "垛内湿度上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.stackMoistureLower", value = "垛内湿度下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseTempUpper0ne", value = "仓间温度上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseTempUpperTwo", value = "仓间温度上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseTempUpperThree", value = "仓间温度上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseTempLower", value = "仓间温度下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseMoistureUpperOne", value = "仓间湿度上限一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseMoistureUpperTwo", value = "仓间湿度上限二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseMoistureUpperThree", value = "仓间湿度上限三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.warehouseMoistureLower", value = "仓间湿度下限",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.coreTempHighEnvTempOne", value = "平均包芯高于环境一级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.coreTempHighEnvTempTwo", value = "平均包芯高于环境二级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingDto.coreTempHighEnvTempThree", value = "平均包芯高于环境三级",required = true),
            @ApiImplicitParam(name = "alarmValueSettingId", value = "预警设置Id",required = true)
    })
    public Result changeMaintainPlanStage(MaintainPlanDto maintainPlanDto) {
        Integer result = maintainPlanService.changeMaintainPlanStage(maintainPlanDto);
        return new Result<>(true, StatusCode.OK, "转段成功",result);
    }

    @ApiOperation(value = "养护到期汇总列表", tags = "养护到期汇总列表")
    @GetMapping("/findExpireCollectTable")
    public Result findExpireCollectTable(MaintainPlanDto maintainPlanDto) {
        PageInfo<MaintainPlanVo> maintainPlan = maintainPlanService.findExpireCollectTable(maintainPlanDto);
        return new Result<>(true, StatusCode.OK, "养护到期汇总列表查询成功",maintainPlan);
    }
}
