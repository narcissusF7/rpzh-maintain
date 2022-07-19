package com.rpzh.dao;

import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.vo.MaintainPlanVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/07/16/16:46
 * @Description: 功能
 */
@Mapper
public interface MaintainReviewDao {
    List<MaintainPlanVo> findReviewTable(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto, @Param("column") String column);

    List<MaintainPlanVo> findMaintainPlanReview(@Param("id") Integer id);

    MaintainPlanVo findMaintainBaseInfo(@Param("id") Integer id);
}
