package com.rpzh.feign.impl;

import com.rpzh.entity.Result;
import com.rpzh.feign.UserPhoneFeign;


import java.util.List;
import java.util.Map;

/**
 * @Author: zyq
 * @Date: 2022/05/23/11:47
 * @Description: UserPhoneFeign 的降级实现
 */
public class UserPhoneFeignFallback implements UserPhoneFeign {

    //根据id查询手机号
    @Override
    public Result findPhoneNumberById(String id) {
        throw new RuntimeException("服务器异常");
    }

}
