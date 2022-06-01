package com.rpzh.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.constant.NodeConstant;
import com.rpzh.dto.NodeDto;
import com.rpzh.dto.NodeOperationDto;
import com.rpzh.entity.ExcelUtils;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.enums.NodeTypeEnum;
import com.rpzh.service.MaterialService;
import com.rpzh.service.NodeOperationService;
import com.rpzh.service.NodeService;
import com.rpzh.util.EasyPoiUtil;
import com.rpzh.vo.NodeDataVo;
import com.rpzh.vo.NodeOperationVo;
import com.rpzh.vo.NodeVo;
import io.swagger.annotations.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 节点管理
 *
 * @Author liubc
 * @create 2022/3/3 9:39
 * @Description: good good study,day day up   /node/deleteNode
 */
@RestController
@RequestMapping("/maintain/node")
@CrossOrigin
@Api(value = "设备控制中心", tags = "设备控制中心")
public class NodeController {

    @Autowired
    NodeOperationService operationService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private MaterialService materialService;

    @PostMapping("/addNode")
    @ApiOperation(value = "添加设备")
    @ApiImplicitParams({@ApiImplicitParam(
            name = "nodeId",
            value = "设备id",
            required = true,
            dataType = "String"
    ), @ApiImplicitParam(
            name = "nodeType",
            value = "设备类型,默认:01",
            required = true,
            dataType = "String"
    ), @ApiImplicitParam(
            name = "siteType",
            value = "采集位置类型,04:仓间,05非仓间  默认05",
            required = true,
            dataType = "String",
            defaultValue = "05"
    )})
    public Result addNode(@ApiIgnore NodeDto nodeDto) {
        String nodeId = nodeDto.getNodeId();
        if (ToolUtil.isEmpty(nodeId)) {
            return new Result<>(false, StatusCode.ERROR, "节点Id不能为空");
        }
        //判断设备id是否存在
        int nodeCount = nodeService.findNodeCountById(nodeId);
        if (nodeCount > 0) {
            return new Result<>(false, StatusCode.ERROR, "该设备已经存在,禁止添加");
        }
        int addFlag = nodeService.addNode(nodeDto);
        return new Result<>(true, StatusCode.OK, "添加节点成功");
    }

