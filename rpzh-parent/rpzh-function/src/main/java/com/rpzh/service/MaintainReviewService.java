package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.vo.MaintainPlanVo;
import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/07/16/16:45
 * @Description: 功能
 */
public interface MaintainReviewService {
    PageInfo<MaintainPlanVo> findReviewTable(MaintainPlanDto maintainPlanDto);
}
