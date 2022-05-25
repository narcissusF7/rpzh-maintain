package com.rpzh.service.impl;

import com.rpzh.dao.EarlyWarningDao;
import com.rpzh.service.EarlyWarningService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author: zyq
 * @Date: 2022/05/20/14:46
 * @Description: 功能
 */
@Service
public class EarlyWarningServiceImpl implements EarlyWarningService {
    @Resource
    private EarlyWarningDao earlyWarningDao;
}
