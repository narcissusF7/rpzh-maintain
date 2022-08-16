package com.rpzh.controller;


import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.constant.NodeConstant;
import com.rpzh.dto.NodeDto;
import com.rpzh.dto.PestDataDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.pojo.NodeData;
import com.rpzh.service.MonitoringCenterService;
import com.rpzh.service.NodeService;
import com.rpzh.service.PestMonitoringService;
import com.rpzh.service.WarehouseService;
import com.rpzh.util.ManuallyPageUtil;
import com.rpzh.util.MonitorCondition;
import com.rpzh.vo.NodeDataVo;
import com.rpzh.vo.PestDataVo;
import com.rpzh.vo.StackVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author ljj
 * @description [app养护监控控制层]
 */

@RestController
@RequestMapping({"/maintain/appMonitoringCenter"})
@CrossOrigin
@Api(value = "app监控中心控制类", tags = "app监控中心控制类")
public class AppMonitoringCenterController {

    @Resource
    MonitoringCenterService monitoringCenterService;
    @Resource
    NodeService nodeService;

    @Resource
    PestMonitoringService pestMonitoringService;

    @Resource
    WarehouseService warehouseService;

    @ApiOperation(value = "查询监控数据列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pidRoute", value = "位置路由", dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "所选的位置id", dataType = "Integer"),
            @ApiImplicitParam(name = "nodeTypes", value = "设备类型 包芯监控传输04（包温）垛内/仓间监控传其他01（氧气）,03（环境温湿度）", dataType = "String", required = true, allowMultiple = true),
            @ApiImplicitParam(name = "page", value = "当前页数", dataType = "Integer"),
            @ApiImplicitParam(name = "size", value = "每页数量", dataType = "Integer")})
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

    @ApiOperation(value = "查询设备历史数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nodeId", value = "设备id", dataType = "String", required = true),
            @ApiImplicitParam(name = "nodeType", value = "设备类型 01氧气 03温湿度 04 包芯氧气", dataType = "String", required = true),
            @ApiImplicitParam(name = "page", value = "当前页数", dataType = "Integer"),
            @ApiImplicitParam(name = "size", value = "每页数量", dataType = "Integer"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "String")})
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
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nodeId", value = "设备id", dataType = "String", required = true),
            @ApiImplicitParam(name = "nodeType", value = "设备类型  01氧气 03温湿度 04 包芯氧气", dataType = "String", required = true),
            @ApiImplicitParam(name = "dataType", value = "数据类型(温湿度湿度数据 传4 其他随意)", dataType = "Integer", required = true),
            @ApiImplicitParam(name = "startTime", value = "开始时间", dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "String")})
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
    @ApiImplicitParams({
            @ApiImplicitParam(name = "siteRoute", value = "位置路由", dataType = "String", required = true),
            @ApiImplicitParam(name = "nodeType", value = "设备类型  01氧气 03温湿度 04 包芯氧气 P1垛内虫情 P2仓间虫情", dataType = "String", required = true)})
    @PostMapping("findTaskFromMonitored")
    public Result findTask(String siteRoute,String nodeType){
        return nodeService.findTask(siteRoute,nodeType);
    }


    @ApiOperation("虫情数据实时监控")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "monitorStatus", value = "监控状态 0 全部 1 预警 2 正常", dataType = "Integer"),
            @ApiImplicitParam(name = "pidRoute", value = "位置路由", dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "位置id", dataType = "Integer"),
            @ApiImplicitParam(name = "siteType", value = "位置类型 04仓间05垛位", dataType = "String"),
            @ApiImplicitParam(name = "page", value = "当前页", dataType = "Integer"),
            @ApiImplicitParam(name = "size", value = "每页长度", dataType = "Integer")
    })
    @PostMapping("/findPestDataHistory")
    public Result<List<PestDataVo>>  findPestDataHistory(@ApiIgnore MonitorCondition monitorCondition){
        PageInfo<PestDataVo> pageInfo = pestMonitoringService.findPestDataHistory(monitorCondition);
        return new Result<>(true,StatusCode.OK,"查询成功",pageInfo);
    }


    @ApiOperation("查询单个点位虫情数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pestBoardId", value = "虫情点位id", dataType = "String" ,required = true),
            @ApiImplicitParam(name = "startTime", value = "开始时间", dataType = "String"),
            @ApiImplicitParam(name = "siteType", value = "位置类型 04仓间05垛位", dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "String"),
            @ApiImplicitParam(name = "page", value = "当前页", dataType = "Integer"),
            @ApiImplicitParam(name = "size", value = "每页长度", dataType = "Integer")
    })
    @GetMapping("/findPestDataHistoryByOne")
    public Result<List<PestDataVo>> findPestDataHistoryByOne(@ApiIgnore PestDataDto pestDataDto){
        if (ToolUtil.isEmpty(pestDataDto.getPestBoardId())){
            return new Result(false,StatusCode.ERROR,"虫情点位id不能为空");
        }
        if (!ToolUtil.isEmpty(pestDataDto.getPage())&&!ToolUtil.isEmpty(pestDataDto.getSize())){
            //分页
            PageHelper.startPage(pestDataDto.getPage(),pestDataDto.getSize());
        }
        List<PestDataVo> pestDataVos = pestMonitoringService.findPestDataHistoryByOne(pestDataDto);
        return new Result<>(true,StatusCode.OK,"查询成功",new PageInfo<>(pestDataVos));
    }

    @ApiOperation(value = "查询设备的预警阈值")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "maintainStageId", value = "养护计划阶段Id", dataType = "Integer", required = true),
            @ApiImplicitParam(name = "type", value = "设备类型  01氧气 03温湿度 04 包芯氧气", dataType = "String", required = true),
            @ApiImplicitParam(name = "siteType", value = "采集位置类型", dataType = "String", required = true),
            @ApiImplicitParam(name = "pidRoute", value = "位置路由 ", dataType = "String", required = true)})
    @PostMapping("/findAlarmValue")
    public Result<Map<String,String>> findAlarmValue(@ApiIgnore NodeData nodeData){
        if (ToolUtil.isEmpty(nodeData.getMaintainStageId())){
            return new Result<>(false, StatusCode.ERROR, "养护阶段id不能为空");
        }
        if (ToolUtil.isEmpty(nodeData.getType())){
            return new Result<>(false, StatusCode.ERROR, "设备类型不能为空");
        }
        if (ToolUtil.isEmpty(nodeData.getSiteType())){
            return new Result<>(false, StatusCode.ERROR, "位置类型不能为空");
        }
        if (ToolUtil.isEmpty(nodeData.getSiteRoute())){
            return new Result<>(false, StatusCode.ERROR, "位置路由不能为空");
        }
        Map<String,String> map = monitoringCenterService.findAlarmValue(nodeData);
        return new Result<>(true,StatusCode.OK,"查询成功",map);
    }
    @ApiOperation("查询虫情点位预警值")
    @GetMapping("/findPestWarningValue")
    @ApiImplicitParam(name = "pestBoardId", value = "虫情点位id", dataType = "String", required = true)
    public Result<Map<String,Object>> findPestWarningValue(String pestBoardId){
        if (ToolUtil.isEmpty(pestBoardId)){
            return new Result(false,StatusCode.ERROR,"虫情点位id不能为空");
        }
        Map<String,Object> map = pestMonitoringService.findPestWarningValue(pestBoardId);
        return new Result(true,StatusCode.OK,"查询成功",map);
    }

    @ApiOperation("查询位置负责人")
    @GetMapping("/findSitePrincipal")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "siteId", value = "位置id", dataType = "Integer", required = true),
            @ApiImplicitParam(name = "pidRoute", value = "位置路由 ", dataType = "String", required = true)})
    public Result<Map<String, Object>> findSitePrincipal(String pidRoute,Integer siteId){
        if (ToolUtil.isEmpty(pidRoute)){
            return new Result(false,StatusCode.ERROR,"位置路由不能为空");
        }
        if (ToolUtil.isEmpty(siteId)){
            return new Result(false,StatusCode.ERROR,"位置id不能为空");
        }
        Map<String, Object> sitePrincipal = warehouseService.getSitePrincipal(pidRoute.split(",").length, siteId);
        return new Result(true,StatusCode.OK,"查询成功",sitePrincipal);
    }

    @ApiOperation("设备入垛")
    @ApiImplicitParam(
            name = "data",
            value = "JSON字符串,可以包含多个对象 每个对象包含" +
                    "id（设备主键id 必传）,xyz（根据情况填写）,siteId（位置id 垛内设备必传）,caseNo（箱号）,pid_route(必传)，stack 位置名称，gather",
            dataType = "String"
    )
    @PostMapping("/enterStack")
    public Result enterStack(String data) {
        if (ToolUtil.isEmpty(data)) {
            return new Result<>(false, StatusCode.ERROR, "请选择一个设备");
        }
        List<Map> list = JSONObject.parseObject(data, List.class);

        for (int i = 0; i < list.size(); i++) {
            Map map = list.get(i);
            NodeDto nodeDto = new NodeDto();
            //设置主键id
            Object id = map.get("id");
            Object nodeId = map.get("nodeId");
            if (ToolUtil.isEmpty(id)||ToolUtil.isEmpty(nodeId)){
                return new Result<>(false, StatusCode.ERROR, "第"+(i+1)+"个设备非正常添加");
            }
            //查询该设备的设备类型
            nodeDto.setId((int)id);
            nodeDto.setNodeId((String) nodeId);
            String nodeType = nodeService.findNodeTypeByNodeId(nodeDto);
            if (ToolUtil.isEmpty(nodeType)){
                return new Result<>(false, StatusCode.ERROR, "第"+(i+1)+"个设备非正常添加");
            }
            nodeDto.setId(Integer.parseInt(id.toString()));

            //如果是垛内设备
            Object pidRoute = map.get("pidRoute");
            if (ToolUtil.isEmpty(pidRoute)){
                return new Result<>(false, StatusCode.ERROR, "第"+(i+1)+"个设备的入垛位置为空");
            }
            String[] split = pidRoute.toString().split(",");

            //如果时包芯设备
            if ("04".equals(nodeType)&&split.length==4){
                return new Result<>(false, StatusCode.ERROR, "包芯设备不能放置到垛位外");
            }
            if (split.length==5) {
                //设置设备所在位置的id
                if (ToolUtil.isEmpty(map.get("siteId"))){
                    return new Result<>(false, StatusCode.ERROR, "第"+(i+1)+"个设备的位置id为空");
                }
                nodeDto.setStackId(Integer.parseInt(map.get("siteId").toString()));
                nodeDto.setSiteId(Integer.parseInt(map.get("siteId").toString()));
                nodeDto.setSiteType(NodeConstant.NODE_STACK);

                if (!ToolUtil.isEmpty(map.get("x"))){
                    //设置X坐标
                    nodeDto.setSiteX(map.get("x").toString());
                }
                if (!ToolUtil.isEmpty(map.get("y"))){
                    //设置Y坐标
                    nodeDto.setSiteY(map.get("y").toString());
                }
                if (!ToolUtil.isEmpty(map.get("z"))){
                    //设置Z坐标
                    nodeDto.setSiteZ(map.get("z").toString());
                }
                //设置采集位置
                nodeDto.setGatherSite(map.get("stack").toString());
                //设置垛位名称
                nodeDto.setStack(map.get("stack").toString());

                nodeDto.setSiteRoute(map.get("pidRoute").toString()+",[05:"+map.get("stackId")+"]");

                //如果是包芯设备
                if (NodeConstant.TEMPERATURE_CORE_CHIP_NODE_TYPE_VALUE.equals(map.get("nodeType").toString())) {
                    nodeDto.setCaseNo(map.get("caseNo").toString());
                }

                //如果是仓间设备
            } else if (split.length==4) {
                //设置位置路由
                nodeDto.setSiteRoute(map.get("pidRoute").toString()+",[04:"+map.get("siteId")+"]");
                //设置采集位置
                nodeDto.setGatherSite(map.get("stack").toString());
                //设置采集位置类型
                nodeDto.setSiteType(NodeConstant.NODE_WARE_AREA);

                if (!ToolUtil.isEmpty(map.get("x"))){
                    //设置X坐标
                    nodeDto.setSiteX(map.get("x").toString());
                }
            }
            nodeService.enterStack(nodeDto);
        }

        return new Result<>(true, StatusCode.OK, "设备入垛成功");
    }

    @ApiOperation(value = "设备解垛")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids",value = "id集", dataType = "Integer",allowMultiple = true, required = true),
            @ApiImplicitParam(name = "nodeIds",value = "设备id集", dataType = "String",allowMultiple = true, required = true)
    })
    @PostMapping({"/unbindStack"})
    public Result unbindStack(@ApiIgnore NodeDto nodeDto) {
        Integer[] ids = nodeDto.getIds();
        if (ToolUtil.isEmpty(ids)) {
            return new Result<>(false, StatusCode.ERROR, "请至少选择一个设备");
        }
        int updateFlag = nodeService.unbindStack(nodeDto);
        return new Result<>(true, StatusCode.OK, "解垛成功");
    }

    @ApiOperation(value = "设备运维")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids",value = "id集", dataType = "Integer",allowMultiple = true, required = true),
            @ApiImplicitParam(name = "nodeIds",value = "设备id集", dataType = "String",allowMultiple = true, required = true)
    })
    @PostMapping({"/nodeOPS"})
    public Result nodeOPS(@ApiIgnore NodeDto nodeDto) {
        Integer[] ids = nodeDto.getIds();
        if (ToolUtil.isEmpty(ids)) {
            return new Result<>(false, StatusCode.ERROR, "请至少选择一个设备");
        }
        int updateFlag = nodeService.nodeOPS(nodeDto);
        return new Result<>(true, StatusCode.OK, "运维成功");
    }

    @ApiOperation(value = "查询指定位置下的垛位")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pidRoute",value = "位置路由", dataType = "String", required = true),
            @ApiImplicitParam(name = "siteId",value = "所选的位置id", dataType = "String", required = true)
    })
    @PostMapping({"/findStacks"})
    public Result<List<StackVo>> findStacks(String pidRoute, String siteId) {
        if (ToolUtil.isEmpty(pidRoute)) {
            return new Result<>(false, StatusCode.ERROR, "位置路由不能为空");
        }
        if (ToolUtil.isEmpty(siteId)) {
            return new Result<>(false, StatusCode.ERROR, "位置id不能为空");
        }
        String siteRoute = pidRoute+",[0"+pidRoute.split(",").length+":"+siteId+"]";
        List<StackVo> list= warehouseService.findStackByRoute(siteRoute);
        return new Result<>(true, StatusCode.OK, "查询成功",list);
    }
}
