package com.rpzh.vo;

import com.rpzh.pojo.NodeData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "天气返回类")
public class WeatherDataVo extends NodeDataVo {
    @ApiModelProperty(notes = "天气名称")
    private String weatherName;
    @ApiModelProperty(notes = "温度")
    private Double temp;
    @ApiModelProperty(notes = "湿度")
    private Double humidity;
    @ApiModelProperty(notes = "时间")
    private String createdTime;
    @ApiModelProperty(notes = "单位小时下雨量")
    private Double precip;
}