package com.rpzh.controller;


import com.github.pagehelper.PageInfo;
import com.rpzh.dto.LogAlarmSettingDto;
import com.rpzh.dto.WareAreaAlarmSettingDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.service.WareAreaAlarmSettingService;
import com.rpzh.vo.WareAreaAlarmSettingVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/05/07/13:46
 * @Description: 仓间预警规则
 */
@RestController
@CrossOrigin
@RequestMapping("/maintain/wareAreaAlarmSetting")
@Api(value = "仓间预警规则类", tags = "仓间预警规则类", description = "仓间预警规则类")
@Slf4j
public class WareAreaAlarmSettingController {
    @Resource
    private WareAreaAlarmSettingService wareAreaAlarmSettingService;

    @PostMapping(value = "/insertWareAreaAlarmSetting")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "warehouseOxygenUpperOne", value = "仓间氧气上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseOxygenUpperTwo", value = "仓间氧气上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseOxygenUpperThree", value = "仓间氧气上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseOxygenLower", value = "仓间氧气下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseMoistureUpperOne", value = "仓间水分上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseMoistureUpperTwo", value = "仓间水分上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseMoistureUpperThree", value = "仓间水分上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseMoistureLower", value = "仓间水分下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseTempUpperOne", value = "仓间温度上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseTempUpperTwo", value = "仓间温度上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseTempUpperThree", value = "仓间温度上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseTempLower", value = "仓间温度下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "pidRoute", value = "位置路由",required = true,dataType ="String"),
            @ApiImplicitParam(name = "stackId", value = "位置id",required = true,dataType ="Integer")
    })
    @ApiOperation(value = "添加仓间预警规则成功")
    @PreAuthorize("hasAnyAuthority('f:wa:add')")
    public Result insertWareAreaAlarmSetting(@ApiIgnore WareAreaAlarmSettingDto wareAreaAlarmSettingDto) {
        return wareAreaAlarmSettingService.insertWareAreaAlarmSetting(wareAreaAlarmSettingDto);
    }

    @PostMapping(value = "/deleteWareAreaAlarmSetting")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "仓间预警规则ids", required = true,dataType = "List")
    })
    @ApiOperation(value = "删除仓间预警规则")
    @PreAuthorize("hasAnyAuthority('f:wa:del')")
    public Result deleteWareAreaAlarmSetting(@RequestParam("ids") List<Integer> ids){
        Result result = wareAreaAlarmSettingService.deleteWareAreaAlarmSetting(ids);
        return result;
    }

    @PostMapping(value = "/findWareAreaAlarmSettingInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pidRoute", value = "位置路由",required = false,dataType ="String"),
            @ApiImplicitParam(name = "stackId", value = "位置id",required = false,dataType ="int"),
            @ApiImplicitParam(name = "createUser", value = "制定人",required = false,dataType ="String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间",required = false,dataType ="String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间",required = false,dataType ="String")
    })
    @ApiOperation(value = "查询仓间预警规则")
    public Result findWareAreaAlarmSettingInfo(@ApiIgnore WareAreaAlarmSettingDto wareAreaAlarmSettingDto) {
        PageInfo<WareAreaAlarmSettingVo> wareAreaAlarmSettingInfo = wareAreaAlarmSettingService.findWareAreaAlarmSettingInfo(wareAreaAlarmSettingDto);
        return new Result<>(true, StatusCode.OK, "查询仓间预警规则成功!",wareAreaAlarmSettingInfo);
    }

    @PostMapping(value = "/findWareAreaAlarmSettingById")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "仓间预警规则id",required = true,dataType ="Integer"),
            @ApiImplicitParam(name = "siteName", value = "位置名称",required = true,dataType ="String")
    })
    @ApiOperation(value = "修改前回显")
    public Result findWareAreaAlarmSettingById(@ApiIgnore WareAreaAlarmSettingDto wareAreaAlarmSettingDto) {
        Result result = wareAreaAlarmSettingService.findWareAreaAlarmSettingById(wareAreaAlarmSettingDto);
        return result;
    }

    @PostMapping(value = "/updateWareAreaAlarmSetting")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "仓间预警规则id",required = true,dataType ="Integer"),
            @ApiImplicitParam(name = "warehouseOxygenUpperOne", value = "仓间氧气上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseOxygenUpperTwo", value = "仓间氧气上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseOxygenUpperThree", value = "仓间氧气上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseOxygenLower", value = "仓间氧气下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseMoistureUpperOne", value = "仓间水分上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseMoistureUpperTwo", value = "仓间水分上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseMoistureUpperThree", value = "仓间水分上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseMoistureLower", value = "仓间水分下限",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseTempUpperOne", value = "仓间温度上限一级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseTempUpperTwo", value = "仓间温度上限二级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseTempUpperThree", value = "仓间温度上限三级",required = true,dataType ="double"),
            @ApiImplicitParam(name = "warehouseTempLower", value = "仓间温度下限",required = true,dataType ="double")
    })
    @ApiOperation(value = "修改仓间预警规则成功")
    @PreAuthorize("hasAnyAuthority('f:wa:up')")
    public Result updateWareAreaAlarmSetting(WareAreaAlarmSettingDto wareAreaAlarmSettingDto) {
        return wareAreaAlarmSettingService.updateWareAreaAlarmSetting(wareAreaAlarmSettingDto);
    }

    @PostMapping(value = "/findLogData")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "alarmValueSettingId", value = "仓间预警id", required = false,dataType = "Integer"),
            @ApiImplicitParam(name = "size", value = "每页数量,默认为10", required = false,dataType = "Integer"),
            @ApiImplicitParam(name = "page", value = "当前页，默认为第一页", required = false,dataType = "Integer")
    })
    @ApiOperation(value = "查询仓间操作日志")
    public Result findLogData(LogAlarmSettingDto logAlarmSettingDto) {
        return wareAreaAlarmSettingService.findLogData(logAlarmSettingDto);
    }
}
