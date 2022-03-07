package com.rpzh.controller;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.NodeDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.NodeService;
import com.rpzh.vo.NodeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 节点管理
 * @Author liubc
 * @create 2022/3/3 9:39
 * @Description: good good study,day day up
 */
@RestController
@RequestMapping("/node")
@CrossOrigin
public class NodeController {


    @Autowired
    private NodeService nodeService;

    /**
     * 添加节点
     * @param nodeDto
     * @return
     */
    @PostMapping("/addNode")
    public Result addNode(NodeDto nodeDto){
        String nodeId = nodeDto.getNodeId();
        if (ToolUtil.isEmpty(nodeId)){
            return new Result(false, StatusCode.ERROR, "节点Id不能为空");
        }
        //判断设备id是否存在
        int nodeCount = nodeService.findNodeCountById(nodeId);
        if (nodeCount > 0){
            return new Result(false, StatusCode.ERROR, "该设备已经存在,禁止添加");
        }
        int addFlag = nodeService.addNode(nodeDto);
        return new Result(true, StatusCode.OK, "添加节点成功");
    }

    /**
     * 更新设备
     * @param nodeDto
     * @return
     */
    @PostMapping("/updateNode")
    public Result updateNode(NodeDto nodeDto){
        String nodeId = nodeDto.getNodeId();
        if (ToolUtil.isEmpty(nodeId)){
            return new Result(false, StatusCode.ERROR, "节点Id不能为空");
        }
        Integer id = nodeDto.getId();
        if (ToolUtil.isEmpty(id)){
            return new Result(false, StatusCode.ERROR, "请选择一个设备");
        }
        String nodeIdFind = nodeService.findNodeID(nodeId,id);
        if (nodeId.equals(nodeIdFind)){
            return new Result(false, StatusCode.ERROR, "节点id已经存在,不允许修改");
        }
        int updateFlag = nodeService.updateNode(nodeDto);
        return new Result(true, StatusCode.OK, "修改设备成功");
    }


    /**
     * 查询节点列表
     * @param nodeDto
     * @return
     */
    @PostMapping("/findNode")
    public Result<PageInfo> findNode(NodeDto nodeDto){
        PageInfo<NodeVo> pageInfo = nodeService.findAllNodeList(nodeDto);
        return new Result<PageInfo>(true, StatusCode.OK, "查询成功", pageInfo);
    }


    /**
     * 删除节点
     * @param
     * @return
     */
    @PostMapping("/deleteNode")
    public Result deleteNode(NodeDto nodeDto){
        List<Integer> ids = nodeDto.getIds();
        if (ToolUtil.isEmpty(ids)){
            return new Result(false, StatusCode.ERROR, "请选择一个节点");
        }
        int delFlag = nodeService.deleteNode(ids);
        return new Result(true, StatusCode.OK, "删除节点成功");
    }
}