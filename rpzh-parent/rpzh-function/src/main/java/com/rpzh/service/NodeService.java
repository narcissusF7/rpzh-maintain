package com.rpzh.service;

import com.rpzh.dto.NodeDto;

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
}