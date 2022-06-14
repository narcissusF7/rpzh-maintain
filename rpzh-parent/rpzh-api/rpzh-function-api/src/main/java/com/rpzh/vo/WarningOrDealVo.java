package com.rpzh.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: zyq
 * @Date: 2022/06/11/16:16
 * @Description: 功能
 */
@Data
public class WarningOrDealVo {
    @ApiModelProperty(notes = "预警类型 01:监测设备故障,04:垛内虫数预警,05:仓间虫数预警,06:垛内氧气预警,07:仓间氧气预警,08:垛内温度预警,09:仓间温度预警,10:垛内湿度预警,11:仓间湿度预警,12:包芯温度预警,13:平均包芯高于环境预警")
    private String warningType;
    @ApiModelProperty(notes = "预警类型个数")
    private Integer count;
    @ApiModelProperty(notes = "所站的比率")
    private Double percentage;
    @ApiModelProperty(notes = "是否处理,0:未处理,1:已处理")
    private Integer deal;
    @ApiModelProperty(notes = "处理时间")
    private String dayType;
}
