package com.rpzh.controller;


import com.github.pagehelper.PageInfo;
import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.dto.WareAreaAlarmSettingDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.service.AlarmValueSettingService;
import com.rpzh.service.WareAreaAlarmSettingService;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.WareAreaAlarmSettingVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.redis.core.RedisTemplate;
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
@RequestMapping("/maintain/wareAreaAlarmSetting")
@Api(value = "仓间预警规则类", tags = "仓间预警规则类", description = "仓间预警规则类")
@Slf4j
public class WareAreaAlarmSettingController {
    @Resource
    private WareAreaAlarmSettingService wareAreaAlarmSettingService;

    @PostMapping(value = "/insertWareAreaAlarmSetting")
    @ApiImplicitParams({
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
            @ApiImplicitParam(name = "level", value = "位置等级",required = true,dataType ="Integer"),
            @ApiImplicitParam(name = "pidRoute", value = "位置路由",required = true,dataType ="String"),
            @ApiImplicitParam(name = "stackId", value = "位置id",required = true,dataType ="Integer")
    })
    @ApiOperation(value = "添加仓间预警规则成功")
    public Result insertWareAreaAlarmSetting(@ApiIgnore WareAreaAlarmSettingDto wareAreaAlarmSettingDto) {
        wareAreaAlarmSettingService.insertWareAreaAlarmSetting(wareAreaAlarmSettingDto);
        return new Result<>(true, StatusCode.OK, "添加仓间预警规则成功!");
    }

    @PostMapping(value = "/deleteWareAreaAlarmSetting")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "仓间预警规则ids", required = true,dataType = "List")
    })
    @ApiOperation(value = "删除仓间预警规则")
    public Result deleteWareAreaAlarmSetting(@RequestParam("ids") List<Integer> ids){
        Result result = wareAreaAlarmSettingService.deleteWareAreaAlarmSetting(ids);
        return result;
    }

    @PostMapping(value = "/findWareAreaAlarmSettingInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pidRoute", value = "位置路由",required = true,dataType ="String"),
            @ApiImplicitParam(name = "createUser", value = "制定人",required = true,dataType ="String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间",required = true,dataType ="String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间",required = true,dataType ="String")
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
            @ApiImplicitParam(name = "level", value = "位置等级",required = true,dataType ="Integer"),
            @ApiImplicitParam(name = "pidRoute", value = "位置路由",required = true,dataType ="String"),
            @ApiImplicitParam(name = "stackId", value = "位置id",required = true,dataType ="Integer")
    })
    @ApiOperation(value = "修改仓间预警规则成功")
    public Result updateWareAreaAlarmSetting(WareAreaAlarmSettingDto wareAreaAlarmSettingDto) {
        wareAreaAlarmSettingService.updateWareAreaAlarmSetting(wareAreaAlarmSettingDto);
        return new Result<>(true, StatusCode.OK, "修改仓间预警规则成功!");
    }
}
