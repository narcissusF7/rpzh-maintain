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
    @ApiModelProperty(notes = "预警个数")
    private Integer warningCount;
    @ApiModelProperty(notes = "预警等级,1:一级预警,2:二级预警,3:三级预警")
    private String warningGrade;
    @ApiModelProperty(notes = "预警类型,01:检测设备故障,02:虫数超限预警,03:温度超限预警,04:湿度超限预警,05:氧气超限预警")
    private String warningType;
}
