package com.rpzh.service;

import com.rpzh.vo.MaterialVo;

import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/05/07/15:11
 * @Description: 功能
 */
public interface MaterialService {
    MaterialVo getMaterialInfoBySiteId(Integer siteId);

    List<String> findMaterialCode(Integer stackId);

    List<String> findYear(Integer stackId,String materialCode);
}
