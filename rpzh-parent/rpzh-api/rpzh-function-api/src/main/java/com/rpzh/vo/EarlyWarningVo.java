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
    @ApiModelProperty(notes = "预警等级,1:一级预警,2:二级预警,3:三级预警")
    private String warningGrade;
    @ApiModelProperty(notes = "预警类型,01:电量预警,02:信号强度预警,03:设备丢包,04:虫数超限预警,05:包芯温度超限预警,06:环境温度超限预警,07:环境湿度超限预警,08:氧气超限预警,09:垛位温度预警")
    private String warningType;
    @ApiModelProperty(notes = "设备id")
    private String nodeId;
    @ApiModelProperty(notes = "垛位id")
    private String stackId;
    @ApiModelProperty(notes = "是否确认,0:误报,1:已预警,2,未确认")
    private String affirm;
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
    @ApiModelProperty(notes = "最新预警时间")
    private String warningTime;
}
