package com.rpzh.controller;


import com.rpzh.dto.DataContrastDto;
import com.rpzh.dto.NodeDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.pojo.NodeData;
import com.rpzh.service.DataContrastService;
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
import java.util.List;


/**
 * @Author: zyq
 * @Date: 2022/06/23/13:32
 * @Description: 数据对比
 */
@RestController
@CrossOrigin
@RequestMapping("/maintain/dataContrast")
@Api(value = "数据对比类", tags = "数据对比类", description = "数据对比类")
public class DataContrastController {

    @Resource
    private DataContrastService dataContrastService;
    @PostMapping("/getNodeData")
    @ApiOperation(value = "数据对比中的条件选择设备")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "materialCode", value = "物料等级", required = false, dataType = "String"),
            @ApiImplicitParam(name = "siteRoute", value = "位置路由", required = false, dataType = "int"),
            @ApiImplicitParam(name = "stackId", value = "位置id", required = false, dataType = "int"),
            @ApiImplicitParam(name = "nodeType", value = "设备类型 01:氧气 02:包芯温度 03:环境温度 04:环境湿度", required = true, dataType = "String"),
            @ApiImplicitParam(name = "contrastType", value = "对比的类型:1:设备  2:位置", required = true, dataType = "int"),
            @ApiImplicitParam(name = "flag", value = "是否重置:0:是 1:否", required = true, dataType = "int")
    })
    public Result getNodeData(@ApiIgnore NodeDto nodeDto) {
        return dataContrastService.getNodeData(nodeDto);
    }

    @PostMapping("/getNodeDataList")
    @ApiOperation(value = "数据对比")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nodeDatas", value = "选择设备,NodeData包含nodeId、type、pidRoute、nodeName、siteName", required = true, dataType = "List<NodeData>"),
            @ApiImplicitParam(name = "startTime", value = "采集的开始时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "采集的结束时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "beginTime", value = "对比的开始时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "finishTime", value = "对比的结束时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "gatherType", value = "采集的时间类型 1:天  2:周  3:月  4:年    默认月", required = false, dataType = "String"),
            @ApiImplicitParam(name = "contrastType", value = "对比的时间类型 1:天  2:周  3:月  4:年    默认天", required = false, dataType = "String")
    })
    public Result getNodeDateList(@ApiIgnore DataContrastDto dataContrastDto){
        return dataContrastService.getNodeDateList(dataContrastDto);
    }
}
