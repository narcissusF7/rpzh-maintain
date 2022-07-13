package com.rpzh.service.impl;

import com.rpzh.constant.NodeConstant;
import com.rpzh.dao.GMWSDao;
import com.rpzh.dao.MaintainPlanDao;
import com.rpzh.dao.NodeDao;
import com.rpzh.entity.DateToStrUtil;
import com.rpzh.enums.NodeTypeEnum;
import com.rpzh.service.GMWSService;
import com.rpzh.vo.GWMSNodeDataVo;
import com.rpzh.vo.NodeDataVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        //查询尚未结束的养护计划且该计划存在过杀虫阶段及该计划所在的垛位
        List<Map<String,String>>  list = maintainPlanDao.findUsableStack();
        list.stream().filter(map->{
            Calendar c = Calendar.getInstance();
            int month = c.get(Calendar.MONTH) + 1;
            //如果5月份以前
            if (month<=5){
                c.setTime(new Date());
                c.add(Calendar.YEAR, -1);
                //判断12个月内是否杀虫过
                Date date = c.getTime();
                try {
                    if (date.before(format.parse(map.get("begingTime")))){
                        return true;
                    }else {
                        return false;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }else {
                Calendar currCal=Calendar.getInstance();
                //获取今年第一天
                c.set(Calendar.YEAR,currCal.get(Calendar.YEAR));
                Date date = c.getTime();
                try {
                    if (date.before(format.parse(map.get("begingTime")))){
                        return true;
                    }else {
                        return false;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            return false;
        });

        return list;
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
