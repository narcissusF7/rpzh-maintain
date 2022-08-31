package com.rpzh.dao;

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

    Double getMaintainCount(@Param("stackId") Integer stackId);

    List<String> getMaterialBatch(@Param("stackId") Integer stackId);

    List<String> findMaterialCode(@Param("stackId") Integer stackId);

    int updateMaterialStatus(@Param("stackId") Integer stackId);

    List<MaterialVo> findCount(@Param("stackId") Integer stackId, @Param("materialCodes") List<String> materialCodes);

    List<String> findAllMaterialCodes();

    List<String> findMaterialCodeByStackId(@Param("stackId") Integer stackId);
}
