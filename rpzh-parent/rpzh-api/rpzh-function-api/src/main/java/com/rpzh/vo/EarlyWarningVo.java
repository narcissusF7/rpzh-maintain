package com.rpzh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: zyq
 * @Date: 2022/05/19/14:03
 * @Description: EarlyWarningVoEarlyWarningVo返回
 */
@Data
@ApiModel(description = "预警返回数据")
public class EarlyWarningVo {
    @ApiModelProperty(notes = "预警id")
    private Integer id;
    @ApiModelProperty(notes = "预警等级,1:一级预警,2:二级预警,3:三级预警")
    private Integer warningGrade;
    @ApiModelProperty(notes = "预警类型,01:检测设备故障,02:虫数超限预警,03:温度超限预警,04:湿度超限预警,05:氧气超限预警")
    private String warningType;
    @ApiModelProperty(notes = "设备id")
    private String nodeId;
    @ApiModelProperty(notes = "是否确认,0:未确认,1:已预警,2,误报")
    private Integer isAffirm;
    @ApiModelProperty(notes = "预警设备类型  01：无线氧气传感器，03：无线温湿度传感器,04： 无线包芯温度传感器")
    private String nodeType;
    @ApiModelProperty(notes = "一级预警值")
    private Double warningValOne;
    @ApiModelProperty(notes = "二级预警值")
    private Double warningValTwo;
    @ApiModelProperty(notes = "三级预警值")
    private Double warningValThree;
    @ApiModelProperty(notes = "一级预警时间")
    private String warningTimeOne;
    @ApiModelProperty(notes = "二级预警时间")
    private String warningTimeTwo;
    @ApiModelProperty(notes = "三级预警时间")
    private String warningTimeThree;
    @ApiModelProperty(notes = "最新预警值")
    private Double warningVal;
    @ApiModelProperty(notes = "最新预警值")
    private Double warningTime;
    @ApiModelProperty(notes = "垛位id")
    private Integer stackId;
}
