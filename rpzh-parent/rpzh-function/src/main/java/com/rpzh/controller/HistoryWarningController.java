package com.rpzh.controller;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.EarlyWarningDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.service.EarlyWarningService;
import com.rpzh.vo.EarlyWarningVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;

/**
 * @Author: zyq
 * @Date: 2022/05/20/17:34
 * @Description: 历史预警
 */
@RestController
@CrossOrigin
@RequestMapping("/maintain/historyWarning")
@Api(value = "历史预警类", tags = "历史预警类", description = "历史预警类")
public class HistoryWarningController {
    @Resource
    private EarlyWarningService earlyWarningService;

    @PostMapping(value = "/findHistoryWarning")
    @ApiOperation(value = "历史预警列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pidRoute", value = "位置路由", required = false, dataType = "String"),
            @ApiImplicitParam(name = "stackId", value = "位置ID", required = false, dataType = "Integer"),
            @ApiImplicitParam(name = "nodeId", value = "设备ID", required = false, dataType = "String"),
            @ApiImplicitParam(name = "principalName", value = "负责人", required = false, dataType = "String"),
            @ApiImplicitParam(name = "warningType", value = "预警类型 01:设备电量故障,02:设备信号强度故障,03:设备丢包故障,04:垛内虫数预警,05:仓间虫数预警,06:垛内氧气预警,07:仓间氧气预警,08:垛内温度预警,09:仓间温度预警,10:垛内湿度预警,11:仓间湿度预警,12:包芯温度预警,13:平均包芯高于环境预警", required = false, dataType = "String"),
            @ApiImplicitParam(name = "warningGrade", value = "预警等级,1:一级预警,2:二级预警,3:三级预警", required = false, dataType = "Integer"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "size", value = "每页数量", required = false,dataType = "Integer"),
            @ApiImplicitParam(name = "page", value = "当前页",required = false,dataType ="Integer")
    })
    public Result findHistoryWarning(@ApiIgnore EarlyWarningDto earlyWarningDto){
        PageInfo<EarlyWarningVo> historyWarningInfos = earlyWarningService.findHistoryWarning(earlyWarningDto);
        return new Result(true, StatusCode.OK, "查询历史预警列表成功",historyWarningInfos);
    }
}
