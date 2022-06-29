package com.rpzh.service.impl;

import com.rpzh.constant.NodeConstant;
import com.rpzh.dao.GMWSDao;
import com.rpzh.dao.NodeDao;
import com.rpzh.enums.NodeTypeEnum;
import com.rpzh.service.GMWSService;
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

    @Override
    public List<NodeDataVo> findMaintainData() {

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
        List<NodeDataVo> oxygenDataVos =  gmwsDao.findNodeData(oxygenType.getTableName(),oxygenType.getTableColumn());

        //所有温湿度设备的最新数据
        List<NodeDataVo> evnDataVos =  gmwsDao.findNodeData(evnType.getTableName(),evnType.getTableColumn());

        //所有包芯温度设备的最新数据
        List<NodeDataVo> coreChipTempDataVos =  gmwsDao.findNodeData(coreChipTempType.getTableName(),coreChipTempType.getTableColumn());


        List<NodeDataVo> nodeDataVos = new ArrayList<>();

        //将在垛氧气设备的数据存放到nodedataVos中
        nodeDataVos.addAll(oxygenDataVos.stream().filter(nodeDataVo -> oxygenSet.contains(nodeDataVo.getNodeId())).collect(Collectors.toList()));

        //将在垛温湿度设备的数据存放到nodedataVos中
        nodeDataVos.addAll(evnDataVos.stream().filter(nodeDataVo -> envSet.contains(nodeDataVo.getNodeId())).collect(Collectors.toList()));

        //将在垛包芯温度设备的数据存放到nodedataVos中
        nodeDataVos.addAll(coreChipTempDataVos.stream().filter(nodeDataVo -> coreChipTempSet.contains(nodeDataVo.getNodeId())).collect(Collectors.toList()));

        return nodeDataVos;
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
