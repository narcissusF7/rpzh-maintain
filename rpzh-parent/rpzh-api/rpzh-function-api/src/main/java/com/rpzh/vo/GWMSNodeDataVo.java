package com.rpzh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "GWMS数据返回类")
public class GWMSNodeDataVo {
    @ApiModelProperty(notes = "设备id")
    private String nodeId;
    @ApiModelProperty(notes = "数据传输时间")
    private String transferTime;
    @ApiModelProperty(notes = "氧气浓度")
    private Double oxygen;
    @ApiModelProperty(notes = "温度")
    private Double temp;
    @ApiModelProperty(notes = "湿度")
    private Double humiture;
    @ApiModelProperty(notes = "采集位置")
    private String gatherSite;
    @ApiModelProperty(notes = "位置路由")
    private String pidRoute;
    @ApiModelProperty(notes = "设备类型 01氧气 03 温湿度 04 包芯温度")
    private String nodeType;

}

