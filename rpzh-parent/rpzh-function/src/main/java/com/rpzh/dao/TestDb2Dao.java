package com.rpzh.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @Author liubc
 * @create 2022-08-30 10:56
 * @Description: good good study,day day up
 * 测试db2数据库查询
 */
@Mapper
public interface TestDb2Dao {

    //测试
    @DS("db2")
    List<Map> findCount();
}
