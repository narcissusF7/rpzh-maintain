package com.rpzh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;
@Data
@ApiModel(description = "虫情预警配置返回类")
public class PestMonitoringRuleVo {
    @ApiModelProperty(notes = "巡查时间（周）0:1")
    Map<String, Integer> weekPatrol;
    @ApiModelProperty(notes = "虫情板替换周期")
    Integer replacePeriod;
    @ApiModelProperty(notes = "预警值，预警颜色值")
    Map<String,Object> pestMonitorValues;
    @ApiModelProperty(notes = "特殊垛位的数据 siteName 位置名称 ， siteRoute 位置路由 ，little 一级预警值 middle 二级预警值，big 三级预警值")
    List<Map<String ,Object>> specialStacks;
    @ApiModelProperty(notes = "一级预警值")
    Double valueOne;
    @ApiModelProperty(notes = "二级预警值")
    Double valueTwo;
    @ApiModelProperty(notes = "三级预警值")
    Double valueThree;

    public PestMonitoringRuleVo() {
    }

    public PestMonitoringRuleVo(Map<String, Integer> weekPatrol, Integer replacePeriod, Map<String, Object> pestMonitorValues, List<Map<String, Object>> specialStacks) {
        this.weekPatrol = weekPatrol;
        this.replacePeriod = replacePeriod;
        this.pestMonitorValues = pestMonitorValues;
        this.specialStacks = specialStacks;
    }
}

