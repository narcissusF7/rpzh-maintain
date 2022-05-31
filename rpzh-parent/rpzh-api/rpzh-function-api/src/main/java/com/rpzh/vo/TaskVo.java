package com.rpzh.vo;

import lombok.Data;

/**
 * @Author liubc
 * @create 2022-05-30 16:08
 * @Description: good good study,day day up
 */
@Data
public class TaskVo {
    //任务编号
    private String taskNum;

    //任务类型
    private String type;

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
}