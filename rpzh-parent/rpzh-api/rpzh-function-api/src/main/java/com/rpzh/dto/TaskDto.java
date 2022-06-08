package com.rpzh.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 任务中心管理前端传参
 *
 * @Author liubc
 * @create 2022-05-30 14:49
 * @Description: good good study,day day up
 */
@Data
public class TaskDto {

    //主键id
    private String id;

    //任务编号
    private String taskNum;

    //任务类型
    private String type;

    //任务地点
    private String taskAddress;

    //推送方式
    private String pushWay;

    //处理措施
    private String dealMethod;

    //处理结果
    private String dealResult;

    //发起时间
    private String createdTime;

    //结束时间
    private String endTime;

    //结束开始时间
    private String endStartTime;

    //过期时间
    private String expiresTime;

    //任务完成时间
    private String finishTime;

    //任务详情
    private String detail;

    //任务评价
    private String evaluate;

    //评价人
    private String evaluateUser;

    //任务评价时间
    private String evaluateTime;

    //负责人
    private String principal;

    //任务状态
    private String status;

    //创建人
    private String createdUser;

    //分页开始页面
    private int page;

    //分页每页显示
    private int size;

}