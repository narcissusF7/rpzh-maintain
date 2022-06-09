package com.rpzh.controller;


import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.service.AlarmValueSettingService;
import com.rpzh.vo.AlarmValueSettingVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author: zyq
 * @Date: 2022/05/07/13:46
 * @Description: 预警设置
 */
@RestController
@CrossOrigin
@RequestMapping("/maintain/alarmValueSetting")
@Api(value = "预警设置类", tags = "预警设置类", description = "预警设置类")
public class AlarmValueSettingController {
    @Resource
    private AlarmValueSettingService alarmValueSettingService;

    @PostMapping(value = "/findAlarmValueSettingById")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "预警设置id", required = true,dataType = "Integer")
    })
    @ApiOperation(value = "预警设置修改前回显")
    public Result<AlarmValueSettingVo> findAlarmValueSettingById(Integer id) {
        AlarmValueSettingVo alarmValueSettingVo = alarmValueSettingService.findAlarmValueSettingById(id);
        return new Result<>(true, StatusCode.OK, "预警设置修改前回显成功!",alarmValueSettingVo);
    }

}
