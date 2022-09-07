package com.rpzh.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.ViewDataDao;
import com.rpzh.dto.ViewDataDto;
import com.rpzh.service.ViewDataService;
import com.rpzh.vo.ViewDataVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 对接db2视图,从视图中取数据
 * @Author liubc
 * @create 2022-09-07 14:27
 * @Description: good good study,day day up
 */
@Service
public class ViewDataServiceImpl implements ViewDataService {


    @Autowired
    private ViewDataDao viewDataDao;


    /**
     * 查询实时库条码信息
     *
     * @param viewDataDto
     * @return
     */
    @Override
    public PageInfo<ViewDataVo> vmMaintainInventorybatch(ViewDataDto viewDataDto) {
        int page = viewDataDto.getPage();
        int size = viewDataDto.getSize();
        //静态分页
        PageHelper.startPage(page, size);
        List<ViewDataVo> viewDataVoList = viewDataDao.vmMaintainInventorybatch(viewDataDto);
        return new PageInfo<ViewDataVo>(viewDataVoList);
    }
}