package com.rpzh.feign.impl;

import com.rpzh.entity.Result;
import com.rpzh.feign.UserFeign;


import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/05/23/11:47
 * @Description: 功能
 */
public class UserFeignImpl implements UserFeign {
    @Override
    public Result findUsernameById(List<Integer> ids) {
        throw new RuntimeException("服务器异常");
    }

    @Override
    public Result findPhoneNumberById(String id) {
        throw new RuntimeException("服务器异常");
    }
}
