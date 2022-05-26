package com.rpzh.service.impl;

import com.rpzh.dao.NodeOperationDao;
import com.rpzh.dto.NodeOperationDto;
import com.rpzh.service.NodeOperationService;
import com.rpzh.vo.NodeOperationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class NodeOperationServiceImpl implements NodeOperationService {
    @Autowired
    NodeOperationDao operationDao;

    public NodeOperationServiceImpl() {
    }

    @Override
    @Transactional
    public int insertNodeOperation(NodeOperationDto nodeOperationDto) {
        List<NodeOperationDto> nodeOperationDtos = Arrays.asList(nodeOperationDto);
        return operationDao.bulkInsert(nodeOperationDtos);
    }

    @Override
    @Transactional
    public int insertNodeOperation(List<NodeOperationDto> nodeOperationDtos) {
        return operationDao.bulkInsert(nodeOperationDtos);
    }

    @Override
    public List<NodeOperationVo> selectNodeOperation(NodeOperationDto nodeOperationDto) {
        return operationDao.selectNodeOperation(nodeOperationDto);
    }
}
