package com.rpzh.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.NodeDao;
import com.rpzh.dto.NodeDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.NodeService;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.NodeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.stream.events.NotationDeclaration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    /**
     * 更新设备信息
     *
     * @param nodeId
     * @return
     */
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
        int updateFlag = nodeDao.updateNode(nodeDto);
        return updateFlag;
    }

    /**
     * 删除节点
     * @param ids
     * @return
     */
    @Override
    public int deleteNode(List<Integer> ids) {
        int delFlag = nodeDao.deleteNode(ids);
        return delFlag;
    }

    /**
     * 查询节点列表
     * @param nodeDto
     * @return
     */
    @Override
    public PageInfo<NodeVo> findAllNodeList(NodeDto nodeDto) {
        int page = nodeDto.getPage();
        int size = nodeDto.getSize();
        //静态分页
        PageHelper.startPage(page, size);
        List<NodeVo> nodeVoList = nodeDao.findAllNodeList(nodeDto);
        return new PageInfo<NodeVo>(nodeVoList);
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
        //设备替换,注意:后面要加修改历史数据的设备替换,此处需要添加feign调用,修改历史数据中的设备id
        int updateFlag = nodeDao.replaceNodeId(nodeDto);
        return updateFlag;
    }

    /**
     * 设备替换日志查询
     * @param nodeDto
     * @return
     */
    @Override
    public PageInfo<NodeVo> findNodeReplaceLog(NodeDto nodeDto) {
        int page = nodeDto.getPage();
        int size = nodeDto.getSize();
        //静态分页
        PageHelper.startPage(page, size);
        List<NodeVo> nodeVoList = nodeDao.findNodeReplaceLog(nodeDto);
        return new PageInfo<NodeVo>(nodeVoList);
    }

    /**
     * 设备解垛
     *
     * @param nodeDto
     * @return
     */
    @Override
    public int unbindStack(NodeDto nodeDto) {
        int updateFlag = nodeDao.unbindStack(nodeDto.getId());
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
        int page = nodeDto.getPage();
        int size = nodeDto.getSize();
        //静态分页
        PageHelper.startPage(page, size);
        List<NodeVo> nodeVoList = nodeDao.findNoEnterStack(nodeDto);
        return new PageInfo<NodeVo>(nodeVoList);
    }

    /**
     * 设备入垛
     * @param data
     * @return
     */
    @Override
    public int enterStack(String data) {
        List<Map> list = JSONObject.parseObject(data, List.class);
        int updateFlag = 0;
        for (int i = 0; i < list.size(); i++) {
            Map map = list.get(i);
            //Integer id = map.get("id").toString();
            int id = Integer.parseInt(map.get("id").toString());
            Integer x = Integer.parseInt(map.get("x").toString());
            Integer y = Integer.parseInt(map.get("y").toString());
            Integer z = Integer.parseInt(map.get("z").toString());
            String stack = (String) map.get("stack");
            Integer siteId = Integer.parseInt(map.get("siteId").toString());
            updateFlag = nodeDao.enterStack(id,x,y,z,stack,siteId);
        }
        return updateFlag;
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
    public List<NodeVo> testFindNode(String nodeId, String nodeType, String createTime) {
        List<NodeVo> list = nodeDao.testFindNode(nodeId,nodeType,createTime);
        return list;
    }
}