package com.rpzh.feign;

import com.rpzh.entity.Result;
import com.rpzh.feign.impl.RemoteLoginLogFeignImpl;
import com.rpzh.feign.impl.RemoteOperationLogFeignImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "rpzh-log", path = "/loginlog",fallback  = RemoteLoginLogFeignImpl.class)
public interface RemoteLoginLogFeign {
    /**
     * @param loginLogStr 日志
     * @return com.rpzh.entity.Result<com.rpzh.com.rpzh.pojo.LoginLog>
     * @version 0.1.0
     * @description 登录日志添加
     * @author wbl
     * @date 2021/6/7 19:08
     */
    @PostMapping(consumes = "application/json")
    Result insertLoginLog(@RequestParam("loginLogStr") String loginLogStr) throws Exception;
}
