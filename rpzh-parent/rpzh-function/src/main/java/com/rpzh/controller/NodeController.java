package com.rpzh.controller;

import com.rpzh.dto.NodeDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}