package com.rpzh.service.impl;

import com.github.pagehelper.PageHelper;
import com.rpzh.dao.MaintainEarlyTimeDao;
import com.rpzh.dto.MaintainEarlyTimeDto;
import com.rpzh.entity.ToolUtil;
import com.rpzh.pojo.MaintainEarlyTime;
import com.rpzh.service.MaintainEarlyTimeService;
import com.rpzh.vo.MaintainEarlyTimeVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MaintainEarlyServiceImpl implements MaintainEarlyTimeService {
    @Resource
    MaintainEarlyTimeDao maintainEarlyTimeDao;
    @Override
    public Integer updateMaintain(MaintainEarlyTime maintainEarlyTime) {
        Integer updateFlag = maintainEarlyTimeDao.updateMaintain(maintainEarlyTime);
        return updateFlag;
    }

    @Override
    public MaintainEarlyTime findByMaintainPlanId(Integer maintainPlanId) {
        MaintainEarlyTime maintainEarlyTime = maintainEarlyTimeDao.findByMaintainPlanId(maintainPlanId);
        return maintainEarlyTime;
    }

    @Override
    public List<MaintainEarlyTimeVo> findMaintainEarlyTimes(MaintainEarlyTimeDto maintainEarlyTimeDto) {
        String siteRoute = maintainEarlyTimeDto.getSiteRoute();
        if (siteRoute.split(",").length==5){
            maintainEarlyTimeDto.setSiteRoute(null);
        }else if(ToolUtil.isNotEmpty(siteRoute)){
            maintainEarlyTimeDto.setSiteRoute(siteRoute+",[0"+siteRoute.split(",").length+":"+maintainEarlyTimeDto.getSiteId()+"]");
            maintainEarlyTimeDto.setSiteId(null);
        }

        //分页查询
        PageHelper.startPage(maintainEarlyTimeDto.getPage(),maintainEarlyTimeDto.getSize());
        List<MaintainEarlyTimeVo> list = maintainEarlyTimeDao.findMaintainEarlyTimes(maintainEarlyTimeDto);

        //把累计时间换算成天
        list.forEach(maintainEarlyTimeVo -> {
            Double tempAllEarlyTime = maintainEarlyTimeVo.getTempAllEarlyTime();
            Double coreTempAllEarlyTime = maintainEarlyTimeVo.getCoreTempAllEarlyTime();
            Double humidityAllEarlyTime = maintainEarlyTimeVo.getHumidityAllEarlyTime();
            Double oxygenAllEarlyTime = maintainEarlyTimeVo.getOxygenAllEarlyTime();
            maintainEarlyTimeVo.setCoreTempAllEarlyTime(coreTempAllEarlyTime%24 ==0?coreTempAllEarlyTime/24:coreTempAllEarlyTime/24+1);
            maintainEarlyTimeVo.setTempAllEarlyTime(tempAllEarlyTime%24 ==0?tempAllEarlyTime/24:tempAllEarlyTime/24+1);
            maintainEarlyTimeVo.setOxygenAllEarlyTime(oxygenAllEarlyTime%24 ==0?oxygenAllEarlyTime/24:oxygenAllEarlyTime/24+1);
            maintainEarlyTimeVo.setHumidityAllEarlyTime(humidityAllEarlyTime%24 ==0?humidityAllEarlyTime/24:humidityAllEarlyTime/24+1);
        });
        return list;
    }
}
