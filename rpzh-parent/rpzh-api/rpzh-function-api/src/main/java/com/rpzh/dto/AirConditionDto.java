package com.rpzh.dto;

import lombok.Data;

@Data
public class AirConditionDto {
    //主键id
    private int id;
    //空调编号
    private String airConditionCode;
    //中文名称
    private String tagCN;
    //位置路由
    private String siteRoute;
    //温度标签
    private String tempTag;
    //温度数据
    private Double tempVal;
    //湿度标签
    private String humidityTag;
    //湿度数据
    private Double humidityVal;
}
