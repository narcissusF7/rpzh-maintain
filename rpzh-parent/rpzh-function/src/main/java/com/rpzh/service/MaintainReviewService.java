package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.pojo.EarlyWarning;
import com.rpzh.vo.EarlyWarningVo;
import com.rpzh.vo.MaintainPlanVo;
import java.util.List;
import java.util.Map;

/**
 * @Author: zyq
 * @Date: 2022/07/16/16:45
 * @Description: 功能
 */
public interface MaintainReviewService {
    PageInfo<MaintainPlanVo> findReviewTable(MaintainPlanDto maintainPlanDto);
    List<MaintainPlanVo> findMaintainPlanReview(Integer id);
    MaintainPlanVo findMaintainBaseInfo(Integer id);
    List<EarlyWarningVo> findEarlyWarningReview(Integer id);
    List<Map> findDataReview(String tableType,Integer id,String startTime,String endTime);
}
