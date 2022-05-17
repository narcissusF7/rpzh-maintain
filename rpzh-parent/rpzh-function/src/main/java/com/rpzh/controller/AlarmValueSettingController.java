package com.rpzh.controller;

import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.service.AlarmValueSettingService;
import com.rpzh.util.StringUtils;
import com.rpzh.vo.AlarmValueSettingVo;
import io.swagger.annotations.ApiImplicitParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;

/**
 * @Author: zyq
 * @Date: 2022/05/07/13:46
 * @Description: 功能
 */
@RestController
@RequestMapping("/maintain/alarmValueSetting")
public class AlarmValueSettingController {
    @Resource
    private AlarmValueSettingService alarmValueSettingService;
    @GetMapping(value = "/findAlarmValueSettingInfo")
    public Result findMaintainPlanInfo(AlarmValueSettingDto alarmValueSettingDto) {
        String materialCodeYear = alarmValueSettingDto.getMaterialCodeYear();
        String phases = alarmValueSettingDto.getPhases();
        String maintenceStyle = alarmValueSettingDto.getMaintenceStyle();
        if(StringUtils.isEmpty(materialCodeYear) || StringUtils.isEmpty(phases) || StringUtils.isEmpty(maintenceStyle)){
            return new Result(false, StatusCode.ERROR, "物料等级,烟叶年份,养护阶段或养护方法不能为空!");
        }
        AlarmValueSettingVo alarmValueSetting = alarmValueSettingService.findMaintainPlanInfo(alarmValueSettingDto);
        return new Result(true, StatusCode.OK, "查询成功!",alarmValueSetting);
    }
}
