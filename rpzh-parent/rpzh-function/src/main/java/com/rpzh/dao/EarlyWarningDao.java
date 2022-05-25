package com.rpzh.dao;

import com.rpzh.dto.EarlyWarningDto;
import com.rpzh.vo.EarlyWarningVo;
import com.rpzh.vo.WarningCountVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/05/19/14:02
 * @Description: 预警表
 */
@Mapper
public interface EarlyWarningDao {
    List<WarningCountVo> findEarlyWarningCount(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);
    EarlyWarningVo findEarlyWarning(@Param("earlyWarningDto") EarlyWarningDto earlyWarningDto);
}
