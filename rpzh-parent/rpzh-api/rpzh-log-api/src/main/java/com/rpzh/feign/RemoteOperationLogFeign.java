package com.rpzh.feign;

import com.rpzh.entity.Result;
import com.rpzh.feign.impl.RemoteOperationLogFeignImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author wbl
 * @version 0.1.0
 * @create 2022-01-26 09:01
 * @description 操作日志Feign调用
 */
@FeignClient(name = "rpzh-log", path = "/operationlog",fallback = RemoteOperationLogFeignImpl.class)
public interface RemoteOperationLogFeign {

    /**
     * @param operationLogJson 操作日志JSON
     * @return com.rpzh.entity.Result
     * @description 操作日志添加
     * @author wbl
     * @date 2021/6/10 9:46
     */
    @PostMapping(consumes = "application/json")
    Result insertOperationLog(@RequestParam("operationLogJson")String operationLogJson) throws Exception;

}
