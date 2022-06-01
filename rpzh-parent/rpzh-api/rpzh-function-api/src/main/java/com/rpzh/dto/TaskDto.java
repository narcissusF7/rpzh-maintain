package com.rpzh.dto;

import lombok.Data;

/**
 * 任务中心管理前端传参
 * @Author liubc
 * @create 2022-05-30 14:49
 * @Description: good good study,day day up
 */
@Data
public class TaskDto {

    //任务编号
    private String taskNum;

    //任务类型
    private String type;

    //任务地点
    private String taskAddress;

    //推送方式
    private String pushWay;

    //发起时间
    private String createdTime;

    //截止时间
    private String endTime;

    //任务详情
    private String detail;

    //负责人
    private String principal;

    //任务状态
    private String status;

    //创建人
    private String createdUser;

}