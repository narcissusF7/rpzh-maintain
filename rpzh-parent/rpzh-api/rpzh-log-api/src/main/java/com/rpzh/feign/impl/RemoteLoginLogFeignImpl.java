package com.rpzh.feign.impl;

import com.rpzh.entity.Result;
import com.rpzh.feign.RemoteLoginLogFeign;
import org.springframework.stereotype.Component;

@Component
public class RemoteLoginLogFeignImpl implements RemoteLoginLogFeign {
    /**
     * @param loginLogStr 日志
     * @return com.rpzh.entity.Result<com.rpzh.com.rpzh.pojo.LoginLog>
     * @version 0.1.0
     * @description 登录日志添加
     * @author wbl
     * @date 2021/6/7 19:08
     */
    @Override
    public Result insertLoginLog(String loginLogStr) throws Exception {
        throw new Exception("登录日志异常");
    }
}
