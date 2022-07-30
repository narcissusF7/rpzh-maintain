package com.rpzh.controller;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.pojo.EarlyWarning;
import com.rpzh.service.MaintainReviewService;
import com.rpzh.vo.EarlyWarningVo;
import com.rpzh.vo.MaintainPlanVo;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Author: zyq
 * @Date: 2022/07/16/13:25
 * @Description: 养护追溯控制层
 */
@RestController
@CrossOrigin
@RequestMapping("/maintain/maintainReview")
@Api(value = "养护追溯类", tags = "养护追溯类", description = "养护追溯类")
public class MaintainReviewController {
    @Resource
    private MaintainReviewService maintainReviewService;
    @PostMapping("/findReviewTable")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "materialCode", value = "物料代码", required = false,dataType = "String"),
            @ApiImplicitParam(name = "maintenceStyle", value = "养护方式", required = false,dataType = "String"),
            @ApiImplicitParam(name = "phases", value = "养护阶段", required = false,dataType = "String"),
            @ApiImplicitParam(name = "principalId", value = "负责人id", required = false,dataType = "String"),
            @ApiImplicitParam(name = "pidRoute", value = "养护阶段", required = false,dataType = "String"),
            @ApiImplicitParam(name = "stackId", value = "养护阶段", required = false,dataType = "String")
    })
    @ApiOperation(value = "养护追溯列表")
    public Result<MaintainPlanVo> findReviewTable(MaintainPlanDto maintainPlanDto) {
        PageInfo<MaintainPlanVo> maintainPlanVos = maintainReviewService.findReviewTable(maintainPlanDto);
        return new Result(true, StatusCode.OK, "查询成功!", maintainPlanVos);
    }

    @PostMapping("/findMaintainPlanReview")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户计划id", required = true,dataType = "String")
    })
    @ApiOperation(value = "养护计划追溯列表")
    public Result<MaintainPlanVo> findMaintainPlanReview(Integer id) {
        List<MaintainPlanVo> maintainPlanVos = maintainReviewService.findMaintainPlanReview(id);
        return new Result(true, StatusCode.OK, "养护计划追溯查询成功!", maintainPlanVos);
    }

    @PostMapping("/findEarlyWarningReview")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户计划id", required = true,dataType = "String")
    })
    @ApiOperation(value = "养护预警追溯列表")
    public Result<EarlyWarningVo> findEarlyWarningReview(Integer id) {
        List<EarlyWarningVo> maintainPlanVos = maintainReviewService.findEarlyWarningReview(id);
        return new Result(true, StatusCode.OK, "养护预警追溯查询成功!", maintainPlanVos);
    }

    @PostMapping("/findDataReview")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户计划id", required = true,dataType = "String")
    })
    @ApiOperation(value = "养护数据追溯列表")
    public Result findDataReview(String tableType, Integer id, String startTime, String endTime) {
        if(ToolUtil.isEmpty(id)){
            return new Result(false, StatusCode.ERROR, "养护计划id不能为空!");
        }
        if(ToolUtil.isEmpty(tableType)){
            return new Result(false, StatusCode.ERROR, "设备类型不能为空!");
        }
        List<Map> dataReviews = maintainReviewService.findDataReview(tableType, id, startTime, endTime);
        return new Result(true, StatusCode.OK, "养护数据追溯查询成功!",dataReviews);
    }

    @PostMapping("/findMaintainBaseInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "养护计划id", required = true,dataType = "int")
    })
    @ApiOperation(value = "养护追溯详情中的基础信息")
    public Result<MaintainPlanVo> findMaintainBaseInfo(Integer id) {
        MaintainPlanVo maintainBaseInfo = maintainReviewService.findMaintainBaseInfo(id);
        return new Result(true, StatusCode.OK, "查询成功!", maintainBaseInfo);
    }
}
