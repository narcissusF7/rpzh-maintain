package com.rpzh.service.impl;

import com.rpzh.dao.AlarmValueSettingDao;
import com.rpzh.service.AlarmValueSettingService;
import com.rpzh.vo.AlarmValueSettingVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author: zyq
 * @Date: 2022/05/07/15:01
 * @Description: 功能
 */
@Service
public class AlarmValueSettingServiceImpl implements AlarmValueSettingService {
    @Resource
    private AlarmValueSettingDao alarmValueSettingDao;

    @Override
    public AlarmValueSettingVo findAlarmValueSettingById(Integer id) {
        AlarmValueSettingVo alarmValueSettingVo = alarmValueSettingDao.findAlarmValueSettingById(id);
        return alarmValueSettingVo;
    }


}
