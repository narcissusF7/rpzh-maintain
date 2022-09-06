package com.rpzh.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.constant.NodeConstant;
import com.rpzh.dto.NodeDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.enums.NodeTypeEnum;
import com.rpzh.pojo.NodeData;
import com.rpzh.service.MonitoringCenterService;
import com.rpzh.service.NodeService;
import com.rpzh.util.EasyPoiUtil;
import com.rpzh.util.ManuallyPageUtil;
import com.rpzh.util.MonitorCondition;
import com.rpzh.vo.EarlyWarningVo;
import com.rpzh.vo.NodeDataVo;
import com.rpzh.vo.NodeVo;
import com.rpzh.vo.WeatherDataVo;
import io.swagger.annotations.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 监控中心控制类
 *
 * @Author liujj
 * @create 2022/5/31
 */
@RestController
@RequestMapping({"/maintain/monitoringCenter"})
@CrossOrigin
@Api(value = "监控中心控制类", tags = "监控中心控制类")
public class MonitoringCenterController {
    @Resource
    MonitoringCenterService monitoringCenterService;
    @Resource
    NodeService nodeService;

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
            value = "排序规则 0 按位置排序 1 按预警等级排序",
            dataType = "Integer",
            defaultValue = "0"
    ), @ApiImplicitParam(
            name = "pidRoute",
            value = "位置路由",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "siteId",
            value = "所选的位置id",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "nodeTypes",
            value = "设备类型数组 查询全部 包芯监控传输04（包温） 05（包水）  垛内/仓间监控传其他01（氧气）,02（二氧化碳）,03（环境温湿度）,06（磷化氢）",
            dataType = "String",
            required = true,
            allowMultiple = true
    ), @ApiImplicitParam(
            name = "monitorStatus",
            value = "监控状态 0 全部 1预警 2 正常",
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
            value = "查询故障设备列表 01:设备电量故障,02:设备信号强度故障,03:设备丢包故障"
    )
    @GetMapping("/findTroubleNode")
    public Result<EarlyWarningVo> findTroubleNode() {
        List<EarlyWarningVo> earlyWarningVos = monitoringCenterService.findTroubleNode();
        return new Result<>(true, StatusCode.OK, "查询成功", earlyWarningVos);
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
            value = "设备类型  01氧气 03温湿度 04 包芯氧气",
            dataType = "String",
            required = true
    ),@ApiImplicitParam(
            name = "siteType",
            value = "采集位置类型",
            dataType = "String",
            required = true
    ),@ApiImplicitParam(
            name = "pidRoute",
            value = "位置路由 ",
            dataType = "String",
            required = true
    )})
    @PostMapping("/findAlarmValue")
    public Result<Map<String,String>> findAlarmValue(@ApiIgnore NodeData nodeData,String pidRoute){
        if (ToolUtil.isEmpty(nodeData.getMaintainStageId())){
            return new Result<>(false, StatusCode.ERROR, "养护阶段id不能为空");
        }
        if (ToolUtil.isEmpty(nodeData.getType())){
            return new Result<>(false, StatusCode.ERROR, "设备类型不能为空");
        }
        if (ToolUtil.isEmpty(nodeData.getSiteType())){
            return new Result<>(false, StatusCode.ERROR, "位置类型不能为空");
        }
        if (ToolUtil.isEmpty(pidRoute)){
            return new Result<>(false, StatusCode.ERROR, "位置路由不能为空");
        }
        nodeData.setSiteRoute(pidRoute);
        Map<String,String> map = monitoringCenterService.findAlarmValue(nodeData);
        return new Result<>(true,StatusCode.OK,"查询成功",map);
    }


    @ApiOperation(
            value = "查询天气历史数据"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "startTime",
            value = "开始时间",
            dataType = "String"
    ),@ApiImplicitParam(
            name = "endTime",
            value = "结束时间",
            dataType = "String"
    ),@ApiImplicitParam(
            name = "page",
            value = "页数",
            dataType = "Integer",
            defaultValue = "1"
    ),@ApiImplicitParam(
            name = "size",
            value = "每页数量",
            dataType = "Integer",
            defaultValue = "10"
    )})
    @GetMapping("/findWeatherData")
    public Result<List<WeatherDataVo>> findWeatherData(@ApiIgnore MonitorCondition monitorCondition){
        PageHelper.startPage(monitorCondition.getPage(),monitorCondition.getSize());
        List<WeatherDataVo> weatherDataVos = monitoringCenterService.findWeatherData(monitorCondition);

        return new Result<>(true,StatusCode.OK,"查询成功",new PageInfo<>(weatherDataVos));
    }

    @ApiOperation(
            value = "查询天气历史数据(日数据)"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "startTime",
            value = "开始时间",
            dataType = "String"
    ),@ApiImplicitParam(
            name = "endTime",
            value = "结束时间",
            dataType = "String"
    )})
    @GetMapping("/findWeatherDataByDay")
    public Result<List<HashMap<String, Double>>> findWeatherDataByDay(@ApiIgnore MonitorCondition monitorCondition){
        List<HashMap<String, String>> weatherDataVos = monitoringCenterService.findWeatherDataByDay(monitorCondition);
        return new Result<>(true,StatusCode.OK,"查询成功",weatherDataVos);
    }
    @ApiOperation(
            value = "查询现在天气数据"
    )
    @GetMapping("/findNowWeather")
    public Result<WeatherDataVo> findNowWeather(){
        WeatherDataVo nowWeather = monitoringCenterService.findNowWeather();
        return new Result<>(true,StatusCode.OK,"查询成功",nowWeather);
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
            name = "maintainPlanId",
            value = "养护计划id",
            dataType = "Integer",
            required = true
    ),@ApiImplicitParam(
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
                if (!ToolUtil.isEmpty(nodeDto.getNodeType())&&NodeConstant.HUMITURE_NODE_TYPE_VALUE.equals(nodeDto.getNodeType())) {
                    map.put("humitureMax", value.stream().map(NodeDataVo::getHumiture).max(Double::compareTo).get().toString());
                    map.put("humitureMin", value.stream().map(NodeDataVo::getHumiture).min(Double::compareTo).get().toString());
                    map.put("humitureAvg", String.format("%.2f", value.stream().mapToDouble(NodeDataVo::getHumiture).average().getAsDouble()));
                    map.put("humitureDate",k);
                }
                historyGraphData.add(map);
            });
            return new Result<>(true, StatusCode.OK, "查询成功", historyGraphData);
        }
        return new Result<>(true, StatusCode.OK, "", null);
    }

    @ApiOperation(
            value = "导出历史数据"
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
    ),@ApiImplicitParam(
            name = "startTime",
            value = "开始时间",
            dataType = "String"
    ),@ApiImplicitParam(
            name = "endTime",
            value = "结束时间",
            dataType = "String"
    )})
    @GetMapping({"/exportNodeHistory"})
    public Result exportNodeHistory(@ApiIgnore NodeDto nodeDto, HttpServletResponse response) {
        if (ToolUtil.isEmpty(nodeDto.getNodeId())){
            return new Result<>(false, StatusCode.ERROR, "设备id不能为空");
        }

        if (ToolUtil.isEmpty(nodeDto.getNodeType())){
            return new Result<>(false, StatusCode.ERROR, "设备类型不能为空");
        }

        List<NodeDataVo> nodeHistory = nodeService.findNodeHistory(nodeDto);
        EasyPoiUtil<NodeDataVo> easyPoiUtil = new EasyPoiUtil();
        List<String> columns = new ArrayList<>();
        //把要导出的字段添加到集合中
        if (!ToolUtil.isEmpty(nodeDto.getNodeType())) {
            columns.add(NodeDataVo.getColumn(nodeDto.getNodeType()));
            if (NodeConstant.HUMITURE_NODE_TYPE_VALUE.equals(nodeDto.getNodeType())) {
                columns.add("humiture");
            }
        }
        //显示要导出的字段
        easyPoiUtil.t = nodeHistory.get(0);
        columns.forEach((column) -> {
            try {
                easyPoiUtil.hihdColumn(column, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        String excelType = NodeTypeEnum.getNodeType(nodeDto.getNodeType()).getName() + nodeDto.getNodeId() + ".xls";
        columns.forEach((column) -> {
            try {
                easyPoiUtil.hihdColumn(column, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        EasyPoiUtil.exportExcel(nodeHistory, nodeDto.getNodeId(), nodeDto.getNodeId(), NodeDataVo.class, excelType, true, response);

        return new Result(true,StatusCode.OK,"导出成功");
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

