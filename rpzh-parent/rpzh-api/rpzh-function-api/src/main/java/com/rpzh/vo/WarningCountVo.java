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
    @ApiModelProperty(notes = "预警类型 01:设备电量故障,02:设备信号强度故障,03:设备丢包故障,04:垛内虫数预警,05:仓间虫数预警,06:垛内氧气预警,07:仓间氧气预警,08:垛内温度预警,09:仓间温度预警,10:垛内湿度预警,11:仓间湿度预警,12:包芯温度预警,13:平均包芯高于环境预警")
    private String warningType;
    private String warningGrade;
    @ApiModelProperty(notes = "预警类型名称")
    private String warningName;
    @ApiModelProperty(notes = "一级预警颜色")
    private String colorOne;
    @ApiModelProperty(notes = "二级预警颜色")
    private String colorTwo;
    @ApiModelProperty(notes = "三级预警颜色")
    private String colorThree;
}
