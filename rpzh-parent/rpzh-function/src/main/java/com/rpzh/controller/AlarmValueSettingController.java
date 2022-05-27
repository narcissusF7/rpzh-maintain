package com.rpzh.controller;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.dto.SettingColorAndDayDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.AlarmValueSettingService;
import com.rpzh.util.StringUtils;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.SettingColorAndDayVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/05/07/13:46
 * @Description: 功能
 */
@RestController
@CrossOrigin
@RequestMapping("/maintain/alarmValueSetting")
@Api(value = "预警设置类", tags = "预警设置类", description = "预警设置类")
public class AlarmValueSettingController {
    @Resource
    private AlarmValueSettingService alarmValueSettingService;

    @PostMapping(value = "/findAlarmValueSettingInfo")
    public Result findMaintainPlanInfo(MaintainPlanDto maintainPlanDto) {
        AlarmValueSettingDto alarmValueSettingDto = new AlarmValueSettingDto(maintainPlanDto);
        String materialCodeYear = alarmValueSettingDto.getMaterialCodeYear();
        String phases = alarmValueSettingDto.getPhases();
        String maintenceStyle = alarmValueSettingDto.getMaintenceStyle();
        if(StringUtils.isEmpty(materialCodeYear) || StringUtils.isEmpty(phases) || StringUtils.isEmpty(maintenceStyle)){
            return new Result(false, StatusCode.ERROR, "物料等级,烟叶年份,养护阶段或养护方法不能为空!");
        }
        AlarmValueSettingVo alarmValueSetting = alarmValueSettingService.findMaintainPlanInfo(alarmValueSettingDto);
        return new Result(true, StatusCode.OK, "查询成功!",alarmValueSetting);
    }

