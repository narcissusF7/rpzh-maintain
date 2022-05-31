package com.rpzh.controller;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.pojo.NodeData;
import com.rpzh.service.MonitoringCenterService;
import com.rpzh.util.MonitorCondition;
import com.rpzh.vo.EarlyWarningVo;
import com.rpzh.vo.NodeDataVo;
import com.rpzh.vo.NodeVo;
import com.rpzh.vo.WeatherDataVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/maintain/monitoringCenter"})
@CrossOrigin
@Api(value = "监控中心控制类", tags = "监控中心控制类")
public class MonitoringCenterController {
    @Resource
    MonitoringCenterService monitoringCenterService;

    @ApiOperation(
            value = "查询监控数据列表"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "siteType",
            value = "位置类型 04:仓间,05非仓间",
            dataType = "String",
            required = true
    ), @ApiImplicitParam(
            name = "materialCode",
            value = "物料代码",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "sortCondition",
            value = "排序规则",
            dataType = "Integer",
            defaultValue = "0"
    ), @ApiImplicitParam(
            name = "gatherSite",
            value = "采集位置",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "nodeTypes",
            value = "设备类型数组 查询全部 包芯监控传输04（包温） 05（包水）  垛内/仓间监控传其他01（氧气）,02（二氧化碳）,03（环境温湿度）,06（磷化氢）",
            dataType = "String",
            required = true,
            allowMultiple = true
    ), @ApiImplicitParam(
            name = "monitorStatus",
            value = "监控状态",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "page",
            value = "当前页数",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "size",
            value = "每页数量",
            dataType = "Integer"
    )})
    @GetMapping({"/findMonitoringData"})
    public Result<NodeDataVo> findMonitoringData(@ApiIgnore MonitorCondition monitorCondition) {
        PageInfo<NodeDataVo> pageInfo = monitoringCenterService.findMonitoringData(monitorCondition);
        return new Result(true, StatusCode.OK, "查询成功", pageInfo);
    }

    @ApiOperation(
            value = "查询故障设备列表 01:设备电量故障,02:设备信号强度故障,03:设备丢包故障,"
    )
    @GetMapping("/findTroubleNode")
    public Result<EarlyWarningVo> findTroubleNode() {
        List<EarlyWarningVo> earlyWarningVos = monitoringCenterService.findTroubleNode();
        return new Result<>(true, StatusCode.OK, "查询成功", earlyWarningVos);
    }

    @ApiOperation(
            value = "修改预警升级阈值"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "oneToTwo",
            value = "一级升二级时间阈值",
            dataType = "Integer"
    ),@ApiImplicitParam(
            name = "twoToThree",
            value = "二级升三级级时间阈值",
            dataType = "Integer"
    )})
    @PostMapping({"/updateWarningThreshold"})
    public Result updateWarningThreshold(Integer oneToTwo ,Integer twoToThree) {
        Map<String,Integer> map = new HashMap<>();
        map.put("oneToTwo",oneToTwo);
        map.put("twoToThree",twoToThree);
        Integer flag = monitoringCenterService.updateWarningThreshold(map);
        return flag > 0 ? new Result(true, StatusCode.OK, "修改成功") : new Result(false, StatusCode.ERROR, "修改失败");
    }

    @ApiOperation(
            value = "查询预警升级阈值"
    )
    @GetMapping({"/findWarningThreshold"})
    public Result findWarningThreshold() {
        Map<String, String> thresholdMap = monitoringCenterService.findWarningThreshold();
        return thresholdMap.size() > 0 ? new Result(true, StatusCode.OK, "查询成功", thresholdMap) : new Result(false, StatusCode.ERROR, "查询失败");
    }


    @ApiOperation(
            value = "查询设备的预警阈值"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "maintainStageId",
            value = "养护计划阶段Id",
            dataType = "Integer",
            required = true
    ),@ApiImplicitParam(
            name = "type",
            value = "设备类型",
            dataType = "String",
            required = true
    ),@ApiImplicitParam(
            name = "siteType",
            value = "采集位置类型",
            dataType = "String",
            required = true
    )})
    @GetMapping("/findAlarmValue")
    public Result<Double> findAlarmValue(NodeData nodeData){
        List<Double> arrayList = monitoringCenterService.findAlarmValue(nodeData);
        return new Result<>(true,StatusCode.OK,"查询成功",arrayList);
    }


    @ApiOperation(
            value = "查询天气历史数据"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "startTime",
            value = "开始时间 默认值当天0点",
            dataType = "String"
    ),@ApiImplicitParam(
            name = "endTime",
            value = "结束时间",
            dataType = "String"
    ),@ApiImplicitParam(
            name = "page",
            value = "页数  默认第一页",
            dataType = "Integer",
            defaultValue = "1"
    ),@ApiImplicitParam(
            name = "size",
            value = "每页数量 默认10条",
            dataType = "Integer",
            defaultValue = "10"
    )})
    @GetMapping("/findWeatherData")
    public Result<List<WeatherDataVo>> findWeatherData(MonitorCondition monitorCondition){
        PageHelper.startPage(monitorCondition.getPage(),monitorCondition.getSize());
        List<WeatherDataVo> weatherDataVos = monitoringCenterService.findWeatherData(monitorCondition);
        return new Result<>(true,StatusCode.OK,"查询成功",new PageInfo<>(weatherDataVos));
    }

    @ApiOperation(
            value = "查询天气历史数据(日数据)"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "startTime",
            value = "开始时间 默认值一星期前",
            dataType = "String"
    ),@ApiImplicitParam(
            name = "endTime",
            value = "结束时间 默认值为当天",
            dataType = "String"
    ),@ApiImplicitParam(
            name = "page",
            value = "页数  默认第一页",
            dataType = "Integer",
            defaultValue = "1"
    ),@ApiImplicitParam(
            name = "size",
            value = "每页数量 默认10条",
            dataType = "Integer",
            defaultValue = "10"
    )})
    @GetMapping("/findWeatherDataByDay")
    public Result<Map<String, HashMap<String, Double>>> findWeatherDataByDay(MonitorCondition monitorCondition){
        Map<String, HashMap<String, Double>> weatherDataVos = monitoringCenterService.findWeatherDataByDay(monitorCondition);
        return new Result<>(true,StatusCode.OK,"查询成功",weatherDataVos);
    }


}

