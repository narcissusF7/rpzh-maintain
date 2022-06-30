package com.rpzh.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.rpzh.vo.GWMSNodeDataVo;
import com.rpzh.vo.NodeDataVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GMWSDao {

    @DS("clickhouse")
    List<GWMSNodeDataVo> findNodeData(@Param("tableName") String tableName, @Param("column") String column);
}
