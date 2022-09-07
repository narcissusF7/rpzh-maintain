package com.rpzh.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.rpzh.dto.ViewDataDto;
import com.rpzh.vo.ViewDataVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 对接db2数据库,从视图中取数据
 * @Author liubc
 * @create 2022-09-07 14:30
 * @Description: good good study,day day up
 */
@Mapper
public interface ViewDataDao {


    /**
     * 查询实时库条码信息
     * @param viewDataDto
     * @return
     */
    @DS("db2")
    List<ViewDataVo> vmMaintainInventorybatch(@Param("viewDataDto") ViewDataDto viewDataDto);
}
