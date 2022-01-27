package com.rpzh.service;

import com.alibaba.fastjson.JSONObject;
import com.rpzh.annotation.OperationLog;
import com.rpzh.entity.LogLogin;
import com.rpzh.entity.LogOperation;
import com.rpzh.feign.RemoteLoginLogFeign;
import com.rpzh.feign.RemoteOperationLogFeign;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author wbl
 * 异步调用日志存储
 */
@Service
public class AsyncLogService {

    @Resource
    private RemoteOperationLogFeign remoteOperationLogFeign;
    @Resource
    private RemoteLoginLogFeign remoteLoginLogFeign;

    /**
     * @param operationLog 操作日志实体类
     * @description 添加操作日志
     * @author wbl
     * @date 2021/6/10 9:49
     */
    @Async
    public void saveOperationLog(LogOperation operationLog) throws Exception {
        String s = JSONObject.toJSONString(operationLog);
        remoteOperationLogFeign.insertOperationLog(s);
    }

    /**
     * @param logLogin 登录日志实体类
     * @description 添加操作日志
     * @author wbl
     * @date 2021/6/10 9:49
     */
    @Async
    public void saveLoginLog(LogLogin logLogin) throws Exception {
        String s = JSONObject.toJSONString(logLogin);
        remoteLoginLogFeign.insertLoginLog(s);
    }
}