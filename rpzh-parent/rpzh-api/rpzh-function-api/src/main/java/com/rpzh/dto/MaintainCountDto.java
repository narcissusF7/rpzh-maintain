package com.rpzh.dto;

import lombok.Data;

/**
 * @author zyq
 * @description 养护数量前端传参
 */
@Data
public class MaintainCountDto {
    //物料代码
    private String materialCode;
    //开始时间
    private String startTime;
    //结束时间
    private String endTime;
    //养护方式
    private String maintenceStyle;
    //养护阶段
    private String phases;
    //位置路由
    private String pidRoute;
    private Integer page = 1;
    private Integer size = 10;
    //位置id
    private Integer stackId;
}
