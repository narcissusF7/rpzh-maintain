package com.rpzh.vo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.rpzh.pojo.NodeData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(description = "天气返回类")
public class WeatherDataVo extends NodeDataVo {
    @Excel(
            name = "天气名称",
            orderNum = "1"
    )
    @ApiModelProperty(notes = "天气名称")
    private String weatherName;
    @Excel(
            name = "温度",
            orderNum = "2"
    )
    @ApiModelProperty(notes = "温度")
    private Double temp;
    @Excel(
            name = "湿度",
            orderNum = "3"
    )
    @ApiModelProperty(notes = "湿度")
    private Double humidity;
    @Excel(
            name = "时间",
            orderNum = "4"
    )
    @ApiModelProperty(notes = "时间")
    private String transferTime;
    @Excel(
            name = "单位小时下雨量",
            orderNum = "5"
    )
    @ApiModelProperty(notes = "单位小时下雨量")
    private Double precip;
}