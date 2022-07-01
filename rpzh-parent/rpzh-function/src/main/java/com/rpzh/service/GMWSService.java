package com.rpzh.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.rpzh.vo.GWMSNodeDataVo;
import com.rpzh.vo.NodeDataVo;

import java.util.List;
import java.util.Map;

/**
 * @Author: ljj
 * @Date: 2022/06/28/14:59
 * @Description: 功能
 */
public interface GMWSService {
    List<GWMSNodeDataVo> findMaintainData();

    List<Map<String, String>> findUsableTobacco();
}
