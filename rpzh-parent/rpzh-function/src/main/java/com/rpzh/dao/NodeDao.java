package com.rpzh.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.rpzh.dto.NodeDto;
import com.rpzh.pojo.NodeData;
import com.rpzh.util.MonitorCondition;
import com.rpzh.vo.DataReportVo;
import com.rpzh.vo.NodeDataVo;
import com.rpzh.vo.NodeVo;
import com.rpzh.vo.WeatherDataVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.util.*;

/**
 * 节点管理
 *
 * @Author liubc
 * @create 2022/3/3 10:58
 * @Description: good good study,day day up
 */
@Mapper
public interface NodeDao {

    /**
     * 添加节点
     *
     * @param nodeDto
     * @return
     */
    int addNode(@Param("nodeDto") NodeDto nodeDto);

    /**
     * 查询设备id
     *
     * @param nodeId
     * @return
     */
    String findNodeID(@Param("nodeId") String nodeId, @Param("id") Integer id);

    /**
     * 更新设备信息
     *
     * @param nodeDto
     * @return
     */
    int updateNode(@Param("nodeDto") NodeDto nodeDto);

    int updateNode(@Param("nodeDto") Map<String, String> nodeDto);

    /**
     * 删除节点
     *
     * @param ids
     * @return
     */
    int deleteNode(@Param("nodeIds") List<String> ids);

    /**
     * 查询节点列表
     *
     * @param nodeDto
     * @return
     */
    List<NodeVo> findAllNodeList(@Param("nodeDto") NodeDto nodeDto);

    /**
     * 根据设备id查询设备数量
     *
     * @param nodeId
     * @return
     */
    int findNodeCountById(@Param("nodeId") String nodeId);

    /**
     * 替换设备id
     *
     * @param nodeDto
     * @return
     */
    int replaceNodeId(@Param("nodeDto") NodeDto nodeDto);

    /**
     * 设备替换日志插入
     *
     * @param nodeDto
     * @return
     */
    int insertReplaceNodeIdLog(@Param("nodeDto") NodeDto nodeDto);

    /**
     * 设备替换日志查询
     *
     * @param nodeDto
     * @return
     */
    List<NodeVo> findNodeReplaceLog(@Param("nodeDto") NodeDto nodeDto);

    /**
     * 设备解垛
     *
     * @param ids
     * @return
     */
    int unbindStack(@Param("ids") Integer[] ids);

    List<NodeVo> findNodeIdAndCaseNo(@Param("ids") Integer[] ids);

    /**
     * 查询未入垛
     *
     * @param nodeDto
     * @return
     */
    List<NodeVo> findNoEnterStack(@Param("nodeDto") NodeDto nodeDto);

    /**
     * @param nodeDto
     * @return
     */
    int enterStack(@Param("nodeDto") NodeDto nodeDto);

    /**
     * 判断设备是否存在
     *
     * @param nodeId
     * @return
     */
    int findNodeIdCount(@Param("nodeId") String nodeId);

    /**
     * 批量导入成功
     *
     * @param nodeVoList
     * @return
     */
    int exportNode(@Param("nodeVoList") List<NodeVo> nodeVoList);

    /**
     * 查询设备类型
     *
     * @param nodeId
     * @return
     */
    String findNodeType(@Param("nodeId") String nodeId);

    /**
     * 模糊查询设备id
     *
     * @param nodeDto
     * @return
     */
    List<NodeVo> findVagueNode(@Param("nodeDto") NodeDto nodeDto);

    /**
     * 测试方法
     *
     * @param nodeId
     * @param nodeType
     * @param createTime
     * @return
     */
    List<NodeVo> testFindNode(@Param("nodeId") String nodeId, @Param("nodeType") String nodeType,
                              @Param("createTime") String createTime, @Param("startTime") String startTime,
                              @Param("endTime") String endTime);

    List<NodeVo> findNodeByStackId(Integer stackID);

    int findNodeCountByNodeId(@Param("nodeId") String nodeId);

    List<NodeVo> findNodeByMonitorCondition(@Param("monitorCondition") MonitorCondition monitorCondition);

