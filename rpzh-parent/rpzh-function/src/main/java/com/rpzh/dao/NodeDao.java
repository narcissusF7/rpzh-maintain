package com.rpzh.dao;

import com.rpzh.dto.NodeDto;
import com.rpzh.vo.NodeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 节点管理
 * @Author liubc
 * @create 2022/3/3 10:58
 * @Description: good good study,day day up
 */
@Mapper
public interface NodeDao {

    /**
     * 添加节点
     * @param nodeDto
     * @return
     */
    int addNode(@Param("nodeDto") NodeDto nodeDto);

    /**
     * 查询设备id
     * @param nodeId
     * @return
     */
    String findNodeID(@Param("nodeId") String nodeId,@Param("id") Integer id);

    /**
     * 更新设备信息
     * @param nodeDto
     * @return
     */
    int updateNode(@Param("nodeDto") NodeDto nodeDto);

    /**
     * 删除节点
     * @param ids
     * @return
     */
    int deleteNode(@Param("ids") List<Integer> ids);

    /**
     * 查询节点列表
     * @param nodeDto
     * @return
     */
    List<NodeVo> findAllNodeList(@Param("nodeDto") NodeDto nodeDto);

    /**
     * 根据设备id查询设备数量
     * @param nodeId
     * @return
     */
    int findNodeCountById(@Param("nodeId") String nodeId);

    /**
     * 替换设备id
     * @param nodeDto
     * @return
     */
    int replaceNodeId(@Param("nodeDto") NodeDto nodeDto);

    /**
     * 设备替换日志插入
     * @param nodeDto
     * @return
     */
    int insertReplaceNodeIdLog(@Param("nodeDto") NodeDto nodeDto);

    /**
     * 设备替换日志查询
     * @param nodeDto
     * @return
     */
    List<NodeVo> findNodeReplaceLog(@Param("nodeDto") NodeDto nodeDto);

    /**
     * 设备解垛
     * @param id
     * @return
     */
    int unbindStack(@Param("id") Integer id);

    /**
     *
     * @param nodeDto
     * @return
     */
    List<NodeVo> findNoEnterStack(@Param("nodeDto") NodeDto nodeDto);
}