    @PostMapping(value = "/findAlarmValueSettingTable")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "earlyWarningRuleName", value = "预警规则名称", required = false,dataType = "String"),
            @ApiImplicitParam(name = "materialCode", value = "物料代码", required = false,dataType = "String"),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式", required = false,dataType = "String"),
            @ApiImplicitParam(name = "creatrUser", value = "创建人", required = false,dataType = "String"),
            @ApiImplicitParam(name = "status", value = "状态(0:停用,1:启用)", required = false,dataType = "Integer"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "size", value = "每页数量,默认为10", required = false,dataType = "Integer"),
            @ApiImplicitParam(name = "page", value = "当前页，默认为第一页", required = false,dataType = "Integer")
    })
    @ApiOperation(value = "预警设置列表")
    public Result<AlarmValueSettingVo> findAlarmValueSettingTable(@ApiIgnore AlarmValueSettingDto alarmValueSettingDto) {
        PageInfo<AlarmValueSettingVo> alarmValueSettingTable = alarmValueSettingService.findAlarmValueSettingTable(alarmValueSettingDto);
        return new Result<>(true, StatusCode.OK, "查询预警列表成功!",alarmValueSettingTable);
    }

    @PostMapping(value = "/changeAlarmValueSettingStatus")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "预警设置id", required = true,dataType = "Integer")
    })
    @ApiOperation(value = "修改预警的状态")
    public Result changeAlarmValueSettingStatus(Integer id) {
        Result result = alarmValueSettingService.changeAlarmValueSettingStatus(id);
        return result;
    }

    @PostMapping(value = "/deleteAlarmValueSetting")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "预警设置id集合", required = true,dataType = "List<Integer>")
    })
    @ApiOperation(value = "删除预警设置")
    public Result deleteAlarmValueSetting(@RequestParam List<Integer> ids) {
        Result result = alarmValueSettingService.deleteAlarmValueSetting(ids);
        return result;
    }

    @PostMapping(value = "/findYear")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "materialCode", value = "物料代码", required = true,dataType = "String")
    })
    @ApiOperation(value = "根据物料代码查找烟叶年份")
    public Result findYear(String materialCode) {
        List<String> years = alarmValueSettingService.findYear(materialCode);
        return new Result<>(true, StatusCode.OK, "查询烟叶年份成功!",years);
    }

    @PostMapping(value = "/insertAlarmValueSetting")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "earlyWarningRuleName", value = "预警规则名称", required = true,dataType = "String"),
            @ApiImplicitParam(name = "materialCodeYear", value = "物料代码和烟叶年份的集合", required = true,dataType = "String"),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式", required = true,dataType = "String"),
            @ApiImplicitParam(name = "phases", value = "养护阶段", required = true,dataType = "String"),
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
            @ApiImplicitParam(name = "coreTempHighEnvTempThree", value = "平均包芯高于环境三级",required = true,dataType ="Double")
    })
    @ApiOperation(value = "增加预警设置")
    public Result insertAlarmValueSetting(@ApiIgnore AlarmValueSettingDto alarmValueSettingDto) {
        String earlyWarningRuleName = alarmValueSettingDto.getEarlyWarningRuleName();
        String materialCodeYear = alarmValueSettingDto.getMaterialCodeYear();
        String phases = alarmValueSettingDto.getPhases();
        String maintenceStyle = alarmValueSettingDto.getMaintenceStyle();
        if(StringUtils.isEmpty(earlyWarningRuleName) || StringUtils.isEmpty(maintenceStyle) ||
                StringUtils.isEmpty(materialCodeYear) || StringUtils.isEmpty(phases)){
            return new Result(false, StatusCode.ERROR, "预警规则名称,物料等级,烟叶年份,养护阶段或养护方法不能为空!");
        }
        Integer count = alarmValueSettingService.isExistEarlyWarningRuleName(earlyWarningRuleName);
        if(count>0){
            return new Result(false, StatusCode.ERROR, "预警规则名称已存在!");
        }
        alarmValueSettingService.insertAlarmValueSetting(alarmValueSettingDto);
        return new Result<>(true, StatusCode.OK, "增加预警设置成功!");
    }

    @PostMapping(value = "/findAlarmValueSettingById")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "预警设置id", required = true,dataType = "Integer")
    })
    @ApiOperation(value = "预警设置修改修改前回显")
    public Result<AlarmValueSettingVo> findAlarmValueSettingById(Integer id) {
        AlarmValueSettingVo alarmValueSettingVo = alarmValueSettingService.findAlarmValueSettingById(id);
        return new Result<>(true, StatusCode.OK, "预警设置修改修改前回显成功!",alarmValueSettingVo);
    }

    @PostMapping(value = "/updateAlarmValueSetting")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "alarmValueSettingId", value = "预警设置id", required = true,dataType = "Integer"),
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
            @ApiImplicitParam(name = "coreTempHighEnvTempThree", value = "平均包芯高于环境三级",required = true,dataType ="Double")
    })
    @ApiOperation(value = "修改预警设置")
    public Result updateAlarmValueSetting(@ApiIgnore AlarmValueSettingDto alarmValueSettingDto) {
        Integer alarmValueSettingId = alarmValueSettingDto.getAlarmValueSettingId();
        String earlyWarningRuleName = alarmValueSettingDto.getEarlyWarningRuleName();
        if(StringUtils.isEmpty(earlyWarningRuleName) || ToolUtil.isEmpty(alarmValueSettingId)){
            return new Result(false, StatusCode.ERROR, "预警规则名称或预警设置id不能为空!");
        }
        Integer count = alarmValueSettingService.isExistEarlyWarningRuleName(earlyWarningRuleName);
        String ruleName = alarmValueSettingService.findEarlyWarningRuleName(alarmValueSettingId);
        if(count>0 && !ruleName.equals(earlyWarningRuleName)){
            return new Result(false, StatusCode.ERROR, "预警规则名称已存在!");
        }
        alarmValueSettingService.updateAlarmValueSetting(alarmValueSettingDto);
        return new Result<>(true, StatusCode.OK, "修改预警设置成功!");
    }

    @PostMapping(value = "/findColorAndAlertUpgradeDay")
    @ApiOperation(value = "修改预警升级和颜色前回显")
    public Result<SettingColorAndDayVo> findColorAndAlertUpgradeDay() {
        SettingColorAndDayVo settingColorAndDayVo = alarmValueSettingService.findColorAndAlertUpgradeDay();
        return new Result<>(true, StatusCode.OK, "预警设置修改前回显成功!",settingColorAndDayVo);
    }

    @PostMapping(value = "/updateColorAndAlertUpgradeDay")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "colorOne", value = "预警颜色一级", required = true,dataType = "String"),
            @ApiImplicitParam(name = "colorTwo", value = "预警颜色二级", required = true,dataType = "String"),
            @ApiImplicitParam(name = "colorThree", value = "预警颜色三级", required = true,dataType = "String"),
            @ApiImplicitParam(name = "oneToTwoDay", value = "预警一级升二级的天数", required = true,dataType = "Integer"),
            @ApiImplicitParam(name = "twoToThreeDay", value = "预警二级升三级的天数", required = true,dataType = "Integer")
    })
    @ApiOperation(value = "修改预警升级和颜色")
    public Result updateColorAndAlertUpgradeDay(@ApiIgnore SettingColorAndDayDto settingColorAndDayDto) {
        Integer result = alarmValueSettingService.updateColorAndAlertUpgradeDay(settingColorAndDayDto);
        return new Result<>(true, StatusCode.OK, "预警设置修改成功!",result);
    }
}
