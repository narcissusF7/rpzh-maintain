package com.rpzh.service;

import com.rpzh.dto.MaintainEarlyTimeDto;
import com.rpzh.pojo.MaintainEarlyTime;
import com.rpzh.vo.MaintainEarlyTimeVo;

import java.util.List;

public interface MaintainEarlyTimeService {
    Integer updateMaintain(MaintainEarlyTime maintainEarlyTime);

    MaintainEarlyTime findByMaintainPlanId(Integer maintainPlanId);

    List<MaintainEarlyTimeVo> findMaintainEarlyTimes(MaintainEarlyTimeDto maintainEarlyTimeDto);
}
