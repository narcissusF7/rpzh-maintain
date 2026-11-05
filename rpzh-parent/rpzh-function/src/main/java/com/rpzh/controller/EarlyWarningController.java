package com.rpzh.controller;

import com.rpzh.dto.EarlyWarningDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.EarlyWarningService;
import com.rpzh.vo.RepairDataVo;
import io.swagger.annotations.*;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.access.prepost.PreAuthorize;
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

    //解决前端传时间参数时出现的问题
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(true);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    @PostMapping("/findVideoMonitoring")
    @ApiOperation(value = "视频监控")
    @ApiImplicitParams({
    })
    public Result findVideoMonitoring(){
        return null;
    }

    @PostMapping("/findEarlyWarningInfo")
    @ApiOperation(value = "预警信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "principalName", value = "负责人", dataType = "String",required = false),
            @ApiImplicitParam(name = "warningType", value = "预警类型:01:设备故障,04:垛内甲虫预警,05:仓间甲虫预警,06:垛内氧气预警,07:仓间氧气预警,08:垛内温度预警,09:仓间温度预警," +
                    "10:垛内湿度预警,11:仓间湿度预警,12:包芯温度预警,13:平均包芯高于环境预警,14:仓间粉螟预警，19:平均包芯低于环境预警", required = false,dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", dataType = "String",required = false),
            @ApiImplicitParam(name = "endTime", value = "结束时间",dataType = "String",required = false)
    })
    public Result findEarlyWarningInfo(@ApiIgnore EarlyWarningDto earlyWarningDto){
        return earlyWarningService.findEarlyWarningInfo(earlyWarningDto);
    }

    @PostMapping("/findEarlyWarningByWarningType")
    @ApiOperation(value = "预警设备")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "principalName", value = "负责人", dataType = "String",required = false),
            @ApiImplicitParam(name = "warningType", value = "预警类型:01:设备故障,04:垛内甲虫预警,05:仓间甲虫预警,06:垛内氧气预警," +
                    "07:仓间氧气预警,08:垛内温度预警,09:仓间温度预警,10:垛内湿度预警,11:仓间湿度预警,12:包芯温度预警,13:平均包芯高于环境预警,14:仓间粉螟预警，19:平均包芯低于环境预警", required = true,dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", dataType = "String",required = false),
            @ApiImplicitParam(name = "endTime", value = "结束时间",dataType = "String",required = false)
    })
    public Result findEarlyWarningByWarningType(@ApiIgnore EarlyWarningDto earlyWarningDto){
        return earlyWarningService.findEarlyWarningByWarningType(earlyWarningDto);
    }

    @PostMapping("/findCurrentWarningList")
    @ApiOperation(value = "当前预警逐条列表（含关联任务ID）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nodeId", value = "节点编号", dataType = "String", required = false),
            @ApiImplicitParam(name = "warningType", value = "预警类型", dataType = "String", required = false),
            @ApiImplicitParam(name = "warningGrade", value = "预警等级", dataType = "String", required = false),
            @ApiImplicitParam(name = "principalName", value = "负责人", dataType = "String", required = false)
    })
    public Result findCurrentWarningList(@ApiIgnore EarlyWarningDto earlyWarningDto){
        return earlyWarningService.findCurrentWarningList(earlyWarningDto);
    }

    @PostMapping("/findEarlyWarningBySiteId")
    @ApiOperation(value = "预警设备详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "principalName", value = "负责人", dataType = "String",required = false),
            @ApiImplicitParam(name = "warningType", value = "预警类型:01:设备故障,04:垛内甲虫预警,05:仓间甲虫预警,06:垛内氧气预警," +
                    "07:仓间氧气预警,08:垛内温度预警,09:仓间温度预警,10:垛内湿度预警,11:仓间湿度预警,12:包芯温度预警,13:平均包芯高于环境预警,14:仓间粉螟预警，19:平均包芯低于环境预警", required = true,dataType = "String"),
            @ApiImplicitParam(name = "stackId", value = "垛位id", dataType = "Integer",required = true),
            @ApiImplicitParam(name = "areaId", value = "仓间Id", required = true,dataType = "Integer"),
            @ApiImplicitParam(name = "pidRoute", value = "位置路由", required = true,dataType = "String"),
            @ApiImplicitParam(name = "warningGrade", value = "预警等级", dataType = "String",required = true),
            @ApiImplicitParam(name = "isAffirm", value = "是否处理", dataType = "String",required = true),
            @ApiImplicitParam(name = "startTime", value = "开始时间", dataType = "String",required = false),
            @ApiImplicitParam(name = "endTime", value = "结束时间",dataType = "String",required = false),
    })
    public Result findEarlyWarningBySiteId(@ApiIgnore EarlyWarningDto earlyWarningDto){
        return earlyWarningService.findEarlyWarningBySiteId(earlyWarningDto);
    }

    @PostMapping("/misreport")
    @ApiOperation(value = "误报")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id",dataType = "int",required = true)
    })
    @PreAuthorize("hasAnyAuthority('f:e:mp')")
    public Result misreport(@RequestParam("id") Integer id){
        if(ToolUtil.isEmpty(id)){
            return new Result<>(false, StatusCode.ERROR, "预警id不能为空");
        }
        earlyWarningService.misreport(id);
        return new Result<>(true, StatusCode.OK, "处理误报成功");
    }

    @PostMapping("/findNodeData")
    @ApiOperation(value = "查看传感器实时数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nodeType", value = "设备类型 01:无线氧气传感器,03:无线温湿度传感器,04:无线包芯温度传感器)", dataType = "String",required = true),
            @ApiImplicitParam(name = "warningType", value = "预警类型", dataType = "String",required = true),
            @ApiImplicitParam(name = "nodeId", value = "设备Id", required = true,dataType = "String"),
            @ApiImplicitParam(name = "pidRoute", value = "位置路由", dataType = "String",required = true),
            @ApiImplicitParam(name = "stackId", value = "位置id", dataType = "Integer",required = true)
    })
    public Result findNodeData(@ApiIgnore EarlyWarningDto earlyWarningDto){
        return earlyWarningService.findNodeData(earlyWarningDto);
    }

    @PostMapping("/getRepairData")
    @ApiOperation(value = "设备维修时回显")
    public Result<RepairDataVo> getRepairData(@RequestParam(value = "stackId", required = false) Integer stackId,
                                              @RequestParam(value = "areaId", required = false) Integer areaId){
        if(ToolUtil.isEmpty(stackId) && ToolUtil.isEmpty(areaId)){
            return new Result<>(false, StatusCode.ERROR, "位置id不能为空");
        }
        List<RepairDataVo> repairDataVos = earlyWarningService.getRepairData(stackId, areaId);
        return new Result<>(true, StatusCode.OK, "查询成功",repairDataVos);
    }

    @PostMapping("/updateRepairId")
    @ApiOperation(value = "去处理（故障），设备维修")
    @PreAuthorize("hasAnyAuthority('f:e:upr')")
    public void updateRepairId(@RequestBody List<Map<String,String>> repairInfos) {
        earlyWarningService.updateRepairId(repairInfos);
    }

    @PostMapping("/warningSend")
    @ApiOperation(value = "发送预警短信")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "number", value = "手机号", dataType = "String",required = true),
            @ApiImplicitParam(name = "username", value = "用户名", required = true,dataType = "String"),
            @ApiImplicitParam(name = "siteRoute", value = "位置路由", dataType = "String",required = true)
    })
    public Result warningSend(@RequestParam("number") String number,
                              @RequestParam("username") String username,
                              @RequestParam("siteRoute") String siteRoute){
        earlyWarningService.warningSend(number,username,siteRoute);
        return new Result<>(true, StatusCode.OK, "发送预警短信成功");
    }
}
