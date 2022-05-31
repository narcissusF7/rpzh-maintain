package com.rpzh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: zyq
 * @Date: 2022/05/20/14:34
 * @Description: 预警统计返回
 */
@Data
@ApiModel(description = "预警统计返回数据")
public class WarningCountVo {
    @ApiModelProperty(notes = "一级预警个数")
    private Integer oneWarningCount=0;
    @ApiModelProperty(notes = "二级预警个数")
    private Integer twoWarningCount=0;
    @ApiModelProperty(notes = "三级预警个数")
    private Integer threeWarningCount=0;
    @ApiModelProperty(notes = "预警类型,01:设备电量故障,02:设备信号强度故障,03:设备故障,04:虫数超限预警,05:包芯温度超限预警,06:环境温度超限预警,07:环境湿度超限预警,08:氧气超限预警,09:垛位温度预警")
    private String warningType;
}
