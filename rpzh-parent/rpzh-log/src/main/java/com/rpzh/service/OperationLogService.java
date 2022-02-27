package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.OperationLogDto;
import com.rpzh.entity.LogOperation;
import com.rpzh.entity.LogSystemModule;
import com.rpzh.entity.PageResult;

/**
 * @author wbl
 * @version 0.1.0
 * @create 2021-06-10 09:06
 * @description 操作日志服务层
 */
public interface OperationLogService {

    /**
     * @param operationLog 操作日志实体类
     * @return int
     * @description 添加一条操作日志
     * @author wbl
     * @date 2021/6/10 9:06
     */
    int insertOperationLog(LogOperation operationLog);
    /**
     * @param operationLogDto 日志查询条件信息
     * @return int
     * @description 添加一条操作日志
     * @author wbl
     * @date 2021/6/10 9:06
     */
    PageInfo<LogOperation> findOperationLog(OperationLogDto operationLogDto);

    LogSystemModule findSystemModule();
}
