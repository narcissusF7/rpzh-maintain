package com.rpzh.service.impl;

import com.rpzh.dao.AirConditioningDao;
import com.rpzh.service.AirConditionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AirConditionServiceImpl implements AirConditionService {

    @Autowired
    AirConditioningDao airConditioningDao;

    @Override
    public Integer insertAirCondtionTags(List<Map<String,String>> integerStringMap) {
        return airConditioningDao.insertAirCondtionTags(integerStringMap);
    }
}