    @DS("clickhouse")
    List<NodeDataVo> findNodeDataHistory(@Param("nodeDto") NodeDto nodeDto, @Param("tableName") String tableName, @Param("tableColumn") String tableColumn,@Param("pidRoute")String pidRoute);

    @DS("clickhouse")
    NodeDataVo findNodeNowData(@Param("nodeId") String nodeId, @Param("tableName") String tableName);

    NodeVo findByNodeId(@Param("nodeId") String nodeId);

    @DS("clickhouse")
    Integer insertNodeHistory(@Param("nodeData") NodeData nodeData, @Param("tableName") String tableName, @Param("id") String id);


    Integer findNodeStatus(@Param("id") Integer id);

    List<NodeVo> findNodeIdBySiteType(@Param("siteType") String siteType, @Param("stackId") Integer stackId, @Param("pidRoute") String pidRoute);

    List<NodeVo> findHumitureDevice(@Param("monitorCondition") MonitorCondition monitorCondition);

    @DS("clickhouse")
    List<NodeData> findNodeData(@Param("nodeId") String nodeId, @Param("tableName") String tableName, @Param("tableColumn") String tableColumn);

    @DS("clickhouse")
    List<NodeData> findDataNode(@Param("tableName") String tableName, @Param("column") String column, @Param("siteRoutes") List<String> pidRoutes,@Param("size")Integer size);

    @DS("clickhouse")
    List<NodeData> findDataContrast(@Param("tableName") String tableName, @Param("column") String column, @Param("siteRoutes") List<String> pidRoutes,@Param("size")Integer size);

    List<Map<String, String>> findEnterStack();

    List<NodeVo> findAirConditioner(@Param("monitorCondition") MonitorCondition monitorCondition);

    @DS("clickhouse")
    List<NodeData> findDataOxygen(@Param("siteRoutes") List<String> siteRoutes,@Param("size")Integer size);

    @DS("clickhouse")
    List<Map<String, Object>> findSelectedNode(@Param("nodeId") String nodeId, @Param("startTime") Date startTime, @Param("endTime") Date endTime,
                                               @Param("gatherType") String gatherType, @Param("columnOne") String columnOne, @Param("tableColumn") String tableColumn);

    @DS("clickhouse")
    List<Map<String, Object>> findSelectedSiteRoute(@Param("siteRoute") String siteRoute, @Param("startTime") Date startTime, @Param("endTime") Date endTime,
                                                    @Param("gatherType") String gatherType, @Param("columnOne") String columnOne, @Param("tableColumn") String tableColumn);

    @DS("clickhouse")
    Map<String, Double> findNowNodeData(@Param("airNodes") List<String> airNodes);

    @DS("clickhouse")
    Map<String, Double> findStackEnv(@Param("id") Integer id);

    @DS("clickhouse")
    Double findStackCore(@Param("id") Integer id);

    @DS("clickhouse")
    Double findO2StackData(@Param("id") Integer id);

    @DS("clickhouse")
    Double findO2AreaData(@Param("pidRoute") String pidRoute);

    Integer findCountByNodeID(@Param("nodeId") String nodeId);

    String findSiteRouteByNodeId(@Param("nodeId")String nodeId);

    Integer findCountByCaseNo(@Param("caseNo") String caseNo);

    String findNodeTypeByNodeId(@Param("id") Integer id, @Param("nodeId") String nodeId);

    List<Map<String, Object>> findNodeAddress(@Param("deviceId") String deviceId,
                                        @Param("addressRoute") String addressRoute);

    @DS("clickhouse")
    List<Map> findStatistics(@Param("array") List<String> pidRoutes, @Param("tableName") String tableName,@Param("dateType")String dateType,@Param("beginTime")String beginTime,@Param("endTime")String endTime);

    List<String> findNodeIds(@Param("stackId")Integer stackId);

    int nodeOPS(@Param("nodeIds") String[] nodeIds);

    List<NodeDto> findNodeVos(@Param("ids") Integer[] ids);

    List<String> findNodeIdByIds(@Param("ids")Integer[] ids);
}
