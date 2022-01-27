package com.rpzh.service;

import com.rpzh.entity.LogOperation;

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

}
