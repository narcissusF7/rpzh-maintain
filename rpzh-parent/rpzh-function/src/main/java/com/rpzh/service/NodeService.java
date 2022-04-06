package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.NodeDto;
import com.rpzh.vo.NodeVo;

import java.util.List;

/**
 * @Author liubc
 * @create 2022/3/3 10:57
 * @Description: good good study,day day up
 */
public interface NodeService {

    /**
     * 添加节点
     * @param nodeDto
     * @return
     */
    int addNode(NodeDto nodeDto);

    /**
     * 更新设备信息
     * @param nodeId
     * @return
     */
    String findNodeID(String nodeId,Integer id);

    /**
     * 修改设备
     * @param nodeDto
     * @return
     */
    int updateNode(NodeDto nodeDto);

    /**
     * 删除节点
     * @param ids
     * @return
     */
    int deleteNode(List<Integer> ids);

    /**
     * 查询节点列表
     * @param nodeDto
     * @return
     */
    PageInfo<NodeVo> findAllNodeList(NodeDto nodeDto);

    /**
     * 根据设备id查询设备数量
     * @param nodeId
     * @return
     */
    int findNodeCountById(String nodeId);

    /**
     * 替换设备id
     * @param nodeDto
     * @return
     */
    int replaceNodeId(NodeDto nodeDto);

    /**
     * 设备替换日志查询
     * @param nodeDto
     * @return
     */
    PageInfo<NodeVo> findNodeReplaceLog(NodeDto nodeDto);

    /**
     * 设备解垛
     * @param nodeDto
     * @return
     */
    int unbindStack(NodeDto nodeDto);

    /**
     * 查询未入垛设备
     * @param nodeDto
     * @return
     */
    PageInfo<NodeVo> findNoEnterStack(NodeDto nodeDto);
}