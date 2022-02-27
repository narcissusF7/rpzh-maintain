package com.rpzh.mapper;

import com.rpzh.dto.OperationLogDto;
import com.rpzh.entity.LogOperation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志mapper
 */
@Mapper
public interface OperationLogMapper {
    /**
     * 插入一条日志
     *
     * @param logOperation
     * @return
     */
    int insertLog(@Param("logOperation") LogOperation logOperation);

    /**
     * 分页查询日志
     * @param operationLogDto
     * @return
     */
    List<LogOperation> findOperationLog(@Param("operationLogDto") OperationLogDto operationLogDto);
}
