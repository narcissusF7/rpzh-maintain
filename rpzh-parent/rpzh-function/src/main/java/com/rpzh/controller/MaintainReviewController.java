package com.rpzh.controller;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.service.MaintainReviewService;
import com.rpzh.vo.MaintainPlanVo;
import io.swagger.annotations.Api;
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
    public Result<MaintainPlanVo> findReviewTable(MaintainPlanDto maintainPlanDto) {
        PageInfo<MaintainPlanVo> maintainPlanVos = maintainReviewService.findReviewTable(maintainPlanDto);
        return new Result(true, StatusCode.OK, "查询成功!", maintainPlanVos);
    }
}
