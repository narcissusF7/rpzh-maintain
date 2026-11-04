package com.rpzh.feign.impl;

import com.rpzh.entity.Result;
import com.rpzh.feign.RfidFeign;

import java.util.List;

/**
 * &#064;Author:  zyq
 * &#064;Date:  2022/05/23/11:47
 * @Description: 功能
 */
public class RfidFeignImpl implements RfidFeign {
    @Override
    public List<String> findIps(){
        throw new RuntimeException("服务器异常");
    }

    @Override
    public Result updateStatus(String ip,String status){
        throw new RuntimeException("服务器异常");
    }

}
