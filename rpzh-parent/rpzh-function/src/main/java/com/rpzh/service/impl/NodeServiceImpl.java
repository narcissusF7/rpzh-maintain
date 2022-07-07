package com.rpzh.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.constant.NodeConstant;
import com.rpzh.dao.GeneralSettingDao;
import com.rpzh.dao.NodeDao;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.dto.NodeDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.enums.NodeTypeEnum;
import com.rpzh.pojo.GeneralSetting;
import com.rpzh.pojo.NodeData;
import com.rpzh.service.MonitoringCenterService;
import com.rpzh.service.NodeService;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.NodeDataVo;
import com.rpzh.vo.NodeVo;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 节点管理
 * @Author liubc
 * @create 2022/3/3 10:57
 * @Description: good good study,day day up
 */
@Service
public class NodeServiceImpl implements NodeService {

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private MonitoringCenterService monitoringCenterService;
    /**
     * 添加节点
     *
     * @param nodeDto
     * @return
     */
    @Override
    public int addNode(NodeDto nodeDto) {
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        nodeDto.setCreatedUser(username);
        int addFlag = nodeDao.addNode(nodeDto);
        return addFlag;
    }

    @Override
    public String findNodeID(String nodeId,Integer id) {
        String nodeIdFind = nodeDao.findNodeID(nodeId,id);
        return nodeIdFind;
    }

    /**
     * 修改设备
     * @param nodeDto
     * @return
     */
    @Override
    public int updateNode(NodeDto nodeDto) {
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        //最后更新人
        nodeDto.setLastUpdatedUser(username);
        String siteType = nodeDto.getSiteRoute().split(",").length ==4? "04":"05";
        nodeDto.setSiteType(siteType);
        nodeDto.setSiteRoute(nodeDto.getSiteRoute()+",["+siteType+":"+nodeDto.getSiteId()+"]");

        int updateFlag = nodeDao.updateNode(nodeDto);
        return updateFlag;
    }

    /**
     * 删除节点
     * @param nodeIds
     * @return
     */
    @Override
    public int deleteNode(List<String> nodeIds) {
        return nodeDao.deleteNode(nodeIds);
    }

    /**
     * 查询节点列表
     * @param nodeDto
     * @return
     */
    @Override
    public PageInfo<NodeVo> findAllNodeList(NodeDto nodeDto) {
        if (!ToolUtil.isEmpty(nodeDto.getPage())&&!ToolUtil.isEmpty(nodeDto.getSize())){
            int page = nodeDto.getPage();
            int size = nodeDto.getSize();
            //静态分页
            PageHelper.startPage(page, size);
        }
        if (!ToolUtil.isEmpty(nodeDto.getEndTime())){
            nodeDto.setEndTime(nodeDto.getEndTime()+" 23:59:59");
        }
        List<NodeVo> nodeVoList = nodeDao.findAllNodeList(nodeDto);
        return new PageInfo<>(nodeVoList);
    }

    /**
     * 根据设备id查询设备数量
     *
     * @param nodeId
     * @return
     */
    @Override
    public int findNodeCountById(String nodeId) {
        int count = nodeDao.findNodeCountById(nodeId);
        return count;
    }

    /**
     * 替换设备id
     *
     * @param nodeDto
     * @return
     */
    @Override
    @Transactional
    public int replaceNodeId(NodeDto nodeDto) {
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        nodeDto.setLastUpdatedUser(username);
        //插入设备替换日志
        int insertFlag = nodeDao.insertReplaceNodeIdLog(nodeDto);
        if (insertFlag>0){
            return nodeDao.replaceNodeId(nodeDto);
        }
        return -1;
    }

    /**
     * 设备替换日志查询
     * @param nodeDto
     * @return
     */
    @Override
    public PageInfo<NodeVo> findNodeReplaceLog(NodeDto nodeDto) {
        if (!ToolUtil.isEmpty(nodeDto.getPage())&&!ToolUtil.isEmpty(nodeDto.getSize())){
            int page = nodeDto.getPage();
            int size = nodeDto.getSize();
            //静态分页
            PageHelper.startPage(page, size);
        }
        List<NodeVo> nodeVoList = nodeDao.findNodeReplaceLog(nodeDto);
        return new PageInfo<>(nodeVoList);
    }

    /**
     * 设备解垛
     *
     * @param nodeDto
     * @return
     */
    @Override
    public int unbindStack(NodeDto nodeDto) {
        int updateFlag = nodeDao.unbindStack(nodeDto.getIds());
        return updateFlag;
    }

    /**
     * 查询未入垛设备
     *
     * @param nodeDto
     * @return
     */
    @Override
    public PageInfo<NodeVo> findNoEnterStack(NodeDto nodeDto) {
        if (!ToolUtil.isEmpty(nodeDto.getPage())&&!ToolUtil.isEmpty(nodeDto.getPage())){
            int page = nodeDto.getPage();
            int size = nodeDto.getSize();
            //静态分页
            PageHelper.startPage(page, size);
        }
        List<NodeVo> nodeVoList = nodeDao.findNoEnterStack(nodeDto);
        return new PageInfo<>(nodeVoList);
    }

    /**
     * 设备入垛
     * @param nodeDto
     * @return
     */
    @Override
    @Transactional
    public int enterStack(NodeDto nodeDto) {
        return nodeDao.enterStack(nodeDto);
    }

