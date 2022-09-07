package com.rpzh.pojo;

import lombok.Data;

@Data
public class AirCondition {
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
    //湿度标签
    private String humidityTag;
}
