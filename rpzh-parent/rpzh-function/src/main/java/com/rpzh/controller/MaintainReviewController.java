package com.rpzh.controller;

import com.rpzh.service.MaintainReviewService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
}
