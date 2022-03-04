package com.rpzh.service.impl;

import com.rpzh.dao.NodeDao;
import com.rpzh.dto.NodeDto;
import com.rpzh.service.NodeService;
import com.rpzh.util.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.stream.events.NotationDeclaration;
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
}