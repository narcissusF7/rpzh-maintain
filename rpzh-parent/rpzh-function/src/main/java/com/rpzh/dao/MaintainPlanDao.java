package com.rpzh.dao;

import dto.MaintainPlanDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pojo.MaintainPlan;
import vo.MaintainPlanVo;

import java.util.List;

@Mapper
public interface MaintainPlanDao {

    int deleteByPrimaryKey(Integer id);

    int insert(MaintainPlan record);

    int insertSelective(MaintainPlan record);

    MaintainPlan selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MaintainPlan record);

    int updateByPrimaryKey(MaintainPlan record);

    List<MaintainPlanVo> findMaintainPlanInfo(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);
}
