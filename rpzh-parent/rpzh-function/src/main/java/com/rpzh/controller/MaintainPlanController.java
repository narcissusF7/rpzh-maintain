package com.rpzh.controller;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.dto.MaintainCountDto;
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
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import com.rpzh.vo.MaintainPlanVo;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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
    @ApiOperation(value = "查询预警规则名称")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "earlyWarningRuleName", value = "预警规则名称", required = true,dataType = "String")
    })
    public Result<AlarmValueSettingVo> findAlarmSettingTemplate(String earlyWarningRuleName){
        AlarmValueSettingVo alarmSettingTemplate = maintainPlanService.findAlarmSettingTemplate(earlyWarningRuleName);
        return new Result<AlarmValueSettingVo>(true, StatusCode.OK, "查询成功",alarmSettingTemplate);
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
            @ApiImplicitParam(name = "materialCodes", value = "物料代码集合", required = true, dataType = "List"),
            @ApiImplicitParam(name = "stackId", value = "垛位id", required = true, dataType = "int"),
            @ApiImplicitParam(name = "principalIds", value = "负责人ids集合", required = true, dataType = "List"),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式", required = true, dataType = "String"),
            @ApiImplicitParam(name = "maintainDays", value = "养护天数", required = true, dataType = "int"),
            @ApiImplicitParam(name = "aftercareDateStart", value = "开始调养日期", required = true, dataType = "String"),
            @ApiImplicitParam(name = "oxygenReductionTime", value = "降氧完成时间", required = true, dataType = "String"),
            @ApiImplicitParam(name = "expireTime", value = "到期提醒时间", required = true, dataType = "String"),
            @ApiImplicitParam(name = "maintainCount", value = "养护数量", required = true, dataType = "double"),
            @ApiImplicitParam(name = "materialBatchNum", value = "批次号", required = true, dataType = "String"),
            @ApiImplicitParam(name = "phases", value = "养护阶段", required = true, dataType = "String"),
            @ApiImplicitParam(name = "earlyWarningRuleName", value = "预警规则名称", required = true,dataType = "String"),
            @ApiImplicitParam(name = "stackOxygenUpperOne", value = "垛内氧气上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackOxygenUpperTwo", value = "垛内氧气上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackOxygenUpperThree", value = "垛内氧气上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackOxygenLower", value = "垛内氧气下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "tempCoreChipUpperOne", value = "包芯温度上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "tempCoreChipUpperTwo", value = "包芯温度上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "tempCoreChipUpperThree", value = "包芯温度上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "tempCoreChipLower", value = "包芯温度下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackTempUpperOne", value = "垛内温度上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackTempUpperTwo", value = "垛内温度上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackTempUpperThree", value = "垛内温度上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackTempLower", value = "垛内温度下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackMoistureUpperOne", value = "垛内湿度上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackMoistureUpperTwo", value = "垛内湿度上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackMoistureUpperThree", value = "垛内湿度上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackMoistureLower", value = "垛内湿度下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "coreTempHighEnvTempOne", value = "平均包芯高于环境一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "coreTempHighEnvTempTwo", value = "平均包芯高于环境二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "coreTempHighEnvTempThree", value = "平均包芯高于环境三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "pidRoute", value = "位置路径,00:根节点,01:仓库, 02: 楼号, 03: 层, 04: 区, 05: 垛位",required = true,dataType ="String"),
            @ApiImplicitParam(name = "coreTempLowEnvTemp", value = "平均包芯低于环境三级",required = true,dataType ="double")
    })
    public Result insertMaintainPlan(@ApiIgnore MaintainPlanDto maintainPlanDto){
        // 烟叶代码是否为null
        if (ToolUtil.isEmpty(maintainPlanDto.getMaterialCodes())) {
            return new Result(false, StatusCode.ERROR, "烟叶代码不能为空!");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getStackId())) {
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
    @ApiOperation(value = "分页查询养护计划")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "materialCode", value = "物料代码", required = false, dataType = "String"),
            @ApiImplicitParam(name = "stackName", value = "垛位名称", required = false, dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式", required = false, dataType = "String"),
            @ApiImplicitParam(name = "phases", value = "养护阶段", required = false, dataType = "String"),
            @ApiImplicitParam(name = "size", value = "每页数量", required = false,dataType = "int"),
            @ApiImplicitParam(name = "page", value = "当前页",required = false,dataType ="int")
    })
    public Result<MaintainPlanVo> findMaintainPlanInfo(@ApiIgnore MaintainPlanDto maintainPlanDto) {
        if(StringUtils.isNotEmpty(maintainPlanDto.getEndTime())){
            maintainPlanDto.setEndTime(maintainPlanDto.getEndTime().replace("00:00:00", "23:59:59"));
        }
        PageInfo<MaintainPlanVo> maintainPlanInfo = maintainPlanService.findMaintainPlanInfo(maintainPlanDto);
        return new Result(true, StatusCode.OK, "查询成功!", maintainPlanInfo);
    }

    /**
     * 删除养护计划
     * @param ids  养护计划id
     * @return Result
     * @createUser wbl
     * @updateUser zyq
     */
    @PostMapping("/deleteMaintainPlanInfo")
    @ApiOperation(value = "删除养护计划")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "养护计划ids", required = true, dataType = "String")
    })
    public Result<Integer> deleteMaintainPlanInfo(@RequestParam("ids") List<Integer> ids) {
        Result result = maintainPlanService.deleteMaintainPlanInfo(ids);
        return result;
    }

    /**
     * 根据养护id查询养护详情
     *
     * @param id 养护计划id
     * @return success or failure
     * @createUser wbl
     */
    @PostMapping("/findMaintainPlanById")
    @ApiOperation(value = "根据养护id查询养护详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "养护计划id", required = true, dataType = "int")
    })
    public Result<MaintainPlanVo> findMaintainPlanById(Integer id) {
        MaintainPlanVo maintainPlan = maintainPlanService.findMaintainPlan(id);
        return new Result<MaintainPlanVo>(true, StatusCode.OK, "查询养护详情成功",maintainPlan);
    }

    @PostMapping("/beforeUpdateMaintainPlan")
    @ApiOperation(value = "更改养护计划前回显")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "养护计划id", required = true, dataType = "int")
    })
    public Result<MaintainPlanVo> beforeUpdateMaintainPlan(Integer id){
        MaintainPlanVo maintainPlan = maintainPlanService.beforeUpdateMaintainPlan(id);
        return new Result<MaintainPlanVo>(true, StatusCode.OK, "回显成功",maintainPlan);
    }
    /**
     * 更改养护计划
     * @param maintainPlanDto
     * @return
     */
    @PostMapping("/updateMaintainPlan")
    @ApiOperation(value = "更改养护计划")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "materialCodes", value = "物料代码集合", required = true, dataType = "List"),
            @ApiImplicitParam(name = "id", value = "养护计划id", required = true, dataType = "int"),
            @ApiImplicitParam(name = "stageId", value = "养护阶段id", required = true, dataType = "int"),
            @ApiImplicitParam(name = "stackId", value = "垛位id", required = true, dataType = "int"),
            @ApiImplicitParam(name = "principalIds", value = "负责人ids集合", required = true, dataType = "List"),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式", required = true, dataType = "String"),
            @ApiImplicitParam(name = "maintainDays", value = "养护天数", required = true, dataType = "int"),
            @ApiImplicitParam(name = "aftercareDateStart", value = "开始调养日期", required = true, dataType = "String"),
            @ApiImplicitParam(name = "oxygenReductionTime", value = "降氧完成时间", required = true, dataType = "String"),
            @ApiImplicitParam(name = "expireTime", value = "到期提醒时间", required = true, dataType = "String"),
            @ApiImplicitParam(name = "maintainCount", value = "养护数量", required = true, dataType = "double"),
            @ApiImplicitParam(name = "materialBatchNum", value = "批次号", required = true, dataType = "String"),
            @ApiImplicitParam(name = "phases", value = "养护阶段", required = true, dataType = "String"),
            @ApiImplicitParam(name = "earlyWarningRuleName", value = "预警规则名称", required = true,dataType = "String"),
            @ApiImplicitParam(name = "alarmValueSettingId", value = "预警设置id", required = true,dataType = "String"),
            @ApiImplicitParam(name = "stackOxygenUpperOne", value = "垛内氧气上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackOxygenUpperTwo", value = "垛内氧气上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackOxygenUpperThree", value = "垛内氧气上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackOxygenLower", value = "垛内氧气下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "tempCoreChipUpperOne", value = "包芯温度上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "tempCoreChipUpperTwo", value = "包芯温度上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "tempCoreChipUpperThree", value = "包芯温度上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "tempCoreChipLower", value = "包芯温度下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackTempUpperOne", value = "垛内温度上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackTempUpperTwo", value = "垛内温度上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackTempUpperThree", value = "垛内温度上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackTempLower", value = "垛内温度下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackMoistureUpperOne", value = "垛内湿度上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackMoistureUpperTwo", value = "垛内湿度上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackMoistureUpperThree", value = "垛内湿度上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackMoistureLower", value = "垛内湿度下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "coreTempHighEnvTempOne", value = "平均包芯高于环境一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "coreTempHighEnvTempTwo", value = "平均包芯高于环境二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "coreTempHighEnvTempThree", value = "平均包芯高于环境三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "pidRoute", value = "位置路径,00:根节点,01:仓库, 02: 楼号, 03: 层, 04: 区, 05: 垛位",required = true,dataType ="String"),
            @ApiImplicitParam(name = "coreTempLowEnvTemp", value = "平均包芯低于环境三级",required = true,dataType ="double")
    })
    public Result updateMaintainPlan(@ApiIgnore MaintainPlanDto maintainPlanDto) {
        Integer i = maintainPlanService.updateMaintainPlan(maintainPlanDto);
        return new Result<>(true, StatusCode.OK, "更改养护计划成功",i);
    }

    /**
     * 开垛使用
     * @param maintainPlanDto
     * @return
     */
    @PostMapping(value = "/endMaintainPlan")
    @ApiOperation(value = "开垛使用")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "养护计划id", required = true, dataType = "int"),
            @ApiImplicitParam(name = "stageId", value = "养护阶段id", required = true, dataType = "int"),
            @ApiImplicitParam(name = "stackId", value = "垛位id", required = true, dataType = "int")
    })
    public Result endMaintainPlan(@ApiIgnore MaintainPlanDto maintainPlanDto) {
        Integer i = maintainPlanService.endMaintainPlan(maintainPlanDto);
        return new Result<>(true, StatusCode.OK, "开垛成功",i);
    }

    @PostMapping("/getPhases")
    @ApiOperation(value = "根据养护方式查询养护阶段")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式", required = true, dataType = "String")
    })
    public Result getPhases(String maintenceStyle) {
        List<String> phases = new ArrayList<>();
        phases.add("养护阶段");
        phases.add("充氮杀虫阶段");
        phases.add("磷化铝杀虫阶段");
        if(maintenceStyle.equals("机械调控")) {
            phases.add("充氮保质阶段");
        }
        return new Result<>(true, StatusCode.OK, "查询养护阶段成功",phases);
    }

    /**
     * 转段
     * @param maintainPlanDto
     * @return
     */
    @PostMapping("/changeMaintainPlanStage")
    @ApiOperation(value = "转段")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "materialCodes", value = "物料代码集合", required = true, dataType = "List"),
            @ApiImplicitParam(name = "id", value = "养护计划id", required = true, dataType = "int"),
            @ApiImplicitParam(name = "stageId", value = "养护阶段id", required = true, dataType = "int"),
            @ApiImplicitParam(name = "stackId", value = "垛位id", required = true, dataType = "int"),
            @ApiImplicitParam(name = "principalIds", value = "负责人ids集合", required = true, dataType = "List"),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式", required = true, dataType = "String"),
            @ApiImplicitParam(name = "maintainDays", value = "养护天数", required = true, dataType = "int"),
            @ApiImplicitParam(name = "aftercareDateStart", value = "开始调养日期", required = true, dataType = "String"),
            @ApiImplicitParam(name = "oxygenReductionTime", value = "降氧完成时间", required = true, dataType = "String"),
            @ApiImplicitParam(name = "expireTime", value = "到期提醒时间", required = true, dataType = "String"),
            @ApiImplicitParam(name = "maintainCount", value = "养护数量", required = true, dataType = "double"),
            @ApiImplicitParam(name = "materialBatchNum", value = "批次号", required = true, dataType = "String"),
            @ApiImplicitParam(name = "phases", value = "养护阶段", required = true, dataType = "String"),
            @ApiImplicitParam(name = "earlyWarningRuleName", value = "预警规则名称", required = true,dataType = "String"),
            @ApiImplicitParam(name = "alarmValueSettingId", value = "预警设置id", required = true,dataType = "String"),
            @ApiImplicitParam(name = "stackOxygenUpperOne", value = "垛内氧气上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackOxygenUpperTwo", value = "垛内氧气上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackOxygenUpperThree", value = "垛内氧气上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackOxygenLower", value = "垛内氧气下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "tempCoreChipUpperOne", value = "包芯温度上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "tempCoreChipUpperTwo", value = "包芯温度上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "tempCoreChipUpperThree", value = "包芯温度上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "tempCoreChipLower", value = "包芯温度下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackTempUpperOne", value = "垛内温度上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackTempUpperTwo", value = "垛内温度上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackTempUpperThree", value = "垛内温度上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackTempLower", value = "垛内温度下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackMoistureUpperOne", value = "垛内湿度上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackMoistureUpperTwo", value = "垛内湿度上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackMoistureUpperThree", value = "垛内湿度上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "stackMoistureLower", value = "垛内湿度下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "coreTempHighEnvTempOne", value = "平均包芯高于环境一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "coreTempHighEnvTempTwo", value = "平均包芯高于环境二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "coreTempHighEnvTempThree", value = "平均包芯高于环境三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "pidRoute", value = "位置路径,00:根节点,01:仓库, 02: 楼号, 03: 层, 04: 区, 05: 垛位",required = true,dataType ="String"),
            @ApiImplicitParam(name = "coreTempLowEnvTemp", value = "平均包芯低于环境三级",required = true,dataType ="double")
    })
    public Result changeMaintainPlanStage(@ApiIgnore MaintainPlanDto maintainPlanDto) {
        Integer result = maintainPlanService.changeMaintainPlanStage(maintainPlanDto);
        return new Result<>(true, StatusCode.OK, "转段成功",result);
    }

    /**
     * 养护到期汇总中的详情
     * @param maintainPlanDto
     * @return
     */
    @PostMapping("/findMaintainPlanDetails")
    @ApiOperation(value = "养护到期汇总中的详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "养护计划id", required = true, dataType = "int"),
            @ApiImplicitParam(name = "stackId", value = "垛位id", required = true, dataType = "int")
    })
    public Result<MaintainPlanVo> findMaintainPlanDetails(@ApiIgnore MaintainPlanDto maintainPlanDto) {
        MaintainPlanVo maintainPlanVo = maintainPlanService.findMaintainPlanDetails(maintainPlanDto.getId(),maintainPlanDto.getStackId());
        return new Result<MaintainPlanVo>(true, StatusCode.OK, "查询成功",maintainPlanVo);
    }

    @PostMapping("/findExpireCollectTable")
    @ApiOperation(value = "养护到期汇总列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "materialCode", value = "物料代码", required = false, dataType = "String"),
            @ApiImplicitParam(name = "stackName", value = "垛位名称", required = false, dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式", required = false, dataType = "String"),
            @ApiImplicitParam(name = "phases", value = "养护阶段", required = false, dataType = "String"),
            @ApiImplicitParam(name = "size", value = "养护数量", required = false,dataType = "double"),
            @ApiImplicitParam(name = "size", value = "每页数量", required = false,dataType = "int"),
            @ApiImplicitParam(name = "page", value = "当前页",required = false,dataType ="int")
    })
    public Result<MaintainPlanVo> findExpireCollectTable(@ApiIgnore MaintainPlanDto maintainPlanDto) {
        PageInfo<MaintainPlanVo> maintainPlanInfo = maintainPlanService.findExpireCollectTable(maintainPlanDto);
        return new Result<>(true, StatusCode.OK, "养护到期汇总列表查询成功",maintainPlanInfo);
    }

    /**
     * 养护当月新建报表/月底养护报表
     * @param maintainPlanDto
     * @return
     */
    @GetMapping(value = "/findMaintainMonthEndReport")
    @ApiOperation(value = "月底养护报表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "materialCode", value = "物料代码", required = false, dataType = "String"),
            @ApiImplicitParam(name = "stackName", value = "垛位名称", required = false, dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式", required = false, dataType = "String"),
            @ApiImplicitParam(name = "phases", value = "养护阶段", required = false, dataType = "String"),
            @ApiImplicitParam(name = "size", value = "每页数量", required = false,dataType = "int"),
            @ApiImplicitParam(name = "page", value = "当前页",required = false,dataType ="int")
    })
    public Result<MaintainPlanVo> findMaintainMonthEndReport(@ApiIgnore MaintainPlanDto maintainPlanDto) {
        PageInfo<MaintainPlanVo> maintainPlanVos = maintainPlanService.findMaintainMonthEndReport(maintainPlanDto);
        return new Result(true, StatusCode.OK, "查询养护统计报表成功",maintainPlanVos);
    }

    /**
     * 历史汇总单
     * @param maintainPlanDto
     * @return
     */
    @PostMapping(value = "/findMaintainHistoryCollect")
    @ApiOperation(value = "历史汇总单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "stackName", value = "垛位名称", required = false, dataType = "String"),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式", required = false, dataType = "String"),
            @ApiImplicitParam(name = "phases", value = "养护阶段", required = false, dataType = "String"),
            @ApiImplicitParam(name = "size", value = "每页数量", required = false,dataType = "int"),
            @ApiImplicitParam(name = "page", value = "当前页",required = false,dataType ="int")
    })
    public Result findMaintainHistoryCollect(@ApiIgnore MaintainPlanDto maintainPlanDto) {
        PageInfo<MaintainPlanVo> maintainPlanVos = maintainPlanService.findMaintainHistoryCollect(maintainPlanDto);
        return new Result(true, StatusCode.OK, "查询历史汇总单成功",maintainPlanVos);
    }

    @ApiOperation(value = "移垛")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "养护计划id", dataType = "Integer",required = true),
            @ApiImplicitParam(name = "stackId", value = "垛位id", dataType = "Integer",required = true),
            @ApiImplicitParam(name = "nodeDtoList", value = "该养护计划下的所有垛位", required = true,allowMultiple = true,dataType = "String"),
            @ApiImplicitParam(name = "pidRoute", value = "位置路由", dataType = "String"),
            @ApiImplicitParam(name = "siteName", value = "位置名称",dataType = "String"),
            @ApiImplicitParam(name = "xxxx", value = "nodeDto扩展说明  id|nodeId|nodeType|x|y|z  用，隔开",dataType ="int")
    })
    @PostMapping({"/moveStack"})
    public Result moveStack(MaintainPlanDto maintainPlanDto) {
        //TODO  根据情况修改
        NodeDto[] nodeDtoList = maintainPlanDto.getNodeDtoList();
        if (maintainPlanDto.getStackId().equals(nodeDtoList[0].getStackId())) {
            return new Result<>(false, StatusCode.ERROR, "垛位没有移动!");
        } else {
            Integer result = maintainPlanService.moveStack(maintainPlanDto);
            return result == -1 ? new Result<>(false, StatusCode.ERROR, "该垛位已有养护计划!") : new Result<>(true, StatusCode.OK, "移垛成功!", result);
        }
    }


    /**
     * 养护统计报表
     * @param maintainCountDto
     * @return
     */
    @PostMapping(value = "/findMaintainCountReport")
    @ApiOperation(value = "养护数量")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pidRoute", value = "位置路由", required = false, dataType = "String"),
            @ApiImplicitParam(name = "stackId", value = "位置id", required = false, dataType = "int"),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式", required = false, dataType = "String"),
            @ApiImplicitParam(name = "phases", value = "养护阶段", required = false, dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "size", value = "每页数量", required = false,dataType = "int"),
            @ApiImplicitParam(name = "page", value = "当前页",required = false,dataType ="int")
    })
    public Result findMaintainCountReport(@ApiIgnore MaintainCountDto maintainCountDto) {
        Map<String,Object> map = maintainPlanService.findMaintainCountReport(maintainCountDto);
        return new Result(true, StatusCode.OK, "查询养护统计报表成功",map);
    }

}
