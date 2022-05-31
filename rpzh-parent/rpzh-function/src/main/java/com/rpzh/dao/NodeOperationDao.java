package com.rpzh.dao;

import com.rpzh.dto.NodeOperationDto;
import com.rpzh.vo.NodeOperationVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NodeOperationDao {
    int bulkInsert(@Param("nodeOperationDtos") List<NodeOperationDto> var1);

    List<NodeOperationVo> selectNodeOperation(@Param("nodeOperationDto") NodeOperationDto var1);
}
