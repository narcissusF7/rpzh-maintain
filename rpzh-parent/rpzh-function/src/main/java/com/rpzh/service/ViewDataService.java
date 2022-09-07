package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.ViewDataDto;
import com.rpzh.vo.ViewDataVo;

/**
 * 对接db2视图,从视图中取数据
 * @Author liubc
 * @create 2022-09-07 14:23
 * @Description: good good study,day day up
 */
public interface ViewDataService {

    /**
     * 查询实时库条码信息
     * @param viewDataDto
     * @return
     */
    PageInfo<ViewDataVo> vmMaintainInventorybatch(ViewDataDto viewDataDto);
}
