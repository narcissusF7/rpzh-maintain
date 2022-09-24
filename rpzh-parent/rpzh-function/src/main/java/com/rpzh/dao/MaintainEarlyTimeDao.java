package com.rpzh.dao;

import com.rpzh.dto.MaintainEarlyTimeDto;
import com.rpzh.pojo.MaintainEarlyTime;
import com.rpzh.vo.MaintainEarlyTimeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MaintainEarlyTimeDao {
    /**
     * 修改养护预警时间
     * @param maintainEarlyTime
     * @return
     */
    Integer updateMaintain(@Param("maintainEarlyTime") MaintainEarlyTime maintainEarlyTime);

    /**
     * 通过养护计划id查询养护预警时间
     * @param maintainPlanId
     * @return
     */
    MaintainEarlyTime findByMaintainPlanId(@Param("maintainPlanId") Integer maintainPlanId);

    List<MaintainEarlyTimeVo> findMaintainEarlyTimes(@Param("maintainEarlyTimeDto") MaintainEarlyTimeDto maintainEarlyTimeDto);
}
