package com.rpzh.service;

import com.rpzh.dto.NodeOperationDto;
import com.rpzh.vo.NodeOperationVo;

import java.util.List;

public interface NodeOperationService {

    /**
     * 操作记录批量插入
     * @param nodeOperationDto
     * @return
     */
    int insertNodeOperation(NodeOperationDto nodeOperationDto);
    int insertNodeOperation(List<NodeOperationDto> nodeOperationDtos);

    List<NodeOperationVo> selectNodeOperation(NodeOperationDto nodeOperationDto);

}
