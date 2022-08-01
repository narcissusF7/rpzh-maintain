package com.rpzh.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.constant.NodeConstant;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.dto.NodeDto;
import com.rpzh.dto.NodeOperationDto;
import com.rpzh.entity.ExcelUtils;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.enums.NodeTypeEnum;
import com.rpzh.service.NodeOperationService;
import com.rpzh.service.NodeService;
import com.rpzh.vo.NodeOperationVo;
import com.rpzh.vo.NodeVo;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 节点管理
 *
 * @Author liubc ljj
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

    @PostMapping("/addNode")
    @ApiOperation(value = "添加设备")
    @ApiImplicitParams({@ApiImplicitParam(
            name = "nodeId",
            value = "设备id",
            required = true,
            dataType = "String"
    ), @ApiImplicitParam(
            name = "nodeType",
            value = "设备类型",
            required = true,
            dataType = "String"
    )})
    public Result addNode(@ApiIgnore NodeDto nodeDto) {
        String nodeId = nodeDto.getNodeId();
        if (ToolUtil.isEmpty(nodeId)) {
            return new Result<>(false, StatusCode.ERROR, "设备Id不能为空");
        }

        if (ToolUtil.isEmpty(nodeDto.getNodeType())) {
            return new Result<>(false, StatusCode.ERROR, "设备类型不能为空");
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
            value = "设备类型",
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
    ), @ApiImplicitParam(
            name = "caseNo",
            value = "箱号",
            dataType = "String"
    )})
    @PostMapping("/updateNode")
    public Result updateNode(@ApiIgnore NodeDto nodeDto) {
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
        if (NodeTypeEnum.TEMPERATURE_CORE_CHIP_NODE.getCode().equals(nodeDto.getNodeType())){
            if (nodeDto.getSiteRoute().split(",").length<5){
                return new Result<>(false, StatusCode.ERROR, "包芯设备不能放置到垛位外");
            }
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
            name = "siteRoute",
            value = "位置路由",
            dataType = "String"
    ),@ApiImplicitParam(
            name = "siteId",
            value = "位置Id",
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
    public Result<NodeVo> findNode(@ApiIgnore NodeDto nodeDto) {
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
    public Result<NodeVo> findNodeReplaceLog(@ApiIgnore NodeDto nodeDto) {
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
    public Result<NodeVo> findNoEnterStack(@ApiIgnore NodeDto nodeDto) {
        PageInfo<NodeVo> pageInfo = nodeService.findNoEnterStack(nodeDto);
        return new Result<>(true, StatusCode.OK, "查询成功", pageInfo);
    }

    @ApiOperation("设备入垛")
    @ApiImplicitParam(
            name = "data",
            value = "JSON字符串,可以包含多个对象 每个对象包含id（设备主键id 必传）,xyz（根据情况填写）,stackId（垛位id 垛内设备必传）,caseNo（箱号）,siteType（采集位置类型 04 仓间设备 05非仓间设备 必传），pid_route(必传)",
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
                //设置设备所在垛位的id
                if (ToolUtil.isEmpty(map.get("stackId"))){
                    return new Result<>(false, StatusCode.ERROR, "第"+(i+1)+"个设备的位置id为空");
                }
                nodeDto.setStackId(Integer.parseInt(map.get("stackId").toString()));
                nodeDto.setSiteId(Integer.parseInt(map.get("stackId").toString()));
                //设置采集位置类型
                if (ToolUtil.isEmpty(map.get("stackId"))){
                    return new Result<>(false, StatusCode.ERROR, "第"+(i+1)+"个设备的位置id为空");
                }
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
                nodeDto.setSiteRoute(map.get("pidRoute").toString()+",[04:"+map.get("stackId")+"]");
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


    @ApiOperation(
            value = "设备解垛"
    )
    @ApiImplicitParam(name = "ids",value = "id集", dataType = "Integer",allowMultiple = true, required = true)
    @PostMapping({"/unbindStack"})
    public Result unbindStack(@ApiIgnore NodeDto nodeDto) {
        Integer[] ids = nodeDto.getIds();
        if (ToolUtil.isEmpty(ids)) {
            return new Result<>(false, StatusCode.ERROR, "请至少选择一个设备");
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
            nodeDate = ExcelUtils.importExcel(file, 1 , 0, 0);
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
        if (!ToolUtil.isEmpty(nodeDto.getPage())&&!ToolUtil.isEmpty(nodeDto.getSize())){
            int page = nodeDto.getPage();
            int size = nodeDto.getSize();
            PageHelper.startPage(page, size);
        }
        if (ToolUtil.isEmpty(nodeDto.getNodeId())) {
            return new Result<>(false, StatusCode.ERROR, "请输入节点id");
        }
        List<NodeVo> nodeList = nodeService.findVagueNode(nodeDto);

        return new Result<>(true, StatusCode.OK, "查询成功", nodeList);
    }

    @ApiOperation(
            value = "根据设备id查询设备类型"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "nodeId",
            value = "设备id",
            required = true,
            dataType = "String"
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

    @ApiOperation(value = "翻垛")
    @PostMapping({"/flipStack"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "养护计划id", dataType = "Integer",required = true),
            @ApiImplicitParam(name = "stackId", value = "垛位id", dataType = "Integer",required = true),
            @ApiImplicitParam(name = "nodeDtoList", value = "该养护计划下的所有垛位", required = true,allowMultiple = true,dataType = "String"),
            @ApiImplicitParam(name = "pidRoute", value = "位置路由", dataType = "String"),
            @ApiImplicitParam(name = "siteName", value = "位置名称",dataType = "String")

    })
    public Result flipStack(MaintainPlanDto maintainPlanDto) {
        if (ToolUtil.isEmpty(maintainPlanDto.getId())){
            return new Result(false,StatusCode.ERROR,"养护计划id不能为空");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getStackId())){
            return new Result(false,StatusCode.ERROR,"垛位id不能为空");
        }
        if (ToolUtil.isEmpty(maintainPlanDto.getNodeDtoList())){
            return new Result(false,StatusCode.ERROR,"该养护计划下的所有垛位不能为空");
        }

        return nodeService.flipStack(maintainPlanDto);
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

}