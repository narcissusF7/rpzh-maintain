package com.rpzh.vo;

import lombok.Data;

import java.util.Map;

@Data
public class AirConditionVo {
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

    public AirConditionVo() {
    }

    public AirConditionVo(Map<Integer,String> map) {
        this.airConditionCode = map.get(0);
        this.tagCN = map.get(1);
        this.siteRoute = map.get(2);
        this.tempTag = map.get(3);
        this.tempVal = Double.parseDouble(map.get(5));
        this.humidityTag = map.get(4);
        this.humidityVal = Double.parseDouble(map.get(6));
    }
}
