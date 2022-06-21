package com.rpzh.controller;

import com.rpzh.dto.EarlyWarningDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.EarlyWarningService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: zyq
 * @Date: 2022/05/20/17:34
 * @Description: 功能
 */
@RestController
@CrossOrigin
@RequestMapping("/maintain/alarmWarning")
@Api(value = "预警信息类", tags = "预警信息类", description = "预警信息类")
public class EarlyWarningController {
    @Resource
    private EarlyWarningService earlyWarningService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(true);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    @PostMapping("/findEarlyWarningInfo")
    @ApiOperation(value = "预警信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "principalName", value = "负责人", dataType = "String",required = false),
            @ApiImplicitParam(name = "warningType", value = "预警类型:01:设备电量故障,02:设备信号强度故障,03:设备丢包故障,04:垛内甲虫预警,05:仓间甲虫预警,06:垛内氧气预警,07:仓间氧气预警,08:垛内温度预警,09:仓间温度预警,10:垛内湿度预警,11:仓间湿度预警,12:包芯温度预警,13:平均包芯高于环境预警,14:仓间粉螟预警", required = false,dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", dataType = "String",required = false),
            @ApiImplicitParam(name = "endTime", value = "结束时间",dataType = "String",required = false)
    })
    public Result findEarlyWarningInfo(@ApiIgnore EarlyWarningDto earlyWarningDto){
        return earlyWarningService.findEarlyWarningInfo(earlyWarningDto);
    }

    @PostMapping("/findEarlyWarningByWarningType")
    @ApiOperation(value = "预警信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "principalName", value = "负责人", dataType = "String",required = false),
            @ApiImplicitParam(name = "warningType", value = "预警类型:01:设备电量故障,02:设备信号强度故障,03:设备丢包故障,04:垛内甲虫预警,05:仓间甲虫预警,06:垛内氧气预警,07:仓间氧气预警,08:垛内温度预警,09:仓间温度预警,10:垛内湿度预警,11:仓间湿度预警,12:包芯温度预警,13:平均包芯高于环境预警,14:仓间粉螟预警", required = true,dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", dataType = "String",required = false),
            @ApiImplicitParam(name = "endTime", value = "结束时间",dataType = "String",required = false)
    })
    public Result findEarlyWarningByWarningType(EarlyWarningDto earlyWarningDto){
        return earlyWarningService.findEarlyWarningByWarningType(earlyWarningDto);
    }

    @PostMapping("/findEarlyWarningBySiteId")
    @ApiOperation(value = "预警信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "warningType", value = "预警类型:01:设备电量故障,02:设备信号强度故障,03:设备丢包故障,04:垛内甲虫预警,05:仓间甲虫预警,06:垛内氧气预警,07:仓间氧气预警,08:垛内温度预警,09:仓间温度预警,10:垛内湿度预警,11:仓间湿度预警,12:包芯温度预警,13:平均包芯高于环境预警,14:仓间粉螟预警", required = true,dataType = "String"),
            @ApiImplicitParam(name = "stackId", value = "垛位id", dataType = "Integer",required = true),
            @ApiImplicitParam(name = "areaId", value = "仓间Id", required = true,dataType = "Integer"),
            @ApiImplicitParam(name = "affirm", value = "是否处理", dataType = "String",required = true),
            @ApiImplicitParam(name = "warningTime", value = "预警时间",dataType = "String",required = true)
    })
    public Result findEarlyWarningBySiteId(EarlyWarningDto earlyWarningDto){
        return earlyWarningService.findEarlyWarningBySiteId(earlyWarningDto);
    }

    @PostMapping("/misreport")
    @ApiOperation(value = "误报")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "warningTime", value = "预警时间",dataType = "String",required = true)
    })
    public Result misreport(Integer id){
        if(ToolUtil.isEmpty(id)){
            return new Result<>(false, StatusCode.ERROR, "预警id不能为空");
        }
        earlyWarningService.misreport(id);
        return new Result<>(true, StatusCode.OK, "处理误报成功");
    }

    @PostMapping("/findNodeData")
    @ApiOperation(value = "预警信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nodeType", value = "设备类型 01:无线氧气传感器,03:无线温湿度传感器,04:无线包芯温度传感器)", dataType = "String",required = true),
            @ApiImplicitParam(name = "warningType", value = "预警类型)", dataType = "String",required = true),
            @ApiImplicitParam(name = "nodeId", value = "设备Id", required = true,dataType = "String"),
            @ApiImplicitParam(name = "stackId", value = "垛位id", dataType = "String",required = false),
            @ApiImplicitParam(name = "areaId", value = "仓间id", dataType = "String",required = false)
    })
    public Result findNodeData(EarlyWarningDto earlyWarningDto){
        return earlyWarningService.findNodeData(earlyWarningDto);
    }

}