    /**
     * 批量导入设备
     *
     * @param nodeDate
     * @param nodeType
     * @return
     */
    @Override
    public Result exportNode(List<Map<Integer, String>> nodeDate, String nodeType) {
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        //获取时间
        String dateTime = DateUtil.dateNew(new Date()).toString();
        //存放数据
        List<NodeVo> nodeVoList = new ArrayList<>();
        for (int i = 0; i < nodeDate.size(); i++) {
            NodeVo nodeVo = new NodeVo();
            //记录行数,用于返回
            int row = 2 + i;
            //获取每一行数据
            Map<Integer, String> integerStringMap = nodeDate.get(i);
            //获取设备id
            String nodeId = integerStringMap.get(0).trim();
            if (ToolUtil.isEmpty(nodeId)){
                return new Result(false, StatusCode.ERROR, "第" + row + "行设备id不能为空");
            }else {
                nodeVo.setNodeId(nodeId);
            }
            //判断设备是否已经存在
            int nodeIdCount = nodeDao.findNodeIdCount(nodeId);
            if (nodeIdCount > 0){
                return new Result(false, StatusCode.ERROR, "第" + row + "行设备id:" + nodeId + "已经存在,禁止添加");
            }
            //设置节点类型
            nodeVo.setNodeType(nodeType);
            nodeVo.setCreateTime(dateTime);
            nodeVo.setCreatedUser(username);
            nodeVoList.add(nodeVo);
        }
        int addFlag = nodeDao.exportNode(nodeVoList);
        return new Result(true,StatusCode.OK,"批量导入成功");
    }


    /**
     * 查询设备类型
     * @param nodeDto
     * @return
     */
    @Override
    public String findNodeType(NodeDto nodeDto) {
        String nodeId = nodeDto.getNodeId();
        String nodeType = nodeDao.findNodeType(nodeId);
        return nodeType;
    }

    /**
     * 模糊查询设备id
     *
     * @param nodeDto
     * @return
     */
    @Override
    public List<NodeVo> findVagueNode(NodeDto nodeDto) {
        List<NodeVo> nodeVoList = nodeDao.findVagueNode(nodeDto);
        return nodeVoList;
    }

    /**
     * 测试方法
     *
     * @param nodeId
     * @param nodeType
     * @param createTime
     * @return
     */
    @Override
    public List<NodeVo> testFindNode(String nodeId, String nodeType, String createTime,String startTime,String endTime) {
        List<NodeVo> list = nodeDao.testFindNode(nodeId,nodeType,createTime,startTime,endTime);
        return list;
    }

    @Override
    @Transactional
    public Integer updateNodes(List<NodeDto> nodeDtoList) {
        AtomicInteger updateFlag = new AtomicInteger();
        nodeDtoList.forEach((nodeDto) -> {
            nodeDao.updateNode(nodeDto);
            updateFlag.getAndIncrement();
        });
        return updateFlag.get();
    }

    @Override
    public Integer updateNodes(String data) {
        List<Map> list = JSONObject.parseObject(data, List.class);
        AtomicInteger updateFlag = new AtomicInteger();
        list.forEach(map->{
            Map newMap = new HashMap();
            Map<String,String> intermediate = Collections.checkedMap(newMap, String.class, String.class);
            intermediate.putAll(map);
            nodeDao.updateNode(intermediate);
            updateFlag.getAndIncrement();
        });
        return updateFlag.get();
    }

    /**
     * 批量修改设备
     * @param stackId
     * @param nodeDtoList
     * @return
     */
    public Integer updateNodes(Integer stackId, List<NodeDto> nodeDtoList) {
        nodeDtoList.stream().forEach((nodeDto) -> {
            nodeDto.setStackId(stackId);
        });
        return updateNodes(nodeDtoList);
    }

    /**
     * 通过垛位查询垛位下的所有设备
     * @param stackId
     * @return
     */
    @Override
    public List<NodeVo> findNodeByStackId(Integer stackId) {
        return nodeDao.findNodeByStackId(stackId);
    }

    /**
     * 查询设备状态
     * @param id
     * @return
     */
    @Override
    public Integer findNodeStatus(Integer id) {
        return nodeDao.findNodeStatus(id);
    }
    @Override
    public Result flipStack(MaintainPlanDto maintainPlanDto) {
        List<String> nodeDtoList = maintainPlanDto.getNodeDtoList();
        nodeDtoList.forEach(nodeDtostr ->
                nodeDao.updateNode(JSONObject.parseObject(nodeDtostr,NodeDto.class)));
        return new Result(true,StatusCode.OK,"翻垛成功");
    }



    /**
     * 查询设备历史记录
     * @param nodeDto
     * @return
     */
    @Override
    public List<NodeDataVo> findNodeHistory(NodeDto nodeDto) {
        NodeTypeEnum nodeType = NodeTypeEnum.getNodeType(nodeDto.getNodeType());
        if (!ToolUtil.isEmpty(nodeType)){
            Map map = monitoringCenterService.putCacheWarningColor();
            if (!ToolUtil.isEmpty(nodeDto.getEndTime())){
                nodeDto.setEndTime(nodeDto.getEndTime()+" 23:59:59");
            }
            List<NodeDataVo> nodeDataVos = nodeDao.findNodeDataHistory(nodeDto, nodeType.getTableName(), nodeType.getTableColumn());
            for (int i = 0; i < nodeDataVos.size(); i++) {
                setWarningColor(nodeDataVos.get(i),map);
            }

            return nodeDataVos;
        }
        return null;
    }

    /**
     * 设置预警颜色
     * @param nodeDataVo
     * @param map
     */
    public static void setWarningColor(NodeDataVo nodeDataVo,Map map){
            if (nodeDataVo.getAlertLevel()==1){
                nodeDataVo.setWarningColor(map.get("one").toString());
            }else if (nodeDataVo.getAlertLevel()==2){
                nodeDataVo.setWarningColor(map.get("two").toString());
            }else if (nodeDataVo.getAlertLevel()==3){
                nodeDataVo.setWarningColor(map.get("three").toString());
            }else {
                nodeDataVo.setWarningColor("");
        }
    }
}