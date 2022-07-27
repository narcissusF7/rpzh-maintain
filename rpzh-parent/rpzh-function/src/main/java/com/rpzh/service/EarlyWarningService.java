package com.rpzh.service;
import com.github.pagehelper.PageInfo;
import com.rpzh.dto.EarlyWarningDto;
import com.rpzh.entity.Result;
import com.rpzh.vo.*;

import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/05/20/14:46
 * @Description: 功能
 */
public interface EarlyWarningService {
    Result findEarlyWarningInfo(EarlyWarningDto earlyWarningDto);
    Result findEarlyWarningByWarningType(EarlyWarningDto earlyWarningDto);
    Result findEarlyWarningBySiteId(EarlyWarningDto earlyWarningDto);
    PageInfo<EarlyWarningVo> findHistoryWarning(EarlyWarningDto earlyWarningDto);
    Result findWarningCount(EarlyWarningDto earlyWarningDto);
    Integer misreport(Integer id);
    List<RepairDataVo> getRepairData(Integer stackId, Integer areaId);
    Result findNodeData(EarlyWarningDto earlyWarningDto);
    int updateRepairId(String affirm, String deviceId, String id, String principal,String dealTime);
}
