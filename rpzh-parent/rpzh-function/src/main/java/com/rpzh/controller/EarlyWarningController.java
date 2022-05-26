package com.rpzh.controller;

import com.rpzh.dto.EarlyWarningDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.service.EarlyWarningService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Random;

/**
 * @Author: zyq
 * @Date: 2022/05/20/17:34
 * @Description: 功能
 */
@RestController
@CrossOrigin
@RequestMapping("/maintain/alarmWarning")
@Api(value = "预警信息类", tags = "预警信息类", description = "预警信息类")
public class EarlyWarningController {
    @Resource
    private EarlyWarningService earlyWarningService;

    @PostMapping("test")
    public Result test(EarlyWarningDto earlyWarningDto){
        return earlyWarningService.findEarlyWarning(earlyWarningDto);
    }
}
