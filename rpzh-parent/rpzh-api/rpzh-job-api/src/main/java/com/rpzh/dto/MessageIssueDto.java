package com.rpzh.dto;

import lombok.Data;

/**
 * 短信下发传参
 * @Author liubc
 * @create 2022-07-26 16:46
 * @Description: good good study,day day up
 */
@Data
public class MessageIssueDto {

    //提示时间开始时间
    private String startTime;

    //提示时间结束时间
    private String endTime;

    //状态
    private String status;

}