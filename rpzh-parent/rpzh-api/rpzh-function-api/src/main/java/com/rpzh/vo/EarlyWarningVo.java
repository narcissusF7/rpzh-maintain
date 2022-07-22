package com.rpzh.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Author: zyq
 * @Date: 2022/05/19/14:03
 * @Description: 返回
 */
@Data
@ApiModel(description = "预警信息返回")
public class EarlyWarningVo {
    @ApiModelProperty(notes ="主键id")
    private Integer id;
    @ApiModelProperty(notes ="上限一级")
    private Double upperOne;
    @ApiModelProperty(notes ="上限二级")
    private Double upperTwo;
    @ApiModelProperty(notes ="上限三级")
    private Double upperThree;
    @ApiModelProperty(notes = "下限")
    private Double lower=0d;
    @ApiModelProperty(notes = "x坐标")
    private String siteX;
    @ApiModelProperty(notes = "y坐标")
    private String siteY;
    @ApiModelProperty(notes = "z坐标")
    private String siteZ;
    @ApiModelProperty(notes = "预警等级,1:一级预警,2:二级预警,3:三级预警")
    private Integer warningGrade;
    @ApiModelProperty(notes = "预警信息")
    private String warningNews;
    @ApiModelProperty(notes = "预警类型:01:设备电量故障,02:设备信号强度故障,03:设备丢包故障,04:垛内甲虫预警,05:仓间甲虫预警,06:垛内氧气预警,07:仓间氧气预警,08:垛内温度预警,09:仓间温度预警,10:垛内湿度预警,11:仓间湿度预警,12:包芯温度预警,13:平均包芯高于环境预警,14:仓间粉螟预警")
    private String warningType;
    @ApiModelProperty(notes = "预警类型名称警")
    private String warningName;
    @ApiModelProperty(notes = "设备id")
    private String nodeId;
    @ApiModelProperty(notes = "垛位id")
    private Integer stackId;
    @ApiModelProperty(notes = "位置名称")
    private String siteName;
    @ApiModelProperty(notes = "物料等级")
    private String materialCode;
    @ApiModelProperty(notes = "负责人")
    private String principalName;
    @ApiModelProperty(notes = "是否确认,0:已处理,1:已预警,2,未确认")
    private String affirm;
    @ApiModelProperty(notes = "处理")
    private String affirmName;
    @ApiModelProperty(notes = "处理时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dealTime;
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
    @ApiModelProperty(notes = "备注")
    private String remark;
    @ApiModelProperty(notes = "处理措施")
    private String dealSuggestion;
    @ApiModelProperty(notes = "仓间id")
    private Integer areaId;
    @ApiModelProperty(notes = "位置路由")
    private String siteRoute;
    private String nodeName;
    private String color;
}