    @ApiOperation(
            value = "更新设备"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "id",
            value = "id",
            required = true,
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "nodeId",
            value = "设备id",
            required = true,
            dataType = "String"
    ), @ApiImplicitParam(
            name = "nodeType",
            value = "设备类型,默认:01",
            required = true,
            dataType = "String"
    ), @ApiImplicitParam(
            name = "status",
            value = "状态,0:解垛,1:入垛,2:未使用 ,3:故障",
            dataType = "String",
            required = true
    ), @ApiImplicitParam(
            name = "siteRoute",
            value = "位置路由",
            dataType = "String",
            required = true
    ), @ApiImplicitParam(
            name = "gatherSite",
            value = "采集位置",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "pid_route",
            value = "位置路由",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "stackId",
            value = "垛位id",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "stack",
            value = "垛位",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "siteType",
            value = "采集位置类型,04:仓间,05非仓间",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "siteX",
            value = "采集位置-X坐标",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "siteY",
            value = "采集位置-Y坐标",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "siteZ",
            value = "采集位置-Z坐标",
            dataType = "Integer"
    )})
    @PostMapping("/updateNode")
    public Result updateNode(@ApiIgnore NodeDto nodeDto) {
        //TODO 需要传个siteId 然后拼接到nodeDto.siteRoute中
        String nodeId = nodeDto.getNodeId();
        if (ToolUtil.isEmpty(nodeId)) {
            return new Result<>(false, StatusCode.ERROR, "节点Id不能为空");
        }
        Integer id = nodeDto.getId();
        if (ToolUtil.isEmpty(id)) {
            return new Result<>(false, StatusCode.ERROR, "请选择一个设备");
        }
        String nodeIdFind = nodeService.findNodeID(nodeId, id);
        if (nodeId.equals(nodeIdFind)) {
            return new Result<>(false, StatusCode.ERROR, "节点id已经存在,不允许修改");
        }
        Integer status = nodeService.findNodeStatus(id);
        if (status ==0){
            return new Result<>(false, StatusCode.ERROR, "解垛设备不允许修改");
        }
        int updateFlag = nodeService.updateNode(nodeDto);
        return new Result<>(true, StatusCode.OK, "修改设备成功");
    }


    @ApiOperation(
            value = "查询设备列表"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "nodeId",
            value = "设备id",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "nodeType",
            value = "设备类型",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "status",
            value = "状态,0:解垛,1:入垛,2:未使用 3:故障",
            dataType = "String"
    ),@ApiImplicitParam(
            name = "gatherSite",
            value = "采集位置",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "startTime",
            value = "开始时间",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "endTime",
            value = "结束时间",
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
    @PostMapping("/findNode")
    public Result<PageInfo<NodeVo>> findNode(@ApiIgnore NodeDto nodeDto) {
        PageInfo<NodeVo> pageInfo = nodeService.findAllNodeList(nodeDto);
        return new Result<>(true, StatusCode.OK, "查询成功", pageInfo);
    }

    @ApiOperation(value = "删除设备")
    @ApiImplicitParam(value = "nodeIds", name = "要被删除的设备id列表", paramType = "body", allowMultiple = true, dataType = "String")
    @DeleteMapping({"/deleteNode"})
    public Result deleteNode(String[] nodeIds) {

        if (ToolUtil.isEmpty(nodeIds)) {
            return new Result<>(false, StatusCode.ERROR, "请选择一个设备");
        }
        int delFlag = nodeService.deleteNode(Arrays.asList(nodeIds));

        return new Result<>(true, StatusCode.OK, "删除节点成功");
    }


    @ApiOperation(
            value = "设备替换"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "id",
            value = "id",
            required = true,
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "nodeId",
            value = "设备id",
            required = true,
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "afterNodeId",
            value = "替换后的设备id",
            required = true,
            dataType = "Integer"
    )})
    @PostMapping({"/replaceNode"})
    public Result replaceNodeId(@ApiIgnore NodeDto nodeDto) {
        Integer id = nodeDto.getId();
        if (ToolUtil.isEmpty(id)) {
            return new Result<>(false, StatusCode.ERROR, "请选择一个设备");
        }
        String beforeNodeId = nodeDto.getNodeId();
        if (ToolUtil.isEmpty(beforeNodeId)) {
            return new Result<>(false, StatusCode.ERROR, "请选择需要替换的设备id");
        }
        String afterNodeId = nodeDto.getAfterNodeId();
        if (ToolUtil.isEmpty(afterNodeId)) {
            return new Result<>(false, StatusCode.ERROR, "请输入替换之后的设备id");
        }

        int updateFlag = nodeService.replaceNodeId(nodeDto);
        if (updateFlag != 1) {
            return new Result<>(true, StatusCode.ERROR, "设备替换失败");
        }
        return new Result<>(true, StatusCode.OK, "设备替换成功");

    }


    @ApiOperation(
            value = "设备替换记录"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "id",
            value = "id",
            required = true,
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "page",
            value = "当前页数",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "size",
            value = "每页数量",
            dataType = "Integer"
    )})
    @GetMapping({"/findNodeReplaceLog"})
    public Result<PageInfo<NodeVo>> findNodeReplaceLog(@ApiIgnore NodeDto nodeDto) {
        Integer id = nodeDto.getId();
        if (ToolUtil.isEmpty(id)) {
            return new Result<>(false, StatusCode.ERROR, "请选择一个设备");
        }
        PageInfo<NodeVo> pageInfo = nodeService.findNodeReplaceLog(nodeDto);
        return new Result<>(true, StatusCode.OK, "查询成功", pageInfo);
    }

    @ApiOperation(
            value = "查询设备未入垛列表"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "nodeId",
            value = "设备id",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "nodeType",
            value = "设备类型",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "siteId",
            value = "采集位置ID",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "page",
            value = "当前页数",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "size",
            value = "每页数量",
            dataType = "Integer"
    )})
    @GetMapping({"/findNoEnterStack"})
    public Result<PageInfo<NodeVo>> findNoEnterStack(@ApiIgnore NodeDto nodeDto) {
        PageInfo<NodeVo> pageInfo = nodeService.findNoEnterStack(nodeDto);
        return new Result<>(true, StatusCode.OK, "查询成功", pageInfo);
    }

    @ApiOperation("设备入垛")
    @ApiImplicitParam(
            name = "data",
            value = "JSON字符串,可以包含多个对象 每个对象包含id（设备主键id 必传）,xyz（根据情况填写）,stackId（垛位id 垛内设备必传）,caseNo（箱号）,siteType（采集位置类型 04 仓间设备 05非仓间设备 必传），level （位置等级 必传），pid_route",
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
            nodeDto.setId(Integer.parseInt(map.get("id").toString()));
            //如果是垛内设备
            if ("5".equals(map.get("level"))) {
                //设置设备所在垛位的id
                nodeDto.setStackId(Integer.parseInt(map.get("stackId").toString()));
                //设置采集位置类型
                nodeDto.setSiteType(NodeConstant.NODE_STACK);
                //设置X坐标
                nodeDto.setSiteX(map.get("x").toString());
                //设置Y坐标
                nodeDto.setSiteY(map.get("y").toString());
                //设置Z坐标
                nodeDto.setSiteZ(map.get("z").toString());
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
            } else if ("4".equals(map.get("level"))) {
                //设置位置路由
                nodeDto.setSiteRoute(map.get("pidRoute").toString()+",[04:"+map.get("stackId")+"]");
                //设置采集位置
                nodeDto.setGatherSite(map.get("stack").toString());
                //设置采集位置类型
                nodeDto.setSiteType(NodeConstant.NODE_WARE_AREA);
            }
            nodeService.enterStack(nodeDto);
        }

        return new Result<>(true, StatusCode.OK, "设备入垛成功");
    }


    @ApiOperation(
            value = "设备解垛"
    )
    @ApiImplicitParam(name = "ids",value = "id集", dataType = "Integer",allowMultiple = true, required = true)
    @PostMapping({"/unbindStack"})
    public Result unbindStack(@ApiIgnore NodeDto nodeDto) {
        Integer[] ids = nodeDto.getIds();
        if (ids.length<=0) {
            return new Result<>(false, StatusCode.ERROR, "请选择一个设备");
        }
        int updateFlag = nodeService.unbindStack(nodeDto);
        return new Result<>(true, StatusCode.OK, "解垛成功");
    }


    @ApiOperation(
            value = "批量导入设备"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "file",
            value = "文件",
            dataType = "MultipartFile",
            required = true
    ), @ApiImplicitParam(
            name = "nodeType",
            value = "设备类型",
            dataType = "String",
            required = true
    )})
    @PostMapping("/exportNode")
    public Result exportNode(MultipartFile file, String nodeType) {
        if (ToolUtil.isEmpty(nodeType)) {
            return new Result<>(false, StatusCode.ERROR, "节点类型不能为空");
        }
        if (ToolUtil.isEmpty(file)) {
            return new Result<>(false, StatusCode.ERROR, "请选择一个文件");
        }
        //读取表格中的数据，file 文件，startRow:开始的行数   startCloumn:开始的列数，endCloumn:结束的列数
        List<Map<Integer, String>> nodeDate;
        try {
            nodeDate = ExcelUtils.importExcel(file, 2, 0, 0);
        } catch (IOException e) {
            e.printStackTrace();
            return new Result<>(false, StatusCode.ERROR, "导入失败");
        }
        return nodeService.exportNode(nodeDate, nodeType);
    }


    /**
     * 设备导入模板下载
     */
    @ApiOperation("设备导入模板下载")
    @GetMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) {
        String downloadFilePath = "/template/node/node.xlsx";//被下载的文件在服务器中的路径,
        String fileName = "设备导入模板";//被下载文件的名称
        InputStream inputStream = getClass().getResourceAsStream(downloadFilePath);
        ExcelUtils.outFile(inputStream, response, fileName, "xlsx");
    }

    @ApiOperation(
            value = "模糊查询设备"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "nodeId",
            value = "设备id",
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
    @PostMapping("/findVagueNode")
    public Result<List<NodeVo>> findVagueNode(@ApiIgnore NodeDto nodeDto) {

        int page = nodeDto.getPage();
        int size = nodeDto.getSize();
        PageHelper.startPage(page, size);
        if (ToolUtil.isEmpty(nodeDto.getNodeId())) {
            return new Result<>(false, StatusCode.ERROR, "请输入节点id");
        }
        List<NodeVo> nodeList = nodeService.findVagueNode(nodeDto);

        return new Result<>(true, StatusCode.OK, "查询成功", nodeList);
    }

    @ApiOperation(
            value = "根据id查询设备类型"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "id",
            value = "id",
            required = true,
            dataType = "Integer"
    )})
    @PostMapping("/findNodeType")
    public Result<String> findNodeType(@ApiIgnore NodeDto nodeDto) {
        if (ToolUtil.isEmpty(nodeDto.getNodeId())) {
            return new Result<>(false, StatusCode.ERROR, "请输入一个设备");
        }
        String nodeType = nodeService.findNodeType(nodeDto);
        return new Result<>(true, StatusCode.OK, "查询成功", nodeType);
    }

    @ApiOperation(
            value = "根据垛位id查询设备"
    )
    @ApiImplicitParam(
            name = "stackId",
            value = "垛位id",
            dataType = "Integer"
    )
    @GetMapping({"/findNodeByStackId"})
    public Result<List<NodeVo>> findNodeByStackId(Integer stackId) {
        List<NodeVo> nodeVoList = nodeService.findNodeByStackId(stackId);
        return new Result(true, StatusCode.OK, "查询成功", nodeVoList);
    }


