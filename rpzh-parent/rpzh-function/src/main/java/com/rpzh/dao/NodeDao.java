package com.rpzh.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.rpzh.dto.NodeDto;
import com.rpzh.pojo.NodeData;
import com.rpzh.util.MonitorCondition;
import com.rpzh.vo.NodeDataVo;
import com.rpzh.vo.NodeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    int updateNode(@Param("nodeDto") Map<String,String> nodeDto);

    /**
     * 删除节点
     * @param ids
     * @return
     */
    int deleteNode(@Param("nodeIds") List<String> ids);

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
     * @param ids
     * @return
     */
    int unbindStack(@Param("ids") Integer[] ids);

    /**
     *查询未入垛
     * @param nodeDto
     * @return
     */
    List<NodeVo> findNoEnterStack(@Param("nodeDto") NodeDto nodeDto);

    /**
     *
     * @param id
     * @param x
     * @param y
     * @param z
     * @return
     */
    int enterStack(@Param("id")Integer id,@Param("x")String x, @Param("y")String y,
                   @Param("z")String z,@Param("stackId") Integer stackId,@Param("stack")String stack,@Param("siteId")Integer siteId,@Param("gatherSite")String gatherSite );

    /**
     * 判断设备是否存在
     * @param nodeId
     * @return
     */
    int findNodeIdCount(@Param("nodeId") String nodeId);

    /**
     * 批量导入成功
     * @param nodeVoList
     * @return
     */
    int exportNode(@Param("nodeVoList")List<NodeVo> nodeVoList);

    /**
     * 查询设备类型
     * @param nodeId
     * @return
     */
    String findNodeType(@Param("nodeId")String nodeId);

    /**
     * 模糊查询设备id
     * @param nodeDto
     * @return
     */
    List<NodeVo> findVagueNode(@Param("nodeDto")NodeDto nodeDto);

    /**
     * 测试方法
     * @param nodeId
     * @param nodeType
     * @param createTime
     * @return
     */
    List<NodeVo> testFindNode(@Param("nodeId")String nodeId, @Param("nodeType")String nodeType,
                              @Param("createTime")String createTime,@Param("startTime") String startTime,
                              @Param("endTime") String endTime);

    List<NodeVo> findNodeByStackId(Integer stackID);

    int findNodeCountByNodeId(@Param("nodeId") String nodeId);

    List<NodeVo> findNodeByMonitorCondition(@Param("monitorCondition") MonitorCondition monitorCondition,String[] nodeTypes);

    @DS("clickhouse")
    List<NodeDataVo> findNodeDataHistory(@Param("nodeDto") NodeDto var1, @Param("tableName") String tableName, @Param("tableColumn") String tableColumn);

//    @DS("clickhouse")
//    List<NodeDataVo> insertNodeDataHistory(@Param("nodeDto") NodeDto nodeDto, @Param("tableName") String tableName, @Param("tableColumn") String tableColumn);

    NodeVo findNodeAndMaintainStageByNodeId(@Param("nodeId") String nodeId);

    @DS("clickhouse")
    Integer insertNodeHistory(@Param("nodeData") NodeData nodeData,@Param("tableName") String tableName);
}
