package com.rpzh.controller;

import com.rpzh.dto.PestBoardDto;
import com.rpzh.dto.PestMonitoringRuleDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.service.PestMonitoringService;
import com.rpzh.vo.PestMonitoringRuleVo;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * 养护任务中心
 * @Author liujj
 * @create 2022-06-08 11:31
 * @Description: good good study,day day up
 */
@RestController
@RequestMapping("/maintain/pestMonitoring")
@CrossOrigin
@Api(value = "虫情监控中心", tags = "重庆监控中心")
public class PestMonitoringController {

    @Autowired
    PestMonitoringService pestMonitoringService;

    @ApiOperation("添加虫情板")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pestBoardId", value = "虫情板Id", dataType = "String", required = true),
            @ApiImplicitParam(name = "pidRoute", value = "位置路由", dataType = "String", required = true),
            @ApiImplicitParam(name = "siteId", value = "位置id", dataType = "Integer", required = true),
            @ApiImplicitParam(name = "principalId", value = "负责人Id", dataType = "Integer", required = true),
            @ApiImplicitParam(name = "useDays", value = "使用天数", dataType = "Integer", required = true),
            @ApiImplicitParam(name = "remark", value = "备注", dataType = "String", required = true)
    })
    @PostMapping("/addPestBoard")
    public Result addPestBoard(@ApiIgnore PestBoardDto pestBoardDto){
        pestMonitoringService.addPestBoard(pestBoardDto);
        return new Result(true, StatusCode.OK,"添加成功");
    }
    @ApiOperation("删除虫情板")
    @ApiImplicitParam(name = "ids", value = "id集", dataType = "Integer", required = true,allowMultiple = true)
    @PostMapping("/deletePestBoard")
    public Result deletePestBoard(Integer [] ids){
        pestMonitoringService.deletePestBoard(ids);
        return new Result(true, StatusCode.OK,"删除成功");
    }

    @ApiOperation("虫情监控规则配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "weekPatrol", value = "巡查时间(0/1)",dataType = "Integer", required = true, allowMultiple = true),
            @ApiImplicitParam(name = "valueOne", value = "一级预警值",dataType = "Integer", required = true),
            @ApiImplicitParam(name = "valueTwo", value = "二级预警值",dataType = "Integer", required = true),
            @ApiImplicitParam(name = "valueThree", value = "三级预警值",dataType = "Integer", required = true),
            @ApiImplicitParam(name = "colorOne", value = "一级预警颜色",dataType = "String", required = true),
            @ApiImplicitParam(name = "colorTwo", value = "二级预警颜色",dataType = "String", required = true),
            @ApiImplicitParam(name = "colorThree", value = "三级预警颜色",dataType = "String", required = true),
            @ApiImplicitParam(name = "replacePeriod", value = "替换周期",dataType = "Integer", required = true),
            @ApiImplicitParam(name = "stackIds", value = "垛位id集合",dataType = "Integer",allowMultiple = true),
            @ApiImplicitParam(name = "stackWarningValues", value = "垛位预警值集合（每个垛位的预警值用|拼接）",dataType = "String", allowMultiple = true),
            @ApiImplicitParam(name = "delIds", value = "需要删除的特殊垛位的主键id集",dataType = "Integer", allowMultiple = true)
    })
    @PostMapping("/updatePestRule")
    public Result updatePestRule(PestMonitoringRuleDto pestMonitoringRuleDto){
        pestMonitoringService.updatePestRule(pestMonitoringRuleDto);
        return new Result(true, StatusCode.OK,"修改成功");
    }

    @ApiOperation("查询虫情监控规则")
    @GetMapping("/findPestRule")
    public Result<PestMonitoringRuleVo> findPestRule(){
        PestMonitoringRuleVo pestMonitoringRuleVo =pestMonitoringService.findPestRule();
        return new Result(true, StatusCode.OK,"查询成功",pestMonitoringRuleVo);
    }
}
