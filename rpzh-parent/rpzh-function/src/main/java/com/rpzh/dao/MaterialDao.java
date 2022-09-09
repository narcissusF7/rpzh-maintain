package com.rpzh.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.rpzh.vo.MaterialVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/05/06/11:35
 * @Description: 功能
 */
@Mapper
public interface MaterialDao {

    @DS("db2")
    List<String> findQuickCode();

    @DS("db2")
    List<String> findMaterialCode(@Param("stackNo")String stackNo);

    @DS("db2")
    Double getAllMaintainCount(@Param("stackNo")String stackNo);

    @DS("db2")
    List<MaterialVo> findCount(@Param("stackNo")String stackNo);

    @DS("db2")
    List<String> findAllMaterialCodes();

    @DS("db2")
    List<String> findNameCn(@Param("materialCode")String materialCode);
}
