package com.rpzh.service.impl;

import com.rpzh.constant.NodeConstant;
import com.rpzh.dao.GMWSDao;
import com.rpzh.dao.MaintainPlanDao;
import com.rpzh.dao.NodeDao;
import com.rpzh.enums.NodeTypeEnum;
import com.rpzh.service.GMWSService;
import com.rpzh.vo.GWMSNodeDataVo;
import com.rpzh.vo.NodeDataVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GMWSServiceImpl implements GMWSService {

    @Autowired
    GMWSDao gmwsDao;

    @Autowired
    NodeDao nodeDao;

    @Autowired
    MaintainPlanDao maintainPlanDao;

    @Override
    public List<GWMSNodeDataVo> findMaintainData() {

        //查询所有的入垛设备
        List<Map<String,String>> nodeIds =  nodeDao.findEnterStack();

        //氧气设备id集
        Set<String> oxygenSet = getNodeIdsSet(nodeIds,NodeConstant.OXYGEN_NODE_TYPE_VALUE);

        NodeTypeEnum oxygenType = NodeTypeEnum.getNodeType(NodeConstant.OXYGEN_NODE_TYPE_VALUE);
        //温湿度设备id集
        Set<String> envSet = getNodeIdsSet(nodeIds,NodeConstant.HUMITURE_NODE_TYPE_VALUE);

        NodeTypeEnum evnType = NodeTypeEnum.getNodeType(NodeConstant.HUMITURE_NODE_TYPE_VALUE);
        //包芯设备id集
        Set<String> coreChipTempSet = getNodeIdsSet(nodeIds,NodeConstant.TEMPERATURE_CORE_CHIP_NODE_TYPE_VALUE);

        NodeTypeEnum coreChipTempType = NodeTypeEnum.getNodeType(NodeConstant.TEMPERATURE_CORE_CHIP_NODE_TYPE_VALUE);

        //所有氧气设备的最新数据
        List<GWMSNodeDataVo> oxygenDataVos =  gmwsDao.findNodeData(oxygenType.getTableName(),oxygenType.getTableColumn());

        //所有温湿度设备的最新数据
        List<GWMSNodeDataVo> evnDataVos =  gmwsDao.findNodeData(evnType.getTableName(),evnType.getTableColumn());

        //所有包芯温度设备的最新数据
        List<GWMSNodeDataVo> coreChipTempDataVos =  gmwsDao.findNodeData(coreChipTempType.getTableName(),coreChipTempType.getTableColumn());


        List<GWMSNodeDataVo> nodeDataVos = new ArrayList<>();

        //将在垛氧气设备的数据存放到nodedataVos中
        nodeDataVos.addAll(oxygenDataVos.stream().filter(nodeDataVo -> oxygenSet.contains(nodeDataVo.getNodeId())).collect(Collectors.toList()));

        //将在垛温湿度设备的数据存放到nodedataVos中
        nodeDataVos.addAll(evnDataVos.stream().filter(nodeDataVo -> envSet.contains(nodeDataVo.getNodeId())).collect(Collectors.toList()));

        //将在垛包芯温度设备的数据存放到nodedataVos中
        nodeDataVos.addAll(coreChipTempDataVos.stream().filter(nodeDataVo -> coreChipTempSet.contains(nodeDataVo.getNodeId())).collect(Collectors.toList()));

        return nodeDataVos;
    }

    @Override
    public List<Map<String, String>> findUsableTobacco() {
        //查询尚未结束的养护计划且该计划存在过杀虫阶段及该计划所在的垛位
        Map<String,String>  map = maintainPlanDao.findUsableStack();

        return null;
    }

    /**
     * 通过设备类型过滤
     * @param nodeIds
     * @param nodeType
     * @return
     */
    public Set<String> getNodeIdsSet(List<Map<String,String>> nodeIds,String nodeType){
        return nodeIds.stream().filter(map->
                map.get("nodeType").equals(nodeType)
        ).map(map1->
                map1.get("nodeId")
        ).collect(Collectors.toSet());
    }
}
