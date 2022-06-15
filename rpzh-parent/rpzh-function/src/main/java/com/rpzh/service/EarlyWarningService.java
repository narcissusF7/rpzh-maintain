package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.EarlyWarningDto;
import com.rpzh.entity.Result;
import com.rpzh.vo.EarlyWarningVo;

/**
 * @Author: zyq
 * @Date: 2022/05/20/14:46
 * @Description: 功能
 */
public interface EarlyWarningService {
    Result findEarlyWarningInfo(EarlyWarningDto earlyWarningDto);
    Result findEarlyWarning(EarlyWarningDto earlyWarningDto);
    PageInfo<EarlyWarningVo> findHistoryWarning(EarlyWarningDto earlyWarningDto);
    Result findDetailsEarlyWarning(EarlyWarningDto earlyWarningDto);
    Result findWarningCount(EarlyWarningDto earlyWarningDto);
}
