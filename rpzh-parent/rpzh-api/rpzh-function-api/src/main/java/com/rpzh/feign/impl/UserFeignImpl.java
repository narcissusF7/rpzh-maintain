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
    //根据id查询用户名
    @Override
    public List<String> findUsernameById(List<Integer> ids) {
        throw new RuntimeException("服务器异常");
    }

    //根据id查询手机号
    @Override
    public Result findPhoneNumberById(String id) {
        throw new RuntimeException("服务器异常");
    }
}
