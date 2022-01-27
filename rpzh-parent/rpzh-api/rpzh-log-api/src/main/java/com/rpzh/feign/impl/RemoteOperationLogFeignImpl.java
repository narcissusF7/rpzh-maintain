package com.rpzh.feign.impl;

import com.rpzh.entity.Result;
import com.rpzh.feign.RemoteOperationLogFeign;
import org.springframework.stereotype.Component;

@Component
public class RemoteOperationLogFeignImpl implements RemoteOperationLogFeign {
    /**
     * @param operationLogJson 操作日志JSON
     * @return com.rpzh.entity.Result
     * @description 操作日志添加
     * @author wbl
     * @date 2021/6/10 9:46
     */
    @Override
    public Result insertOperationLog(String operationLogJson) throws Exception {
        throw new Exception("日志服务器异常");
    }
}