//    @ApiOperation(
//            value = "翻垛"
//    )
//    @PostMapping({"/flipStack"})
//    public Result flipStack(@RequestBody @ApiIgnore List<NodeDto> nodeDtoList) {
//        Integer updateFlag = nodeService.updateNodes(nodeDtoList);
//        return new Result(true, StatusCode.OK, "翻垛成功");
//    }
    @ApiOperation(value = "翻垛")
    @PostMapping({"/flipStack"})
    @ApiImplicitParam(name = "data",value = "翻垛的数据集",dataType = "String",required = true)
    public Result flipStack(String data) {

        Integer updateFlag = nodeService.updateNodes(data);
        return new Result(true, StatusCode.OK, "翻垛成功");
    }

    @ApiOperation(
            value = "操作记录"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "nodeId",
            value = "设备id",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "nodeType",
            value = "设备类型,默认:01",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "operationType",
            value = "操作类型",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "page",
            value = "当前页数",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "size",
            value = "每页数量",
            dataType = "Integer"
    )})
    @GetMapping({"/findOperationHistory"})
    public Result<List<NodeOperationVo>> findOperationHistory(@ApiIgnore NodeOperationDto nodeOperationDto) {
        List<NodeOperationVo> nodeOperationVos = operationService.selectNodeOperation(nodeOperationDto);
        return new Result(true, StatusCode.OK, "查询成功", nodeOperationVos);
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
            value = "设备类型,默认:01",
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
    )})
    @GetMapping({"/findNodeHistory"})
    public Result<List<NodeDataVo>> findNodeHistory(@ApiIgnore NodeDto nodeDto) {
        int page = nodeDto.getPage();
        int size = nodeDto.getSize();
        PageHelper.startPage(page, size);
        List<NodeDataVo> nodeDataVos = nodeService.findNodeHistory(nodeDto);
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
            value = "设备类型,默认:01",
            dataType = "String",
            required = true
    ), @ApiImplicitParam(
            name = "dataType",
            value = "数据类型",
            dataType = "Integer",
            required = true
    )})
    @GetMapping({"/findNodeHistoryGraph"})
    public Result<Map<String, HashMap<String, Double>>> findNodeHistoryGraph(@ApiIgnore NodeDto nodeDto) {
        List<NodeDataVo> nodeDataVos = nodeService.findNodeHistory(nodeDto);
        Map<String, List<NodeDataVo>> collect = nodeDataVos.stream().collect(Collectors.groupingBy((item) -> (
                item.getTransferTime().split(" ")[0])));
        Map<String, HashMap<String, Double>> historyGraphData = new HashMap();
        if (nodeDto.getDataType() == NodeConstant.HUMITURE_NUMBER) {
            collect.forEach((k, value) -> {
                HashMap<String, Double> map = new HashMap();
                map.put("max", value.stream().map(NodeDataVo::getHumiture).max(Double::compareTo).get());
                map.put("min", value.stream().map(NodeDataVo::getHumiture).min(Double::compareTo).get());
                map.put("avg", value.stream().mapToDouble(NodeDataVo::getHumiture).average().getAsDouble());
                historyGraphData.put(k, map);
            });
        } else {
            collect.forEach((k, value) -> {
                HashMap<String, Double> map = new HashMap();
                map.put("max", value.stream().map(NodeDataVo::getData).max(Double::compareTo).get());
                map.put("min", value.stream().map(NodeDataVo::getData).min(Double::compareTo).get());
                map.put("avg", value.stream().mapToDouble(NodeDataVo::getData).average().getAsDouble());
                historyGraphData.put(k, map);
            });
        }

        return new Result<>(true, StatusCode.OK, "查询成功", historyGraphData);
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
            value = "设备类型",
            dataType = "String",
            required = true
    )})
    @GetMapping({"/exportNodeHistory"})
    public void exportNodeHistory(@ApiIgnore NodeDto nodeDto, HttpServletResponse response) {
        List<NodeDataVo> nodeHistory = nodeService.findNodeHistory(nodeDto);
        EasyPoiUtil<NodeDataVo> easyPoiUtil = new EasyPoiUtil();
        List<String> columns = new ArrayList<>();
        //把要导出的字段添加到集合中
        if (ToolUtil.isEmpty(nodeDto.getNodeType())) {
            columns.add(NodeDataVo.getColumn(nodeDto.getNodeType()));
            if (NodeConstant.HUMITURE_NODE_TYPE_VALUE.equals(nodeDto.getNodeType())) {
                columns.add("humiture");
            }
        }

        //显示要导出的字段
        nodeHistory.forEach((item) -> {
            easyPoiUtil.t = item;
            columns.forEach((column) -> {
                try {
                    easyPoiUtil.hihdColumn(column, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        });
        String excelType = NodeTypeEnum.getNodeType(nodeDto.getNodeType()).getName() + nodeDto.getNodeId() + ".xls";
        EasyPoiUtil.exportExcel(nodeHistory, nodeDto.getNodeId(), nodeDto.getNodeId(), NodeDataVo.class, excelType, true, response);
    }
}