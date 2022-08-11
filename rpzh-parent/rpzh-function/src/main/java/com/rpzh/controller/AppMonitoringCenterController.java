package com.rpzh.controller;


import com.github.pagehelper.PageInfo;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.MonitoringCenterService;
import com.rpzh.service.NodeService;
import com.rpzh.util.MonitorCondition;
import com.rpzh.vo.NodeDataVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;

@RestController
@RequestMapping({"/maintain/appMonitoringCenter"})
@CrossOrigin
@Api(value = "app监控中心控制类", tags = "app监控中心控制类")
public class AppMonitoringCenterController {

    @Resource
    MonitoringCenterService monitoringCenterService;
    @Resource
    NodeService nodeService;

    @ApiOperation(
            value = "查询监控数据列表"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
            name = "pidRoute",
            value = "位置路由",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "siteId",
            value = "所选的位置id",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "nodeTypes",
            value = "设备类型 包芯监控传输04（包温）垛内/仓间监控传其他01（氧气）,03（环境温湿度）",
            dataType = "String",
            required = true,
            allowMultiple = true
    ), @ApiImplicitParam(
            name = "page",
            value = "当前页数",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "size",
            value = "每页数量",
            dataType = "Integer"
    )})
    @PostMapping({"/findMonitoringData"})
    public Result<NodeDataVo> findMonitoringData(@ApiIgnore MonitorCondition monitorCondition) {
        if (ToolUtil.isEmpty(monitorCondition.getSiteType())){
            return new Result<>(false, StatusCode.ERROR, "位置类型不能为空");
        }
        if (ToolUtil.isEmpty(monitorCondition.getNodeTypes())){
            return new Result<>(false, StatusCode.ERROR, "设备类型数组不能为空");
        }
        PageInfo<NodeDataVo> pageInfo = monitoringCenterService.findMonitoringData(monitorCondition);
        return new Result<>(true, StatusCode.OK, "查询成功", pageInfo);
    }
}
