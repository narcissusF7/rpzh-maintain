package com.rpzh.service.impl;

import com.rpzh.dao.AlarmValueSettingDao;
import com.rpzh.dao.EarlyWarningDao;
import com.rpzh.dao.MaintainPlanDao;
import com.rpzh.dto.EarlyWarningDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.entity.WarningTypeDefault;
import com.rpzh.service.EarlyWarningService;
import com.rpzh.vo.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/05/20/14:46
 * @Description: 功能
 */
@Service
public class EarlyWarningServiceImpl implements EarlyWarningService {
    @Resource
    private EarlyWarningDao earlyWarningDao;
    @Resource
    private MaintainPlanDao maintainPlanDao;
    @Resource
    private AlarmValueSettingDao alarmValueSettingDao;
    @Override
    public Result findEarlyWarningInfo(EarlyWarningDto earlyWarningDto) {
        List<WarningCountVo> warningCount = earlyWarningDao.findEarlyWarningCount(earlyWarningDto);
        if(warningCount.size()==0){
            return new Result<>(true, StatusCode.OK, "查询成功,无预警信息");
        }
        String warningType = warningCount.get(0).getWarningType();
        earlyWarningDto.setWarningType(warningType);
        String type = "";
        for (WarningCountVo warningCountVo : warningCount) {
            type = type+" "+warningCountVo.getWarningType();
        }
        WarningCountVo warningCountVo = new WarningCountVo();
        if(!type.contains("01")){
            warningCountVo.setWarningType("01");
            warningCount.add(warningCountVo);
        }
        if(!type.contains("02")){
            warningCountVo.setWarningType("02");
            warningCount.add(warningCountVo);
        }
        if(!type.contains("03")){
            warningCountVo.setWarningType("03");
            warningCount.add(warningCountVo);
        }
        if(!type.contains("04")){
            warningCountVo.setWarningType("04");
            warningCount.add(warningCountVo);
        }
        if(!type.contains("05")){
            warningCountVo.setWarningType("05");
            warningCount.add(warningCountVo);
        }
        if(!type.contains("06")){
            warningCountVo.setWarningType("06");
            warningCount.add(warningCountVo);
        }
        if(!type.contains("07")){
            warningCountVo.setWarningType("07");
            warningCount.add(warningCountVo);
        }
        if(!type.contains("08")){
            warningCountVo.setWarningType("08");
            warningCount.add(warningCountVo);
        }
        if(!type.contains("09")){
            warningCountVo.setWarningType("09");
            warningCount.add(warningCountVo);
        }
        WarningVo warningVo = new WarningVo();
        warningVo.setWarningCount(warningCount);
        List<EarlyWarningVo> earlyWarningVos = earlyWarningDao.findEarlyWarning(earlyWarningDto);
        if(warningType.equals("01")){
            earlyWarningVos.forEach(p -> p.setWarningNews("设备丢包"));
        }
        warningVo.setEarlyWarning(earlyWarningVos);
        return new Result<>(true, StatusCode.OK, "查询成功",warningVo);
    }
    @Override
    public Result findEarlyWarning(EarlyWarningDto earlyWarningDto) {
        if(ToolUtil.isEmpty(earlyWarningDto.getWarningType())){
            return new Result<>(false, StatusCode.ERROR, "预警类型不能为空");
        }
        List<EarlyWarningVo> earlyWarningVos = earlyWarningDao.findEarlyWarning(earlyWarningDto);
        if(earlyWarningDto.getWarningType().equals("01")){
            earlyWarningVos.forEach(p -> p.setWarningNews("设备丢包"));
        }
        return new Result<>(true, StatusCode.OK, "查询成功",earlyWarningVos);
    }

    @Override
    public Result findDetailsEarlyWarning(EarlyWarningDto earlyWarningDto) {
        MaintainPlanVo maintainPlan = maintainPlanDao.findMaintainStackId(earlyWarningDto.getStackId());
        String newDate = earlyWarningDao.findNewDate(earlyWarningDto);
        AlarmValueSettingVo alarmValueSetting = alarmValueSettingDao.findAlarmValueSettingById(maintainPlan.getAlarmValueSettingId());
        /*switch (earlyWarningDto.getWarningType()){
            case ;
            case ;
            case ;
        }*/
        return null;
    }
}
