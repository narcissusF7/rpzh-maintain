package com.rpzh.service.impl;

import com.rpzh.entity.LogOperation;
import com.rpzh.mapper.OperationLogMapper;
import com.rpzh.service.OperationLogService;
import com.rpzh.util.RedisUtil;
import com.rpzh.util.TokenDecode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author wbl
 * @version 0.1.0
 * @create 2021-06-10 09:07
 * @description 操作日志服务层实现类
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Resource
    private OperationLogMapper operationLogMapper;
    private final String accountId = "ACCOUNT_ID_";
    @Resource
    private RedisUtil redisUtil;

    /**
     * @param operationLog 操作日志实体类
     * @return int
     * @description 添加一条操作日志
     * @author wbl
     * @date 2021/6/10 9:06
     */
    @Override
    public int insertOperationLog(LogOperation operationLog) {
      //  String username = TokenDecode.getUserInfo().get("username");
       // operationLog.setLogRecorder(redisUtil.get(accountId + username).toString());
        operationLog.setLogRecorder("zhangsan");
        return operationLogMapper.insertLog(operationLog);
    }
}
