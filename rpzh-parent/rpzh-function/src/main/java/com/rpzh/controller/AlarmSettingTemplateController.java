package com.rpzh.controller;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.AlarmSettingTemplateDto;
import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.dto.SettingColorAndDayDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.service.AlarmSettingTemplateService;
import com.rpzh.vo.AlarmSettingTemplateVo;
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
 * @Description: 预警规则模板
 */
@RestController
@CrossOrigin
@RequestMapping("/maintain/alarmSettingTemplate")
@Api(value = "预警模板设置类", tags = "预警模板设置类", description = "预警模板设置类")
public class AlarmSettingTemplateController {
    @Resource
    private AlarmSettingTemplateService alarmSettingTemplateService;

    @PostMapping(value = "/findAlarmSettingTemplateTable")
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
    public Result<AlarmValueSettingVo> findAlarmSettingTemplateTable(@ApiIgnore AlarmValueSettingDto alarmValueSettingDto) {
        PageInfo<AlarmValueSettingVo> alarmValueSettingTable = alarmSettingTemplateService.findAlarmSettingTemplateTable(alarmValueSettingDto);
        return new Result<>(true, StatusCode.OK, "查询预警列表成功!",alarmValueSettingTable);
    }

    @PostMapping(value = "/deleteAlarmSettingTemplate")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "materialCodes", value = "物料代码集合", required = true,dataType = "List")
    })
    @ApiOperation(value = "删除预警模板设置")
    public Result deleteAlarmValueSetting(@RequestParam List<String> materialCodes) {
        Result result = alarmSettingTemplateService.deleteAlarmSettingTemplate(materialCodes);
        return result;
    }

    @PostMapping(value = "/insertAlarmSettingTemplate")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "earlyWarningRuleName", value = "预警规则名称", required = true,dataType = "String"),
            @ApiImplicitParam(name = "materialCode", value = "物料代码", required = true,dataType = "String"),
            @ApiImplicitParam(name = "alarmValueSettingDtos", value = "阶段集合", required = true,dataType = "List"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].maintenceStyle", value = "养护方式", required = true, dataType = "String"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].phases", value = "养护阶段", required = true, dataType = "String"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackOxygenUpperOne", value = "垛内氧气上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackOxygenUpperTwo", value = "垛内氧气上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackOxygenUpperThree", value = "垛内氧气上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackOxygenLower", value = "垛内氧气下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].tempCoreChipUpperOne", value = "包芯温度上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].tempCoreChipUpperTwo", value = "包芯温度上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].tempCoreChipUpperThree", value = "包芯温度上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].tempCoreChipLower", value = "包芯温度下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackTempUpperOne", value = "垛内温度上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackTempUpperTwo", value = "垛内温度上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackTempUpperThree", value = "垛内温度上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackTempLower", value = "垛内温度下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackMoistureUpperOne", value = "垛内湿度上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackMoistureUpperTwo", value = "垛内湿度上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackMoistureUpperThree", value = "垛内湿度上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackMoistureLower", value = "垛内湿度下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].coreTempHighEnvTempOne", value = "平均包芯高于环境一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].coreTempHighEnvTempTwo", value = "平均包芯高于环境二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].coreTempHighEnvTempThree", value = "平均包芯高于环境三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].coreTempLowEnvTemp", value = "平均包芯低于环境三级",required = true,dataType ="double")
    })
    @ApiOperation(value = "添加预警设置模板")
    public Result insertAlarmSettingTemplate(@ApiIgnore AlarmSettingTemplateDto alarmSettingTemplateDto) {
        alarmSettingTemplateService.insertAlarmSettingTemplate(alarmSettingTemplateDto);
        return new Result<>(true, StatusCode.OK, "添加预警设置模板成功!");
    }

    @PostMapping(value = "/findColorAndAlertUpgradeDay")
    @ApiOperation(value = "修改升级规则前回显")
    public Result<SettingColorAndDayVo> findColorAndAlertUpgradeDay() {
        SettingColorAndDayVo settingColorAndDayVo = alarmSettingTemplateService.findColorAndAlertUpgradeDay();
        return new Result<>(true, StatusCode.OK, "修改升级规则前回显!",settingColorAndDayVo);
    }

    @PostMapping(value = "/updateOrInsertColorAndAlertUpgradeDay")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "colorOne", value = "预警颜色一级", required = true,dataType = "String"),
            @ApiImplicitParam(name = "colorTwo", value = "预警颜色二级", required = true,dataType = "String"),
            @ApiImplicitParam(name = "colorThree", value = "预警颜色三级", required = true,dataType = "String"),
            @ApiImplicitParam(name = "oneToTwoDay", value = "预警一级升二级的天数", required = true,dataType = "Integer"),
            @ApiImplicitParam(name = "twoToThreeDay", value = "预警二级升三级的天数", required = true,dataType = "Integer")
    })
    @ApiOperation(value = "修改或添加升级规则成功")
    public Result updateOrInsertColorAndAlertUpgradeDay(@ApiIgnore SettingColorAndDayDto settingColorAndDayDto) {
        Result result = alarmSettingTemplateService.updateOrInsertColorAndAlertUpgradeDay(settingColorAndDayDto);
        return result;
    }

    @PostMapping(value = "/findAlarmValueSetting")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "materialCode", value = "物料代码", required = false,dataType = "String"),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式", required = false,dataType = "String")
    })
    @ApiOperation(value = "预警设置修改前回显")
    public Result<AlarmValueSettingVo> findAlarmValueSetting(String materialCode, String maintenceStyle) {
        AlarmSettingTemplateVo alarmSettingTemplateVo = alarmSettingTemplateService.findAlarmSettingTemplateByMaterialCode(materialCode,maintenceStyle);
        return new Result<>(true, StatusCode.OK, "预警设置修改前回显成功!",alarmSettingTemplateVo);
    }

    @PostMapping(value = "/updateAlarmSettingTemplate")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "earlyWarningRuleName", value = "预警规则名称", required = true,dataType = "String"),
            @ApiImplicitParam(name = "materialCode", value = "物料代码", required = true,dataType = "String"),
            @ApiImplicitParam(name = "alarmValueSettingDtos", value = "阶段集合", required = true,dataType = "List"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].maintenceStyle", value = "养护方式", required = true, dataType = "String"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].phases", value = "养护阶段", required = true, dataType = "String"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackOxygenUpperOne", value = "垛内氧气上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackOxygenUpperTwo", value = "垛内氧气上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackOxygenUpperThree", value = "垛内氧气上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackOxygenLower", value = "垛内氧气下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].tempCoreChipUpperOne", value = "包芯温度上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].tempCoreChipUpperTwo", value = "包芯温度上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].tempCoreChipUpperThree", value = "包芯温度上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].tempCoreChipLower", value = "包芯温度下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackTempUpperOne", value = "垛内温度上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackTempUpperTwo", value = "垛内温度上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackTempUpperThree", value = "垛内温度上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackTempLower", value = "垛内温度下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackMoistureUpperOne", value = "垛内湿度上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackMoistureUpperTwo", value = "垛内湿度上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackMoistureUpperThree", value = "垛内湿度上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].stackMoistureLower", value = "垛内湿度下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].coreTempHighEnvTempOne", value = "平均包芯高于环境一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].coreTempHighEnvTempTwo", value = "平均包芯高于环境二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].coreTempHighEnvTempThree", value = "平均包芯高于环境三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "alarmValueSettingDtos[i].coreTempLowEnvTemp", value = "平均包芯低于环境三级",required = true,dataType ="double")
    })
    @ApiOperation(value = "添加预警设置模板")
    public Result updateAlarmSettingTemplate(@ApiIgnore AlarmSettingTemplateDto alarmSettingTemplateDto) {
        alarmSettingTemplateService.updateAlarmSettingTemplate(alarmSettingTemplateDto);
        return new Result<>(true, StatusCode.OK, "更改预警设置模板成功!");
    }
}
