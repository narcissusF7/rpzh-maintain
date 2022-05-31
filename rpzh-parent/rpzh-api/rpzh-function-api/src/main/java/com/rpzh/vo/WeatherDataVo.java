package com.rpzh.vo;

import com.rpzh.pojo.NodeData;
import lombok.Data;

@Data
public class WeatherDataVo extends NodeData {
    private String weatherName;
    private Double temp;
    private Double humidity;
    private String createdTime;
    private Double precip;
}