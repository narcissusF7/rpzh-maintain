package com.rpzh.service;

import com.rpzh.dto.EarlyWarningDto;
import com.rpzh.entity.Result;
import com.rpzh.vo.WarningVo;

import java.util.Date;
import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/05/20/14:46
 * @Description: 功能
 */
public interface EarlyWarningService {
    Result findEarlyWarning(EarlyWarningDto earlyWarningDto);

}
