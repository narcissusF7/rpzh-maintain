package com.rpzh.dto;

import lombok.Data;

/**
 * @Author liubc成
 * @create 2021/9/2 14:27
 * 登录日志
 */
@Data
public class LoginLogDto {

    //登录地址
    private String loginAddress;

    //用户名称
    private String username;

    //状态
    private Integer status;

    //开始时间
    private String startTime;

    //结束时间
    private String endTime;

    //分页开始页面
    private int page;

    //分页每页显示
    private int size = 10;

}