package com.rpzh.controller;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.pojo.EarlyWarning;
import com.rpzh.service.MaintainReviewService;
import com.rpzh.vo.MaintainPlanVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

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
    public Result<MaintainPlanVo> findMaintainPlanReview(Integer id) {
        List<MaintainPlanVo> maintainPlanVos = maintainReviewService.findMaintainPlanReview(id);
        return new Result(true, StatusCode.OK, "养护计划追溯查询成功!", maintainPlanVos);
    }

    @PostMapping("/findEarlyWarningReview")
    public Result<EarlyWarning> findEarlyWarningReview(Integer id) {
        List<EarlyWarning> maintainPlanVos = maintainReviewService.findEarlyWarningReview(id);
        return new Result(true, StatusCode.OK, "养护预警追溯查询成功!", maintainPlanVos);
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
