package com.rpzh.controller;


import com.github.pagehelper.PageInfo;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.service.MonitoringCenterService;
import com.rpzh.util.MonitorCondition;
import com.rpzh.vo.NodeDataVo;
import com.rpzh.vo.NodeVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.HashMap;
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
            value = "位置类型04:仓间,05非仓间",
            dataType = "String",
            required = true
    ),@ApiImplicitParam(
            name = "stackId",
            value = "垛位id",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "wareAreaId",
            value = "区域id",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "floorId",
            value = "楼层Id",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "buildingId",
            value = "楼栋id",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "wareHouseId",
            value = "仓库Id",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "materialCode",
            value = "物料代码",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "materialName",
            value = "物料名称",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "sortCondition",
            value = "排序规则",
            dataType = "Integer",
            defaultValue = "0"
    ), @ApiImplicitParam(
            name = "nodeTypes",
            value = "设备类型数组 查询全部 包芯监控传输03 04  垛内监控传其他01,02,05,06",
            dataType = "String",
            required = true,
            allowMultiple = true,
            defaultValue = "01"
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

    public Result findTroubleNode() {
        PageInfo<NodeVo> pageInfo = monitoringCenterService.findTroubleNode();
        return null;
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
}

