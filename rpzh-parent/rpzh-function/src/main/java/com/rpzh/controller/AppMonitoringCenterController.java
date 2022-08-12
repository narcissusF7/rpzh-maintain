package com.rpzh.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.constant.NodeConstant;
import com.rpzh.dto.NodeDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.MonitoringCenterService;
import com.rpzh.service.NodeService;
import com.rpzh.util.ManuallyPageUtil;
import com.rpzh.util.MonitorCondition;
import com.rpzh.vo.NodeDataVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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

    @ApiOperation(
            value = "查询设备历史数据"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "nodeId",
            value = "设备id",
            dataType = "String",
            required = true
    ), @ApiImplicitParam(
            name = "nodeType",
            value = "设备类型 01氧气 03温湿度 04 包芯氧气",
            dataType = "String",
            required = true
    ), @ApiImplicitParam(
            name = "page",
            value = "当前页数",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "size",
            value = "每页数量",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "startTime",
            value = "开始时间",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "endTime",
            value = "结束时间",
            dataType = "String"
    )})
    @GetMapping({"/findNodeHistory"})
    public Result<List<NodeDataVo>> findNodeHistory(@ApiIgnore NodeDto nodeDto) {
        if (ToolUtil.isEmpty(nodeDto.getNodeId())){
            return new Result<>(false, StatusCode.ERROR, "设备id不能为空");
        }

        if (ToolUtil.isEmpty(nodeDto.getNodeType())){
            return new Result<>(false, StatusCode.ERROR, "设备类型不能为空");
        }
        List<NodeDataVo> nodeDataVos = nodeService.findNodeHistory(nodeDto);
        if (!ToolUtil.isEmpty(nodeDto.getPage())&&!ToolUtil.isEmpty(nodeDto.getSize())){
            int page = nodeDto.getPage();
            int size = nodeDto.getSize();
            Page<NodeDataVo> nodeDataPage = ManuallyPageUtil.manuallyPage(nodeDataVos, page, size);
            return new Result<>(true, StatusCode.OK, "查询成功", new PageInfo<>(nodeDataPage));
        }
        if (ToolUtil.isEmpty(nodeDataVos)){
            return new Result<>(true, StatusCode.OK, "查询成功",null);
        }
        return new Result<>(true, StatusCode.OK, "查询成功", new PageInfo<>(nodeDataVos));
    }

    @ApiOperation(
            value = "查询设备历史数据(日数据)"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "nodeId",
            value = "设备id",
            dataType = "String",
            required = true
    ), @ApiImplicitParam(
            name = "nodeType",
            value = "设备类型  01氧气 03温湿度 04 包芯氧气",
            dataType = "String",
            required = true
    ), @ApiImplicitParam(
            name = "dataType",
            value = "数据类型(温湿度湿度数据 传4 其他随意)",
            dataType = "Integer",
            required = true
    ), @ApiImplicitParam(
            name = "startTime",
            value = "开始时间",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "endTime",
            value = "结束时间",
            dataType = "String"
    )})
    @GetMapping({"/findNodeHistoryGraph"})
    public Result<Map<String, HashMap<String, String>>> findNodeHistoryGraph(@ApiIgnore NodeDto nodeDto) {
        if (ToolUtil.isEmpty(nodeDto.getNodeId())){
            return new Result<>(false, StatusCode.ERROR, "设备id不能为空");
        }

        if (ToolUtil.isEmpty(nodeDto.getNodeType())){
            return new Result<>(false, StatusCode.ERROR, "设备类型不能为空");
        }

        //如果数据类型为空 设成0
        if (ToolUtil.isEmpty(nodeDto.getDataType())){
            nodeDto.setDataType(0);
        }


        List<NodeDataVo> nodeDataVos = nodeService.findNodeHistory(nodeDto);
        if (!ToolUtil.isEmpty(nodeDataVos)){
            Map<String, List<NodeDataVo>> collect = nodeDataVos.stream().collect(Collectors.groupingBy((item) -> (
                    item.getTransferTime().split(" ")[0])));
            List<HashMap<String, String>> historyGraphData = new ArrayList<>();
            TreeMap<String, List<NodeDataVo>> treeMap = new TreeMap<>(collect);
            treeMap.forEach((k, value) -> {
                HashMap<String, String> map = new HashMap();
                map.put("max", value.stream().map(NodeDataVo::getData).max(Double::compareTo).get().toString());
                map.put("min", value.stream().map(NodeDataVo::getData).min(Double::compareTo).get().toString());
                map.put("avg", value.stream().mapToDouble(NodeDataVo::getData).average().getAsDouble()+"");
                map.put("date",k);
                if (!ToolUtil.isEmpty(nodeDto.getNodeType())&& NodeConstant.HUMITURE_NODE_TYPE_VALUE.equals(nodeDto.getNodeType())) {
                    map.put("humitureMax", value.stream().map(NodeDataVo::getHumiture).max(Double::compareTo).get().toString());
                    map.put("humitureMin", value.stream().map(NodeDataVo::getHumiture).min(Double::compareTo).get().toString());
                    map.put("humitureAvg", value.stream().mapToDouble(NodeDataVo::getHumiture).average().getAsDouble()+"");
                    map.put("humitureDate",k);
                }
                historyGraphData.add(map);
            });
            return new Result<>(true, StatusCode.OK, "查询成功", historyGraphData);
        }
        return new Result<>(true, StatusCode.OK, "", null);
    }

    @ApiOperation(
            value = "查询任务"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "siteRoute",
            value = "位置路由",
            dataType = "String",
            required = true
    ), @ApiImplicitParam(
            name = "nodeType",
            value = "设备类型  01氧气 03温湿度 04 包芯氧气 P1垛内虫情 P2仓间虫情",
            dataType = "String",
            required = true
    )})
    @PostMapping("findTaskFromMonitored")
    public Result findTask(String siteRoute,String nodeType){
        return nodeService.findTask(siteRoute,nodeType);
    }


}
