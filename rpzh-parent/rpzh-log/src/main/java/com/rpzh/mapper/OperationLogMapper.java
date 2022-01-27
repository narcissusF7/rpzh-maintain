package com.rpzh.mapper;

import com.rpzh.entity.LogOperation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}
