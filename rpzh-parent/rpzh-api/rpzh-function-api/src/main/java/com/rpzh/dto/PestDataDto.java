package com.rpzh.dto;

import lombok.Data;
/**
 * @Author: ljj
 * @Date: 2022/05/18/15:30
 * @Description: 虫情数据传参
 */
@Data
public class PestDataDto {
    //虫情点位id
    private String pestBoardId;
    //当前页数
    private Integer page;
    //每页数量
    private Integer size;
    //开始时间
    private String startTime;
    //结束时间
    private String endTime;
    //位置类型
    private String siteType;
    //甲虫数量
    private Integer beetleCount;
    //粉螟数量
    private Integer mealwormCount;
    //采集位置
    private String gatherSite;
    //位置路由
    private String siteRoute;
    //预警等级
    private Integer alertLevel;
    private String rfid;
}
